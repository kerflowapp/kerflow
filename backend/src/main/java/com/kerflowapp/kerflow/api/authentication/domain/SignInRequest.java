package com.kerflowapp.kerflow.api.authentication.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SignInRequest(
    @NotNull
    String username,
    @NotNull
    String password,
    String impersonateEmail
) {

    public String username() {
        return username.toLowerCase().trim();
    }

}
