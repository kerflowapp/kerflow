package com.kerflowapp.kerflow.configuration;

import com.stripe.StripeClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeConfiguration.StripeProperties.class)
public class StripeConfiguration {

    @Bean
    public StripeClient stripeClient(StripeProperties stripeProperties) {
        return new StripeClient(stripeProperties.apiKey());
    }

    @ConfigurationProperties(prefix = "stripe")
    public record StripeProperties(
        String apiKey,
        String priceId,
        String priceIdAnnual
    ) {

    }
}
