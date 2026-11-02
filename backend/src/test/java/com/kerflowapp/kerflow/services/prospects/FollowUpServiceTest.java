package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.repositories.ProspectContactDates;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-01T10:00:00Z");

    private ProspectRepository prospectRepository;
    private ProspectMessageRepository prospectMessageRepository;
    private FollowUpService service;
    private User user;

    @BeforeEach
    void setUp() {
        prospectRepository = mock(ProspectRepository.class);
        prospectMessageRepository = mock(ProspectMessageRepository.class);
        service = new FollowUpService(prospectRepository, prospectMessageRepository);

        user = new User();
        user.setId(UUID.randomUUID());
    }

    private static Instant daysAgo(int days) {
        return NOW.minus(days, ChronoUnit.DAYS);
    }

    private static Prospect prospect(String statusKey, Instant statusChangedAt) {
        return Prospect.builder()
            .id(UUID.randomUUID())
            .name("Boulangerie " + statusKey)
            .statusKey(statusKey)
            .statusChangedAt(statusChangedAt)
            .build();
    }

    private static ProspectContactDates dates(Prospect prospect, Instant lastSentAt, Instant lastReceivedAt) {
        return new ProspectContactDates(prospect.getId(), lastSentAt, lastReceivedAt);
    }

    @Test
    void closedOrNewColumnsNeverNeedAFollowUp() {
        for (String key : List.of("NEW", "WON", "LOST", "CUSTOM_abc")) {
            Prospect prospect = prospect(key, daysAgo(30));
            assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(30), null))).isNull();
        }
    }

    @Test
    void withoutMessagesTheCountdownStartsWhenTheCardEnteredItsColumn() {
        Prospect prospect = prospect("CONTACTED", daysAgo(4));

        assertThat(FollowUpService.followUpSince(prospect, null)).isEqualTo(daysAgo(4));
    }

    @Test
    void legacyRowsFallBackToTheirLastModificationDate() {
        Prospect prospect = prospect(null, null);
        prospect.setStatus(KanbanStatus.IN_DISCUSSION);
        LocalDateTime modified = LocalDateTime.of(2026, 9, 20, 12, 0);
        prospect.setLastModificationDate(modified);

        assertThat(FollowUpService.followUpSince(prospect, null))
            .isEqualTo(modified.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void theLastMessageSentStartsTheCountdown() {
        Prospect prospect = prospect("CONTACTED", daysAgo(10));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(3), null)))
            .isEqualTo(daysAgo(3));
    }

    @Test
    void aBackdatedMessageWinsOverAMoreRecentColumnChange() {
        Prospect prospect = prospect("CONTACTED", daysAgo(0));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(22), null)))
            .isEqualTo(daysAgo(22));
    }

    @Test
    void aReplyAfterTheLastMessageStopsTheCountdown() {
        Prospect prospect = prospect("CONTACTED", daysAgo(10));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(8), daysAgo(6)))).isNull();
    }

    @Test
    void aReplyOlderThanTheLastMessageDoesNotStopIt() {
        Prospect prospect = prospect("IN_DISCUSSION", daysAgo(10));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(2), daysAgo(6))))
            .isEqualTo(daysAgo(2));
    }

    @Test
    void aReplyOlderThanTheColumnChangeDoesNotStopItWhenNothingWasSent() {
        Prospect prospect = prospect("CONTACTED", daysAgo(1));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, null, daysAgo(6))))
            .isEqualTo(daysAgo(1));
    }

    @Test
    void delayDefaultsWhenTheUserNeverSetIt() {
        assertThat(FollowUpService.delayDays(user)).isEqualTo(FollowUpService.DEFAULT_DELAY_DAYS);

        user.setFollowUpDelayDays(12);
        assertThat(FollowUpService.delayDays(user)).isEqualTo(12);
    }

    @Test
    void dueFollowUpsKeepsOnlyOverdueProspectsMostOverdueFirst() {
        user.setFollowUpDelayDays(5);
        Prospect justDue = prospect("CONTACTED", daysAgo(5));
        Prospect veryLate = prospect("CONTACTED", daysAgo(20));
        Prospect notYet = prospect("CONTACTED", daysAgo(4));
        Prospect replied = prospect("IN_DISCUSSION", daysAgo(30));
        Prospect won = prospect("WON", daysAgo(30));
        when(prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId()))
            .thenReturn(List.of(justDue, veryLate, notYet, replied, won));
        when(prospectMessageRepository.findContactDates(any()))
            .thenReturn(List.of(dates(replied, daysAgo(30), daysAgo(29))));

        List<FollowUpService.FollowUp> due = service.dueFollowUps(user, NOW);

        assertThat(due).extracting(FollowUpService.FollowUp::prospect).containsExactly(veryLate, justDue);
        assertThat(due).extracting(FollowUpService.FollowUp::daysOverdue).containsExactly(15L, 0L);
        assertThat(due.getFirst().daysWaiting()).isEqualTo(20L);
    }

    private static LocalDate dayOffset(int days) {
        return NOW.atZone(ZoneId.systemDefault()).toLocalDate().plusDays(days);
    }

    private static Prospect prospectWithDate(String statusKey, LocalDate followUpDate) {
        Prospect prospect = prospect(statusKey, daysAgo(30));
        prospect.setFollowUpDate(followUpDate);
        return prospect;
    }

    @Test
    void aManualDateReplacesTheAutomaticCountdown() {
        Prospect prospect = prospectWithDate("CONTACTED", dayOffset(10));

        assertThat(FollowUpService.followUpSince(prospect, dates(prospect, daysAgo(30), null))).isNull();
    }

    @Test
    void clearingTheManualDateBringsTheAutomaticCountdownBack() {
        Prospect prospect = prospectWithDate("CONTACTED", dayOffset(10));

        prospect.setFollowUpDate(null);

        assertThat(FollowUpService.followUpSince(prospect, null)).isEqualTo(daysAgo(30));
    }

    @Test
    void aManualDateThatHasComeIsDueInAnyColumn() {
        for (String key : List.of("NEW", "WON", "LOST", "CUSTOM_abc", "CONTACTED")) {
            Prospect prospect = prospectWithDate(key, dayOffset(-3));
            when(prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId()))
                .thenReturn(List.of(prospect));

            List<FollowUpService.FollowUp> due = service.dueFollowUps(user, NOW);

            assertThat(due).hasSize(1);
            FollowUpService.FollowUp followUp = due.getFirst();
            assertThat(followUp.prospect()).isEqualTo(prospect);
            assertThat(followUp.followUpDate()).isEqualTo(dayOffset(-3));
            assertThat(followUp.waitingSince()).isNull();
            assertThat(followUp.daysWaiting()).isZero();
            assertThat(followUp.daysOverdue()).isEqualTo(3L);
        }
    }

    @Test
    void aManualDateIsDueOnItsOwnDayButNotBefore() {
        Prospect today = prospectWithDate("NEW", dayOffset(0));
        Prospect tomorrow = prospectWithDate("CONTACTED", dayOffset(1));
        when(prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId()))
            .thenReturn(List.of(today, tomorrow));

        List<FollowUpService.FollowUp> due = service.dueFollowUps(user, NOW);

        assertThat(due).extracting(FollowUpService.FollowUp::prospect).containsExactly(today);
        assertThat(due.getFirst().daysOverdue()).isZero();
    }

    @Test
    void manualAndAutomaticFollowUpsAreSortedTogether() {
        user.setFollowUpDelayDays(5);
        Prospect automatic = prospect("CONTACTED", daysAgo(10));
        Prospect veryLate = prospectWithDate("WON", dayOffset(-20));
        Prospect justDue = prospectWithDate("NEW", dayOffset(0));
        when(prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId()))
            .thenReturn(List.of(justDue, automatic, veryLate));

        List<FollowUpService.FollowUp> due = service.dueFollowUps(user, NOW);

        assertThat(due).extracting(FollowUpService.FollowUp::prospect).containsExactly(veryLate, automatic, justDue);
        assertThat(due).extracting(FollowUpService.FollowUp::daysOverdue).containsExactly(20L, 5L, 0L);
    }

    @Test
    void followUpSinceSkipsTheQueryForAnEmptyList() {
        assertThat(service.followUpSince(List.of())).isEmpty();
        verifyNoInteractions(prospectMessageRepository);
    }
}
