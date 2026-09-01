package com.kerflowapp.kerflow.services;

import com.kerflowapp.kerflow.api.authentication.domain.SignInRequest;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.INVALID_USER_OR_PASSWORD;
import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.USER_ALREADY_EXISTS;

@Slf4j
@Service
public class CognitoService {

    public static final String ATTR_PASSWORD = "PASSWORD";
    public static final String ATTR_USERNAME = "USERNAME";
    public static final String ATTR_SECRET_HASH = "SECRET_HASH";

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
    private final String poolId;
    private final String clientId;
    private final String clientSecret;

    public CognitoService(
        @Value("${aws.cognito.pool-id}") String poolId,
        @Value("${aws.cognito.client-id}") String clientId,
        @Value("${aws.cognito.client-secret}") String clientSecret,
        CognitoIdentityProviderClient cognitoIdentityProviderClient) {

        this.poolId = poolId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
    }

    public void createCognitoUser(String login, String password) {
        try {
            AttributeType loginAttribute = AttributeType.builder().name("email").value(login).build();
            SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(login))
                .username(login)
                .password(password)
                .userAttributes(loginAttribute)
                .build();
            cognitoIdentityProviderClient.signUp(signUpRequest);
        } catch (UsernameExistsException usernameExistsException) {
            throw new KerflowException(USER_ALREADY_EXISTS);
        } catch (CognitoIdentityProviderException exception) {
            LOGGER.error("cognito signUp failed for {}: {}", login, exception.awsErrorDetails().errorMessage(), exception);
            throw exception;
        }
    }

    public void createAndConfirmCognitoUser(String login, String password) {
        try {
            AttributeType loginAttribute = AttributeType.builder().name("email").value(login).build();
            SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(login))
                .username(login)
                .password(password)
                .userAttributes(loginAttribute)
                .build();
            cognitoIdentityProviderClient.signUp(signUpRequest);
            AdminConfirmSignUpRequest request = AdminConfirmSignUpRequest.builder()
                .userPoolId(poolId)
                .username(login)
                .build();
            cognitoIdentityProviderClient.adminConfirmSignUp(request);
        } catch (UsernameExistsException usernameExistsException) {
            throw new KerflowException(USER_ALREADY_EXISTS);
        } catch (CognitoIdentityProviderException exception) {
            LOGGER.error("cognito createAndConfirm failed for {}: {}", login, exception.awsErrorDetails().errorMessage(), exception);
            throw exception;
        }
    }

    public void deleteUser(String login) {
        AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
            .userPoolId(poolId)
            .username(login)
            .build();
        cognitoIdentityProviderClient.adminDeleteUser(request);
    }

    public void adminUpdatePassword(String login, String password) {
        AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest
            .builder()
            .userPoolId(poolId)
            .username(login)
            .password(password)
            .permanent(true)
            .build();
        cognitoIdentityProviderClient.adminSetUserPassword(request);
    }

    public void adminConfirmAccount(String login) {
        AdminConfirmSignUpRequest request = AdminConfirmSignUpRequest.builder()
            .userPoolId(poolId)
            .username(login)
            .build();
        cognitoIdentityProviderClient.adminConfirmSignUp(request);
    }

    public CognitoAuthResult signIn(SignInRequest signInRequest) {
        try {
            Map<String, String> authParams = new LinkedHashMap<>() {{
                put(ATTR_USERNAME, signInRequest.username());
                put(ATTR_PASSWORD, signInRequest.password());
                put(ATTR_SECRET_HASH, calculateSecretHash(signInRequest.username()));
            }};

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .userPoolId(poolId)
                .clientId(clientId)
                .authParameters(authParams)
                .build();
            AdminInitiateAuthResponse authResult = cognitoIdentityProviderClient.adminInitiateAuth(authRequest);

            AuthenticationResultType resultType = authResult.authenticationResult();

            return new CognitoAuthResult(resultType.accessToken(), resultType.refreshToken());
        } catch (NotAuthorizedException | InvalidParameterException exception) {
            LOGGER.error("signin failed for {}: {}", signInRequest.username(), exception.awsErrorDetails().errorMessage(), exception);
            throw new KerflowException(INVALID_USER_OR_PASSWORD);
        }
    }

    public CognitoAuthResult refreshToken(String username, String refreshToken) {
        try {
            Map<String, String> authParams = new LinkedHashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);
            authParams.put(ATTR_SECRET_HASH, calculateSecretHash(username));

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .userPoolId(poolId)
                .clientId(clientId)
                .authParameters(authParams)
                .build();
            AdminInitiateAuthResponse authResult = cognitoIdentityProviderClient.adminInitiateAuth(authRequest);

            AuthenticationResultType resultType = authResult.authenticationResult();

            return new CognitoAuthResult(resultType.accessToken(), refreshToken);
        } catch (NotAuthorizedException | InvalidParameterException exception) {
            LOGGER.error("refresh token failed for {}: {}", username, exception.awsErrorDetails().errorMessage(), exception);
            throw new KerflowException(INVALID_USER_OR_PASSWORD);
        }
    }

    public AdminGetUserResponse getUser(String cognitoUsername) {
        AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder()
            .userPoolId(poolId)
            .username(cognitoUsername)
            .build();
        return cognitoIdentityProviderClient.adminGetUser(adminGetUserRequest);
    }

    public String calculateSecretHash(String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
            clientSecret.getBytes(StandardCharsets.UTF_8),
            HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }
}
