package com.kerflowapp.kerflow.services.mail;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
@ConditionalOnProperty(name = "resend.enabled", havingValue = "false")
public class DummyResendService implements IResendService {

    @PostConstruct
    public void init() {
        LOGGER.info("DummyResendService initialized. Resend is disabled.");
    }

    @Override
    public void sendAccountConfirmationEmail(String login, UUID confirmationCode) {
        LOGGER.info("Fake sending account confirmation email to '{}' with code '{}'", login, confirmationCode);
    }

    @Override
    public void sendResetPasswordEmail(String login, UUID confirmationCode) {
        LOGGER.info("Fake sending reset password email to '{}' with code '{}'", login, confirmationCode);
    }

    @Override
    public void sendOtpEmail(String login, String otpCode) {
        LOGGER.info("Fake sending OTP email to '{}' with code '{}'", login, otpCode);
    }

}
