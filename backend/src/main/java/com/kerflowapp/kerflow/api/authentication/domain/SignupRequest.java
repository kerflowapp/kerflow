package com.kerflowapp.kerflow.api.authentication.domain;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record SignupRequest(
    @NotEmpty
    String login,
    @NotEmpty
    String password,
    @Nullable
    String firstName,
    @Nullable
    String lastName,
    @Nullable
    String phoneNumber
) {

    @Override
    public String login() {
        return login.toLowerCase().trim();
    }

}
