package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, UUID> {

    @Query("select r from OAuthRefreshToken r join fetch r.user join fetch r.client where r.tokenHash = :tokenHash")
    Optional<OAuthRefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    @Query("""
        select r from OAuthRefreshToken r
        where r.user.id = :userId and r.client.id = :clientId
          and r.consumedAt is null and r.revokedAt is null
        """)
    List<OAuthRefreshToken> findActiveByUserAndClient(@Param("userId") UUID userId,
                                                      @Param("clientId") UUID clientId);

    @Modifying
    @Query("delete from OAuthRefreshToken r where r.expiresAt < :before")
    int deleteExpired(@Param("before") Instant before);

}
