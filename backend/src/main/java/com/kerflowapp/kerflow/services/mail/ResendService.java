package com.kerflowapp.kerflow.services.mail;

import com.kerflowapp.kerflow.BaseConfiguration;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.EMAIL_SENDING;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@ConditionalOnProperty(name = "resend.enabled", havingValue = "true")
public class ResendService implements IResendService {

    private static final String ACCOUNT_CONFIRMATION_EMAIL_TEMPLATE = "account-confirmation-v2.html";
    private static final String RESET_PASSWORD_CONFIRMATION_EMAIL_TEMPLATE = "reset-password-v2.html";
    private static final String OTP_VERIFICATION_EMAIL_TEMPLATE = "otp-verification.html";
    private final String fromMail;
    private final String accountConfirmationUrl;
    private final String resetPasswordConfirmationUrl;
    private final Resend resend;
    private final TemplateEngine emailsTemplateEngine;
    private final String postalAddress;

    public ResendService(BaseConfiguration.CommonsProperties commonsProperties,
                         @Qualifier("emailsTemplateEngine") TemplateEngine emailsTemplateEngine,
                         @Value("${resend.api-key}") String resendApiKey) {

        this.emailsTemplateEngine = emailsTemplateEngine;
        this.resend = new Resend(resendApiKey);
        this.accountConfirmationUrl = commonsProperties.accountConfirmationUrl();
        this.resetPasswordConfirmationUrl = commonsProperties.resetPasswordConfirmationUrl();
        this.fromMail = commonsProperties.fromMail();
        this.postalAddress = commonsProperties.postalAddress();
    }

    /**
     * Context pre-filled with the footer variables shared by every template.
     */
    private Context newContext() {
        final Context ctx = new Context();
        ctx.setVariable("year", java.time.Year.now().getValue());
        ctx.setVariable("postal_address", postalAddress == null ? "" : postalAddress);
        ctx.setVariable("support_email", "hello@kerflowapp.com");
        return ctx;
    }

    public void sendAccountConfirmationEmail(String login, UUID confirmationCode) {
        final Context ctx = newContext();
        ctx.setVariable("confirmation_link",
            accountConfirmationUrl + "?login=" + encode(login, UTF_8) + "&code=" + confirmationCode);
        final String htmlContent = this.emailsTemplateEngine.process(ACCOUNT_CONFIRMATION_EMAIL_TEMPLATE, ctx);

        CreateEmailOptions options = CreateEmailOptions
            .builder()
            .from(fromMail)
            .to(login)
            .subject("Confirmez votre inscription")
            .html(htmlContent)
            .attachments(createLogoAttachment())
            .build();
        try {
            CreateEmailResponse data = resend.emails().send(options);
        } catch (ResendException e) {
            LOGGER.error("error while sending email", e);
            throw new KerflowException(EMAIL_SENDING);
        }
    }

    public void sendResetPasswordEmail(String login, UUID confirmationCode) {
        final Context ctx = newContext();
        ctx.setVariable("confirmation_link",
            resetPasswordConfirmationUrl + "?login=" + encode(login, UTF_8) + "&code=" + confirmationCode);
        final String htmlContent = this.emailsTemplateEngine.process(RESET_PASSWORD_CONFIRMATION_EMAIL_TEMPLATE, ctx);

        CreateEmailOptions options = CreateEmailOptions
            .builder()
            .from(fromMail)
            .to(login)
            .subject("Réinitialisez votre mot de passe")
            .html(htmlContent)
            .attachments(createLogoAttachment())
            .build();
        try {
            resend.emails().send(options);
        } catch (ResendException e) {
            LOGGER.error("error while sending email", e);
            throw new KerflowException(EMAIL_SENDING);
        }
    }

    public void sendOtpEmail(String login, String otpCode) {
        final Context ctx = newContext();
        ctx.setVariable("otp_code", otpCode);
        final String htmlContent = this.emailsTemplateEngine.process(OTP_VERIFICATION_EMAIL_TEMPLATE, ctx);

        CreateEmailOptions options = CreateEmailOptions
            .builder()
            .from(fromMail)
            .to(login)
            .subject("Votre code de vérification Kerflow")
            .html(htmlContent)
            .attachments(createLogoAttachment())
            .build();
        try {
            resend.emails().send(options);
        } catch (ResendException e) {
            LOGGER.error("error while sending OTP email", e);
            throw new KerflowException(EMAIL_SENDING);
        }
    }

    private Attachment createLogoAttachment() {
        try {
            ClassPathResource logoResource = new ClassPathResource("static/logo.png");
            byte[] logoBytes = logoResource.getInputStream().readAllBytes();
            String base64Logo = Base64.getEncoder().encodeToString(logoBytes);

            return Attachment.builder()
                .fileName("logo.png")
                .content(base64Logo)
                .contentId("kerflow-logo")
                .build();

        } catch (IOException e) {
            LOGGER.error("Failed to load logo for email attachment", e);
            throw new KerflowException(EMAIL_SENDING);
        }
    }
}
