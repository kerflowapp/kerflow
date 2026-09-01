package com.kerflowapp.kerflow.repositories;

import com.kerflowapp.kerflow.domain.ProspectMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProspectMessageRepository extends JpaRepository<ProspectMessage, UUID> {

    List<ProspectMessage> findAllByProspectIdOrderByCreationDateAsc(UUID prospectId);

    Optional<ProspectMessage> findByIdAndProspectId(UUID id, UUID prospectId);

    long countByProspectId(UUID prospectId);

    @Transactional
    void deleteAllByProspectId(UUID prospectId);

}
