package com.kerflowapp.kerflow.services;

public record CognitoAuthResult(
    String accessToken,
    String refreshToken
) {
}
