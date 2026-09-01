package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.ProspectPipelineColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProspectPipelineColumnRepository extends JpaRepository<ProspectPipelineColumn, UUID> {

    List<ProspectPipelineColumn> findAllByUserIdOrderBySortOrderAsc(UUID userId);

    Optional<ProspectPipelineColumn> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndKey(UUID userId, String key);

}

