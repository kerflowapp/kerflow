package com.kerflowapp.kerflow.api.billing.domain;

import com.kerflowapp.kerflow.domain.enums.BillingInterval;

public record CheckoutRequest(
    BillingInterval interval
) {
}
