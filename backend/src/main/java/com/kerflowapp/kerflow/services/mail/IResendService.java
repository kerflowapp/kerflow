package com.kerflowapp.kerflow.services.mail;

import java.util.UUID;

public interface IResendService {

    void sendAccountConfirmationEmail(String login, UUID confirmationCode);

    void sendResetPasswordEmail(String login, UUID confirmationCode);

    void sendOtpEmail(String login, String otpCode);

}
