package com.kerflowapp.kerflow.services.billing;

import com.kerflowapp.kerflow.BaseConfiguration;
import com.kerflowapp.kerflow.api.authentication.domain.SubscriptionDto;
import com.kerflowapp.kerflow.configuration.StripeConfiguration.StripeProperties;
import com.kerflowapp.kerflow.configuration.rest.authorization.ControllerGuardService;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.BillingInterval;
import com.kerflowapp.kerflow.domain.enums.SubscriptionStatus;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.repositories.UserRepository;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.CHECKOUT_SESSION_INVALID;
import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.STRIPE_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final long TRIAL_DURATION_DAYS = 3;

    private final UserRepository userRepository;
    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final BaseConfiguration baseConfiguration;
    private final ControllerGuardService controllerGuardService;

    public void ensureTrialInitialized(User user) {
        if (user.getTrialEndsAt() == null) {
            user.setTrialEndsAt(Instant.now().plus(TRIAL_DURATION_DAYS, ChronoUnit.DAYS));
            userRepository.save(user);
        }
    }

    public SubscriptionDto toSubscriptionDto(User user) {
        return SubscriptionDto.builder()
            .status(effectiveStatus(user))
            .interval(user.getBillingInterval())
            .trialEndsAt(user.getTrialEndsAt())
            .currentPeriodEnd(user.getCurrentPeriodEnd())
            .cancelledAt(user.getCancelledAt())
            .hasAccess(controllerGuardService.hasActiveAccess(user))
            .build();
    }

    public String createCheckoutSession(User user, BillingInterval interval) {
        try {
            String customerId = getOrCreateCustomer(user);
            String priceId = interval == BillingInterval.ANNUAL
                ? stripeProperties.priceIdAnnual()
                : stripeProperties.priceId();

            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setClientReferenceId(user.getId().toString())
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setPrice(priceId)
                    .setQuantity(1L)
                    .build())
                .setSuccessUrl(baseConfiguration.getWebUrl() + "/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(baseConfiguration.getWebUrl() + "/settings/billing?checkout=cancelled")
                .build();

            Session session = stripeClient.v1().checkout().sessions().create(params);
            return session.getUrl();
        } catch (StripeException e) {
            LOGGER.error("failed to create stripe checkout session for user {}", user.getId(), e);
            throw new KerflowException(STRIPE_ERROR);
        }
    }

    public SubscriptionDto syncFromCheckoutSession(User user, String sessionId) {
        try {
            Session session = stripeClient.v1().checkout().sessions().retrieve(sessionId);

            if (!user.getId().toString().equals(session.getClientReferenceId())) {
                throw new KerflowException(CHECKOUT_SESSION_INVALID);
            }
            if (session.getSubscription() == null) {
                throw new KerflowException(CHECKOUT_SESSION_INVALID);
            }

            Subscription subscription = stripeClient.v1().subscriptions().retrieve(session.getSubscription());
            applySubscription(user, subscription);
            userRepository.save(user);

            return toSubscriptionDto(user);
        } catch (StripeException e) {
            LOGGER.error("failed to sync stripe checkout session '{}' for user {}", sessionId, user.getId(), e);
            throw new KerflowException(STRIPE_ERROR);
        }
    }

    public void refreshIfStale(User user) {
        boolean stale = user.getStripeSubscriptionId() != null
            && (SubscriptionStatus.ACTIVE.equals(user.getSubscriptionStatus())
            || SubscriptionStatus.PAST_DUE.equals(user.getSubscriptionStatus()))
            && user.getCurrentPeriodEnd() != null
            && user.getCurrentPeriodEnd().isBefore(Instant.now());

        boolean missingInterval = user.getStripeSubscriptionId() != null
            && SubscriptionStatus.ACTIVE.equals(user.getSubscriptionStatus())
            && user.getBillingInterval() == null;

        if (!stale && !missingInterval) {
            return;
        }

        try {
            Subscription subscription = stripeClient.v1().subscriptions().retrieve(user.getStripeSubscriptionId());
            applySubscription(user, subscription);
            userRepository.save(user);
            LOGGER.info("refreshed stale stripe subscription for user {}: {}", user.getId(), user.getSubscriptionStatus());
        } catch (StripeException e) {
            LOGGER.warn("could not refresh stripe subscription for user {}: {}", user.getId(), e.getMessage());
        }
    }

    public String createPortalSession(User user) {
        if (user.getStripeCustomerId() == null) {
            throw new KerflowException(STRIPE_ERROR);
        }
        try {
            com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                    .setCustomer(user.getStripeCustomerId())
                    .setReturnUrl(baseConfiguration.getWebUrl() + "/settings/billing")
                    .build();
            return stripeClient.v1().billingPortal().sessions().create(params).getUrl();
        } catch (StripeException e) {
            LOGGER.error("failed to create stripe portal session for user {}", user.getId(), e);
            throw new KerflowException(STRIPE_ERROR);
        }
    }

    private String getOrCreateCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }

        Customer customer = stripeClient.v1().customers().create(CustomerCreateParams.builder()
            .setEmail(user.getEmail())
            .setName(user.fullName())
            .putMetadata("userId", user.getId().toString())
            .build());

        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        return customer.getId();
    }

    private void applySubscription(User user, Subscription subscription) {
        user.setStripeSubscriptionId(subscription.getId());
        user.setSubscriptionStatus(mapStripeStatus(subscription.getStatus()));
        user.setBillingInterval(extractBillingInterval(subscription));
        user.setCurrentPeriodEnd(extractCurrentPeriodEnd(subscription));
        user.setCancelledAt(toInstant(subscription.getCanceledAt() != null
            ? subscription.getCanceledAt()
            : subscription.getCancelAt()));
    }

    private BillingInterval extractBillingInterval(Subscription subscription) {
        if (subscription.getItems() == null || subscription.getItems().getData().isEmpty()) {
            return null;
        }
        String priceId = subscription.getItems().getData().getFirst().getPrice().getId();
        return priceId.equals(stripeProperties.priceIdAnnual())
            ? BillingInterval.ANNUAL
            : BillingInterval.MONTHLY;
    }

    private Instant extractCurrentPeriodEnd(Subscription subscription) {
        if (subscription.getItems() == null || subscription.getItems().getData().isEmpty()) {
            return null;
        }
        return toInstant(subscription.getItems().getData().getFirst().getCurrentPeriodEnd());
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active", "trialing" -> SubscriptionStatus.ACTIVE;
            case "past_due", "unpaid" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELLED;
            default -> SubscriptionStatus.INACTIVE;
        };
    }

    private SubscriptionStatus effectiveStatus(User user) {
        if (user.getSubscriptionStatus() == null) {
            boolean trialActive = user.getTrialEndsAt() != null && user.getTrialEndsAt().isAfter(Instant.now());
            return trialActive ? SubscriptionStatus.TRIALING : SubscriptionStatus.INACTIVE;
        }
        return user.getSubscriptionStatus();
    }

    private Instant toInstant(Long epochSeconds) {
        return epochSeconds != null ? Instant.ofEpochSecond(epochSeconds) : null;
    }
}
