package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mappers.ProspectMapper;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProspectServiceStatusTest {

    private static final Instant MOVED_AT = Instant.parse("2026-09-01T10:00:00Z");

    private ProspectRepository prospectRepository;
    private ProspectService service;
    private User user;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        prospectRepository = mock(ProspectRepository.class);
        service = new ProspectService(prospectRepository, mock(ProspectMessageRepository.class),
            mock(ProspectMapper.class), mock(EnrichmentEngine.class));

        user = new User();
        user.setId(UUID.randomUUID());
        prospect = Prospect.builder()
            .id(UUID.randomUUID())
            .statusKey("CONTACTED")
            .statusChangedAt(MOVED_AT)
            .build();
        when(prospectRepository.findByIdAndUserId(prospect.getId(), user.getId())).thenReturn(Optional.of(prospect));
        when(prospectRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void changingColumnRecordsWhenTheCardMoved() {
        service.updateStatus(user, prospect.getId(), "IN_DISCUSSION");

        assertThat(prospect.getStatusKey()).isEqualTo("IN_DISCUSSION");
        assertThat(prospect.getStatusChangedAt()).isAfter(MOVED_AT);
    }

    @Test
    void settingTheSameColumnKeepsTheCountdown() {
        service.updateStatus(user, prospect.getId(), "CONTACTED");

        assertThat(prospect.getStatusChangedAt()).isEqualTo(MOVED_AT);
    }

    @Test
    void reorderingInsideTheColumnKeepsTheCountdown() {
        service.reorder(user, "CONTACTED", List.of(prospect.getId()));

        assertThat(prospect.getPosition()).isZero();
        assertThat(prospect.getStatusChangedAt()).isEqualTo(MOVED_AT);
    }

    @Test
    void draggingToAnotherColumnRestartsTheCountdown() {
        service.reorder(user, "IN_DISCUSSION", List.of(prospect.getId()));

        assertThat(prospect.getStatusChangedAt()).isAfter(MOVED_AT);
    }
}
