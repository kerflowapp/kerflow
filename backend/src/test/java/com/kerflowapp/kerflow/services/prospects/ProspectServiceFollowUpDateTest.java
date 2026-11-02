package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.mappers.ProspectMapper;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProspectServiceFollowUpDateTest {

    private static final Instant MOVED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final LocalDate DATE = LocalDate.of(2026, 11, 5);

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
    void setsTheDateWithoutTouchingTheColumn() {
        Prospect saved = service.updateFollowUpDate(user, prospect.getId(), DATE);

        assertThat(saved.getFollowUpDate()).isEqualTo(DATE);
        assertThat(saved.getStatusKey()).isEqualTo("CONTACTED");
        assertThat(saved.getStatusChangedAt()).isEqualTo(MOVED_AT);
    }

    @Test
    void aNullDateClearsIt() {
        prospect.setFollowUpDate(DATE);

        Prospect saved = service.updateFollowUpDate(user, prospect.getId(), null);

        assertThat(saved.getFollowUpDate()).isNull();
    }

    @Test
    void movingTheCardKeepsTheDate() {
        prospect.setFollowUpDate(DATE);

        service.updateStatus(user, prospect.getId(), "WON");

        assertThat(prospect.getFollowUpDate()).isEqualTo(DATE);
    }

    @Test
    void refusesAProspectOfAnotherUser() {
        UUID unknownId = UUID.randomUUID();
        when(prospectRepository.findByIdAndUserId(unknownId, user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateFollowUpDate(user, unknownId, DATE))
            .isInstanceOf(KerflowException.class);
        verify(prospectRepository, never()).save(any());
    }
}
