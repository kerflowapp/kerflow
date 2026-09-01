package com.kerflowapp.kerflow.configuration.rest.authorization;

import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.FeatureFlag;
import com.kerflowapp.kerflow.domain.enums.SubscriptionStatus;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.services.SuperAdminEmails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.UNAUTHORIZED;
import static java.util.Optional.ofNullable;


@Slf4j
@Service("guard")
@RequiredArgsConstructor
public class ControllerGuardService {

    private final SuperAdminEmails superAdminEmails;

    public boolean hasFeaturesAccess(User user, FeatureFlag feature) {
        return ofNullable(user.getFeatureFlags())
            .map(f -> f.contains(feature))
            .orElse(false);
    }

    public void checkIsAdmin(User loggedUser) {
        if (!superAdminEmails.contains(loggedUser.getLogin())) {
            throw new KerflowException(UNAUTHORIZED);
        }
    }

    public boolean hasActiveAccess(User user) {
        Instant now = Instant.now();
        boolean subscriptionActive = SubscriptionStatus.ACTIVE.equals(user.getSubscriptionStatus())
            && user.getCurrentPeriodEnd() != null
            && user.getCurrentPeriodEnd().isAfter(now);
        boolean trialActive = user.getTrialEndsAt() != null && user.getTrialEndsAt().isAfter(now);
        return subscriptionActive || trialActive;
    }
}

