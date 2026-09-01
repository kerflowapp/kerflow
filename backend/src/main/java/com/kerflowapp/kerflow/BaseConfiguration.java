package com.kerflowapp.kerflow;

import com.kerflowapp.kerflow.configuration.AWSConfiguration;
import com.kerflowapp.kerflow.configuration.security.SecurityConfig;
import com.kerflowapp.kerflow.configuration.security.WebConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({
    SecurityConfig.class,
    WebConfig.class,
    AWSConfiguration.class
})
@ComponentScan
@EnableConfigurationProperties({BaseConfiguration.CommonsProperties.class})
@EnableAutoConfiguration
public class BaseConfiguration {

    public static final String CFG_PREFIX = "commons";
    @Autowired
    private CommonsProperties commonsProperties;

    public String getWebUrl() {
        return commonsProperties.webUrl;
    }

    public String getBackendUrl() {
        return commonsProperties.backendUrl;
    }

    public String getAccountConfirmationUrl() {
        return commonsProperties.accountConfirmationUrl;
    }

    public String getResetPasswordConfirmationUrl() {
        return commonsProperties.resetPasswordConfirmationUrl;
    }

    public String getSignupConfirmationSucceedUrl() {
        return commonsProperties.signupConfirmationSucceedUrl;
    }

    @ConfigurationProperties(prefix = CFG_PREFIX)
    public record CommonsProperties(
        String webUrl,
        String backendUrl,
        String accountConfirmationUrl,
        String resetPasswordConfirmationUrl,
        String signupConfirmationSucceedUrl,
        String fromMail,
        /** Postal address printed in the email footer. Empty hides the line. */
        String postalAddress,
        DiscordProperties discord
    ) {

    }

    public record DiscordProperties(
        Boolean enabled,
        String botToken,
        String guildId,
        String channelPrefix
    ) {

    }
}
