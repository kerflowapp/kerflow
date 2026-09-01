package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, UUID> {

    @Query("select c from OAuthAuthorizationCode c join fetch c.user join fetch c.client where c.codeHash = :codeHash")
    Optional<OAuthAuthorizationCode> findByCodeHash(@Param("codeHash") String codeHash);

    @Modifying
    @Query("delete from OAuthAuthorizationCode c where c.expiresAt < :before")
    int deleteExpired(@Param("before") Instant before);

}
