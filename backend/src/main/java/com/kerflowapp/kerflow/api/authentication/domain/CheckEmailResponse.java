package com.kerflowapp.kerflow.api.authentication.domain;

public record CheckEmailResponse(
    boolean exists,
    boolean pendingConfirmation
) {
}
