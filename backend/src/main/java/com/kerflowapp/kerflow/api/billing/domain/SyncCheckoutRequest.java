package com.kerflowapp.kerflow.api.billing.domain;

import jakarta.validation.constraints.NotEmpty;

public record SyncCheckoutRequest(
    @NotEmpty String sessionId
) {
}
