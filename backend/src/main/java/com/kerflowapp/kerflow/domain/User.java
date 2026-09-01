package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.BillingInterval;
import com.kerflowapp.kerflow.domain.enums.FeatureFlag;
import com.kerflowapp.kerflow.domain.enums.SubscriptionStatus;
import com.kerflowapp.kerflow.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "public")
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;
    private UUID confirmationCode;
    private String verificationCode;
    private Instant verificationCodeExpiresAt;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String login;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String city;
    private String profession;

    private Instant trialEndsAt;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
    @Enumerated(EnumType.STRING)
    private BillingInterval billingInterval;
    private Instant currentPeriodEnd;
    private Instant cancelledAt;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user")
    private List<Prospect> prospects;

    @Enumerated(EnumType.STRING)
    private List<FeatureFlag> featureFlags;

    public String fullName() {
        return firstName + " " + lastName;
    }

}
