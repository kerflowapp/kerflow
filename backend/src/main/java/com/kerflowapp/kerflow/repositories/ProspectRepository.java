package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProspectRepository extends JpaRepository<Prospect, UUID> {

    List<Prospect> findAllByUserIdOrderByCreationDateDesc(UUID userId);

    List<Prospect> findAllByUserIdAndStatusOrderByCreationDateDesc(UUID userId, KanbanStatus status);

    Optional<Prospect> findByIdAndUserId(UUID id, UUID userId);

    List<Prospect> findAllByUserIdAndStatusKeyOrderByPositionAsc(UUID userId, String statusKey);

    long countByUserIdAndStatusKey(UUID userId, String statusKey);

    @Query("select min(p.position) from Prospect p where p.user.id = :userId and p.statusKey = :statusKey")
    Integer findMinPosition(@Param("userId") UUID userId, @Param("statusKey") String statusKey);

}
