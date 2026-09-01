package com.kerflowapp.kerflow.api.authentication.domain;

import lombok.Builder;
import lombok.With;

import java.util.List;
import java.util.UUID;

@With
@Builder
public record UserDto(
    UUID id,
    String login,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String city,
    String profession,
    List<NotificationDTO> notifications,
    SubscriptionDto subscription
) {
}
