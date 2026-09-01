package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.ApiToken;
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
public interface ApiTokenRepository extends JpaRepository<ApiToken, UUID> {

    @Query("""
        select t from ApiToken t join fetch t.user
        where t.tokenHash = :tokenHash
          and t.revokedAt is null
          and (t.expiresAt is null or t.expiresAt > :now)
        """)
    Optional<ApiToken> findActiveByTokenHash(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    /**
     * Personal tokens only: OAuth-issued ones are an implementation detail of a connector.
     */
    @Query("""
        select t from ApiToken t
        where t.user.id = :userId and t.client is null
        order by t.creationDate desc
        """)
    List<ApiToken> findPersonalByUserId(@Param("userId") UUID userId);

    Optional<ApiToken> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("""
        update ApiToken t set t.revokedAt = :now
        where t.user.id = :userId and t.client.id = :clientId and t.revokedAt is null
        """)
    int revokeByUserAndClient(@Param("userId") UUID userId,
                              @Param("clientId") UUID clientId,
                              @Param("now") Instant now);

    @Modifying
    @Query("delete from ApiToken t where t.client is not null and t.expiresAt < :before")
    int deleteExpiredOAuthTokens(@Param("before") Instant before);

}
