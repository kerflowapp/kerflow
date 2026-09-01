package com.kerflowapp.kerflow.api.users.domain;

import jakarta.annotation.Nullable;

public record UpdateUserRequest(
    @Nullable
    String firstName,
    @Nullable
    String lastName,
    @Nullable
    String phoneNumber,
    @Nullable
    String profession
) {
}
