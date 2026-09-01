package com.kerflowapp.kerflow.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AWSConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AWSConfiguration.class);

    /**
     * Placeholder used when no credentials are supplied, so the context can still start.
     */
    private static final String UNCONFIGURED = "unconfigured";

    @Bean
    public CognitoIdentityProviderClient awsCognitoIdentityProvider(@Value("${aws.region}") Region region,
                                                                    AwsBasicCredentials amazonAWSCredentials) {

        return CognitoIdentityProviderClient.builder()
            .region(region)
            .credentialsProvider(() -> amazonAWSCredentials)
            .build();
    }

    @Bean
    public AwsBasicCredentials amazonAWSCredentials(@Value("${aws.access-key:}") String accessKey,
                                                    @Value("${aws.secret-key:}") String secretKey) {

        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            LOGGER.warn("AWS credentials are not configured: aws.access-key / aws.secret-key are blank. "
                + "The context still starts, but every Cognito call will be rejected. "
                + "Set AWS_ACCESS_KEY_ID and AWS_ACCESS_KEY_SECRET to enable authentication.");
            return AwsBasicCredentials.builder()
                .accessKeyId(UNCONFIGURED)
                .secretAccessKey(UNCONFIGURED)
                .build();
        }

        return AwsBasicCredentials.builder().accessKeyId(accessKey).secretAccessKey(secretKey).build();
    }
}
