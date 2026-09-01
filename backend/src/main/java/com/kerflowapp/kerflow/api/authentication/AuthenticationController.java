package com.kerflowapp.kerflow.api.authentication;

import com.kerflowapp.kerflow.BaseConfiguration;
import com.kerflowapp.kerflow.api.authentication.domain.*;
import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.configuration.rest.authorization.NoAuthorizationRequired;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.mappers.UserMapper;
import com.kerflowapp.kerflow.repositories.UserRepository;
import com.kerflowapp.kerflow.services.CognitoAuthResult;
import com.kerflowapp.kerflow.services.CognitoService;
import com.kerflowapp.kerflow.services.SuperAdminEmails;
import com.kerflowapp.kerflow.services.billing.SubscriptionService;
import com.kerflowapp.kerflow.services.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

import static com.kerflowapp.kerflow.api.authentication.AuthenticationController.BASE_PATH;
import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.INVALID_ACTION;

@Slf4j
@RestController
@RequestMapping(BASE_PATH)
@RequiredArgsConstructor
public class AuthenticationController {

    public static final String BASE_PATH = "/v1/authentication";
    public static final String SIGNUP_PATH = "/signup";
    public static final String SIGNUP_CONFIRMATION_PATH = "/signup-confirmation";
    public static final String SIGNIN_PATH = "/signin";
    public static final String REFRESH_PATH = "/refresh";
    public static final String RESET_PASSWORD_PATH = "/password";
    public static final String CHECK_EMAIL_PATH = "/check-email";
    public static final String VERIFY_PATH = "/verify";
    public static final String RESEND_VERIFICATION_PATH = "/resend-verification";

    private final CognitoService cognitoService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final BaseConfiguration baseConfiguration;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SuperAdminEmails superAdminEmails;

    @PostMapping(SIGNUP_PATH)
    public ResponseEntity<Void> initAccountCreation(@RequestBody @Valid SignupRequest request) {
        LOGGER.info("start creating pending user");
        userService.createPendingUser(request);
        LOGGER.info("done creating pending user");
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(SIGNUP_CONFIRMATION_PATH)
    public RedirectView confirmAccountCreation(@RequestParam("login") String login, @RequestParam("code") UUID code) {
        LOGGER.info("start confirming pending user with login '{}' and code '{}'", login, code);
        try {
            userService.confirmUser(login, code);
        } catch (Exception e) {
            return new RedirectView(baseConfiguration.getWebUrl());
        }
        LOGGER.info("done confirming pending user");
        return new RedirectView(baseConfiguration.getSignupConfirmationSucceedUrl());
    }

    @PostMapping(SIGNIN_PATH)
    public ResponseEntity<AuthenticationResponseDto> authenticate(@RequestBody @Valid SignInRequest request) {
        LOGGER.info("start signing in user with login '{}'", request.username());
        CognitoAuthResult authResult = cognitoService.signIn(request);

        String impersonatedUserEmail = null;
        if (request.impersonateEmail() != null) {
            if (!superAdminEmails.contains(request.username())) {
                throw new KerflowException(KerflowException.ErrorCode.UNAUTHORIZED);
            }
            String targetEmail = request.impersonateEmail().toLowerCase().trim();
            userRepository.findByLogin(targetEmail)
                .orElseThrow(() -> new KerflowException(KerflowException.ErrorCode.USER_NOT_FOUND));
            impersonatedUserEmail = targetEmail;
            LOGGER.info("admin '{}' is impersonating user '{}'", request.username(), impersonatedUserEmail);
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(AuthenticationResponseDto.builder()
                .token(authResult.accessToken())
                .refreshToken(authResult.refreshToken())
                .impersonatedUserEmail(impersonatedUserEmail)
                .build());
    }

    @PostMapping(REFRESH_PATH)
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        LOGGER.info("refreshing token for user '{}'", request.username());
        CognitoAuthResult authResult = cognitoService.refreshToken(request.username(), request.refreshToken());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(AuthenticationResponseDto.builder()
                .token(authResult.accessToken())
                .refreshToken(authResult.refreshToken())
                .build());
    }

    @GetMapping("/user-context")
    public ResponseEntity<UserDto> getUserInformation(@CurrentLoggedUser User loggedUser) {
        subscriptionService.ensureTrialInitialized(loggedUser);
        subscriptionService.refreshIfStale(loggedUser);

        UserDto userDto = userMapper.toUserDto(loggedUser)
            .withSubscription(subscriptionService.toSubscriptionDto(loggedUser));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
    }

    @PostMapping(RESET_PASSWORD_PATH)
    public ResponseEntity<Void> initiateForgotPassword(@RequestBody @Valid UpdatePasswordRequest request) {
        if (UpdatePasswordActionType.RESET.equals(request.action())) {
            userService.initiateForgotPassword(request.email());
        } else if (UpdatePasswordActionType.CONFIRM.equals(request.action())) {
            userService.confirmResetPassword(request.email(), request.password(), request.confirmationCode());
        } else {
            throw new KerflowException(INVALID_ACTION);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(CHECK_EMAIL_PATH)
    public ResponseEntity<CheckEmailResponse> checkEmail(@RequestParam String email) {
        LOGGER.info("checking if email '{}' exists", email);
        boolean exists = userService.checkEmailExists(email);
        boolean pendingConfirmation = userService.isPendingSignupConfirmation(email);
        return ResponseEntity.ok(new CheckEmailResponse(exists, pendingConfirmation));
    }

    @PostMapping(VERIFY_PATH)
    public ResponseEntity<Void> verifyAccount(@RequestBody @Valid VerifyOtpRequest request) {
        LOGGER.info("verifying OTP for email '{}'", request.email());
        userService.verifyOtp(request.email(), request.verificationCode());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @NoAuthorizationRequired
    @PostMapping(RESEND_VERIFICATION_PATH)
    public ResponseEntity<Void> resendVerification(@RequestBody @Valid ResendVerificationRequest request) {
        LOGGER.info("resending verification code for '{}'", request.email());
        userService.resendOtp(request.email());
        return ResponseEntity.ok().build();
    }


}
