package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.OAuthAuthorizationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAuthorizationRequestRepository extends JpaRepository<OAuthAuthorizationRequest, UUID> {

    @Query("select r from OAuthAuthorizationRequest r join fetch r.client where r.id = :id")
    Optional<OAuthAuthorizationRequest> findByIdWithClient(@Param("id") UUID id);

    @Modifying
    @Query("delete from OAuthAuthorizationRequest r where r.expiresAt < :before")
    int deleteExpired(@Param("before") Instant before);

}
