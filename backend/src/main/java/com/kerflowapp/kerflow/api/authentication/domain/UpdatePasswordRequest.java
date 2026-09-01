package com.kerflowapp.kerflow.api.authentication.domain;

import java.util.UUID;

public record UpdatePasswordRequest(
    UpdatePasswordActionType action,
    String email,
    String password,
    UUID confirmationCode
) {
}
