package com.kerflowapp.kerflow.oauth;

import com.kerflowapp.kerflow.repositories.ApiTokenRepository;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationCodeRepository;
import com.kerflowapp.kerflow.repositories.OAuthAuthorizationRequestRepository;
import com.kerflowapp.kerflow.repositories.OAuthRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Authorization requests and codes are short-lived and created on every connection
 * attempt; without a sweep the tables grow forever. Expired rows are kept for a grace
 * period so a failing flow can still be diagnosed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthCleanupJob {

    private static final Duration GRACE = Duration.ofDays(1);

    private final OAuthAuthorizationRequestRepository authorizationRequestRepository;
    private final OAuthAuthorizationCodeRepository authorizationCodeRepository;
    private final OAuthRefreshTokenRepository refreshTokenRepository;
    private final ApiTokenRepository apiTokenRepository;

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeExpired() {
        Instant before = Instant.now().minus(GRACE);

        int requests = authorizationRequestRepository.deleteExpired(before);
        int codes = authorizationCodeRepository.deleteExpired(before);
        int accessTokens = apiTokenRepository.deleteExpiredOAuthTokens(before);
        int refreshTokens = refreshTokenRepository.deleteExpired(before);

        LOGGER.info("Purged expired OAuth rows: {} requests, {} codes, {} access tokens, {} refresh tokens",
            requests, codes, accessTokens, refreshTokens);
    }
    
}
