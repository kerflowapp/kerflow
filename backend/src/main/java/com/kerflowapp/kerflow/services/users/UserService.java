package com.kerflowapp.kerflow.services.users;

import com.kerflowapp.kerflow.api.authentication.domain.SignupRequest;
import com.kerflowapp.kerflow.api.users.domain.UpdateUserRequest;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.repositories.UserRepository;
import com.kerflowapp.kerflow.services.CognitoService;
import com.kerflowapp.kerflow.services.DiscordService;
import com.kerflowapp.kerflow.services.mail.IResendService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static com.kerflowapp.kerflow.domain.enums.UserStatus.*;
import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long OTP_VALIDITY_MINUTES = 15;
    private static final long TRIAL_DURATION_DAYS = 3;

    private final UserRepository userRepository;
    private final CognitoService cognitoService;
    private final IResendService resendService;
    private final DiscordService discordService;

    public void createPendingUser(SignupRequest request) {
        String login = request.login().toLowerCase().trim();
        LOGGER.info("start creating cognito user for {}", login);

        Optional<User> existing = userRepository.findByLogin(login);
        existing.ifPresent(user -> {
            if (CONFIRMED.equals(user.getStatus())) {
                throw new KerflowException(USER_ALREADY_EXISTS);
            }
        });

        try {
            cognitoService.createCognitoUser(login, request.password());
        } catch (KerflowException e) {
            if (e.getCode() != USER_ALREADY_EXISTS) {
                throw e;
            }
            // Cognito user already exists. If it is already confirmed it is a genuine
            // duplicate -> block. Otherwise the user never completed OTP: delete and
            // recreate so Cognito stores the new password the user just typed.
            if (isCognitoUserConfirmed(login)) {
                throw new KerflowException(USER_ALREADY_EXISTS);
            }
            cognitoService.deleteUser(login);
            cognitoService.createCognitoUser(login, request.password());
        }
        LOGGER.info("done creating cognito user for {}", login);

        String otpCode = generateOtpCode();
        Instant otpExpiresAt = Instant.now().plus(OTP_VALIDITY_MINUTES, ChronoUnit.MINUTES);

        User pendingUser = existing
            .map(user -> user.toBuilder()
                .confirmationCode(UUID.randomUUID())
                .verificationCode(otpCode)
                .verificationCodeExpiresAt(otpExpiresAt)
                .status(PENDING_SIGNUP_CONFIRMATION)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .trialEndsAt(Instant.now().plus(TRIAL_DURATION_DAYS, ChronoUnit.DAYS))
                .build())
            .orElseGet(() -> User.builder()
                .confirmationCode(UUID.randomUUID())
                .verificationCode(otpCode)
                .verificationCodeExpiresAt(otpExpiresAt)
                .status(PENDING_SIGNUP_CONFIRMATION)
                .login(login)
                .email(login)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .trialEndsAt(Instant.now().plus(TRIAL_DURATION_DAYS, ChronoUnit.DAYS))
                .build());

        userRepository.save(pendingUser);

        resendService.sendOtpEmail(login, otpCode);

        discordService.sendRegistrationNotification(
            "Nouvelle inscription",
            String.format("**%s %s** (%s)",
                request.firstName(), request.lastName(), login));
    }

    private boolean isCognitoUserConfirmed(String login) {
        try {
            return software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType.CONFIRMED
                .equals(cognitoService.getUser(login).userStatus());
        } catch (Exception e) {
            LOGGER.warn("could not read cognito status for {}: {}", login, e.getMessage());
            return false;
        }
    }

    public void confirmUser(String rawLogin, UUID code) {
        String login = rawLogin.toLowerCase().trim();
        userRepository.findByLogin(login).ifPresentOrElse(
            user -> {
                if (code.equals(user.getConfirmationCode()) && PENDING_SIGNUP_CONFIRMATION.equals(user.getStatus())) {
                    cognitoService.adminConfirmAccount(login);
                    userRepository.save(user.withStatus(CONFIRMED));

                    discordService.sendRegistrationNotification(
                        "Compte confirme",
                        String.format("**%s %s** (%s) a confirme son compte.",
                            user.getFirstName(), user.getLastName(), login));
                } else {
                    throw new KerflowException(VERIFICATION_CODE_EXPIRED);
                }
            },
            () -> {
                throw new KerflowException(USER_NOT_FOUND);
            }
        );
    }

    public boolean checkEmailExists(@NotEmpty String email) {
        return userRepository.findByLogin(email.toLowerCase().trim()).isPresent();
    }

    public boolean isPendingSignupConfirmation(@NotEmpty String email) {
        return userRepository.findByLogin(email.toLowerCase().trim())
            .map(user -> PENDING_SIGNUP_CONFIRMATION.equals(user.getStatus()))
            .orElse(false);
    }

    @Transactional
    public void verifyOtp(@NotEmpty String email, @NotEmpty String verificationCode) {
        User user = userRepository.findByLogin(email.toLowerCase().trim())
            .orElseThrow(() -> new KerflowException(USER_NOT_FOUND));

        if (!PENDING_SIGNUP_CONFIRMATION.equals(user.getStatus())) {
            throw new KerflowException(USER_ALREADY_CONFIRMED);
        }
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(verificationCode)) {
            throw new KerflowException(INVALID_VERIFICATION_CODE);
        }
        if (user.getVerificationCodeExpiresAt() == null
            || user.getVerificationCodeExpiresAt().isBefore(Instant.now())) {
            throw new KerflowException(VERIFICATION_CODE_EXPIRED);
        }

        cognitoService.adminConfirmAccount(user.getLogin());
        user.setStatus(CONFIRMED);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    @Transactional
    public void resendOtp(String login) {
        User user = userRepository.findByLogin(login.toLowerCase().trim())
            .orElseThrow(() -> new KerflowException(USER_NOT_FOUND));

        if (!PENDING_SIGNUP_CONFIRMATION.equals(user.getStatus())) {
            throw new KerflowException(USER_ALREADY_CONFIRMED);
        }

        String otp = generateOtpCode();
        user.setVerificationCode(otp);
        user.setVerificationCodeExpiresAt(Instant.now().plus(OTP_VALIDITY_MINUTES, ChronoUnit.MINUTES));
        userRepository.save(user);

        resendService.sendOtpEmail(user.getLogin(), otp);
    }

    public Optional<User> getUserFromLogin(String login) {
        return userRepository.findByLogin(login.toLowerCase().trim());
    }

    public void initiateForgotPassword(@NotEmpty String rawLogin) {
        String login = rawLogin.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByLogin(login);

        userOpt.ifPresent(
            user -> {
                UUID confirmationCode = UUID.randomUUID();
                user.setStatus(PENDING_FORGOT_PASSWORD_CONFIRMATION);
                user.setConfirmationCode(confirmationCode);
                userRepository.save(user);
                resendService.sendResetPasswordEmail(login, confirmationCode);
            }
        );
    }

    public void updateUser(User loggedUser, @Valid UpdateUserRequest request) {
        userRepository.findByLogin(loggedUser.getLogin()).ifPresentOrElse(
            user -> {
                user.setFirstName(request.firstName());
                user.setLastName(request.lastName());
                user.setPhoneNumber(request.phoneNumber());
                user.setProfession(request.profession());
                userRepository.save(user);
            },
            () -> {
                throw new KerflowException(USER_NOT_FOUND);
            }
        );
    }

    public void confirmResetPassword(String rawLogin, String password, UUID code) {
        String login = rawLogin.toLowerCase().trim();
        userRepository.findByLogin(login).ifPresentOrElse(
            user -> {
                if (code.equals(user.getConfirmationCode())
                    && PENDING_FORGOT_PASSWORD_CONFIRMATION.equals(user.getStatus())) {

                    cognitoService.adminUpdatePassword(user.getLogin(), password);
                    userRepository.save(user.withStatus(CONFIRMED));
                } else {
                    throw new KerflowException(VERIFICATION_CODE_EXPIRED);
                }
            },
            () -> {
                throw new KerflowException(USER_NOT_FOUND);
            }
        );
    }

    private String generateOtpCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}

