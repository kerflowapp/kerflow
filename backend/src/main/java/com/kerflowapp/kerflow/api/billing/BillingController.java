package com.kerflowapp.kerflow.api.billing;

import com.kerflowapp.kerflow.api.authentication.domain.SubscriptionDto;
import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.billing.domain.CheckoutRequest;
import com.kerflowapp.kerflow.api.billing.domain.CheckoutSessionDto;
import com.kerflowapp.kerflow.api.billing.domain.PortalSessionDto;
import com.kerflowapp.kerflow.api.billing.domain.SyncCheckoutRequest;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.BillingInterval;
import com.kerflowapp.kerflow.services.billing.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/billing")
public class BillingController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutSessionDto> createCheckoutSession(@CurrentLoggedUser User loggedUser,
                                                                    @RequestBody(required = false) CheckoutRequest request) {

        BillingInterval interval = (request != null && request.interval() != null)
            ? request.interval()
            : BillingInterval.MONTHLY;
        String url = subscriptionService.createCheckoutSession(loggedUser, interval);
        return ResponseEntity.ok(new CheckoutSessionDto(url));
    }

    @PostMapping("/checkout/sync")
    public ResponseEntity<SubscriptionDto> syncCheckoutSession(@CurrentLoggedUser User loggedUser,
                                                               @Valid @RequestBody SyncCheckoutRequest request) {

        SubscriptionDto subscription = subscriptionService.syncFromCheckoutSession(loggedUser, request.sessionId());
        return ResponseEntity.ok(subscription);
    }

    @PostMapping("/portal")
    public ResponseEntity<PortalSessionDto> createPortalSession(@CurrentLoggedUser User loggedUser) {
        String url = subscriptionService.createPortalSession(loggedUser);
        return ResponseEntity.ok(new PortalSessionDto(url));
    }
}
