package com.kerflowapp.kerflow.api.authentication.domain;

import jakarta.validation.constraints.NotEmpty;

public record VerifyOtpRequest(
    @NotEmpty String email,
    @NotEmpty String verificationCode
) {
}
