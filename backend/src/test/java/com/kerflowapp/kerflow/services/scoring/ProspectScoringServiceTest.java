package com.kerflowapp.kerflow.services.scoring;

import com.kerflowapp.kerflow.api.prospects.domain.SignalDto;
import com.kerflowapp.kerflow.api.search.domain.SearchResultDto;
import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectScoringServiceTest {

    private final ProspectScoringService service = new ProspectScoringService();

    private static ProspectSignal signal(SignalType type, SignalImportance importance) {
        return ProspectSignal.of(type, importance, "test", null);
    }

    @Test
    void emptyOrNullSignalsProduceNoScore() {
        assertThat(service.computeScore(null)).isNull();
        assertThat(service.computeScore(List.of())).isNull();
        assertThat(service.computeScoreFromDtos(null)).isNull();
        assertThat(service.computeScoreFromDtos(List.of())).isNull();
    }

    @Test
    void positiveSignalsRaiseScoreAboveBaseline() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.FLEET_DETECTED, SignalImportance.HIGH),
            signal(SignalType.PRO_EMAIL, SignalImportance.MEDIUM)
        ));

        // 50 + 5 * (3 + 2) = 75
        assertThat(score.score()).isEqualTo(75);
        assertThat(score.stars()).isEqualTo(4);
        assertThat(score.reasons()).hasSize(2);
    }

    @Test
    void negativeSignalsLowerScoreBelowBaseline() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.NO_WEBSITE, SignalImportance.HIGH),
            signal(SignalType.GENERIC_EMAIL, SignalImportance.LOW)
        ));

        // 50 + 5 * (-3 - 1) = 30
        assertThat(score.score()).isEqualTo(30);
        assertThat(score.stars()).isEqualTo(2);
    }

    @Test
    void scoreIsClampedTo100() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.FLEET_DETECTED, SignalImportance.HIGH),
            signal(SignalType.RECRUITING_DETECTED, SignalImportance.HIGH),
            signal(SignalType.MANY_REVIEWS, SignalImportance.HIGH),
            signal(SignalType.PRO_EMAIL, SignalImportance.MEDIUM)
        ));

        // raw = 50 + 5 * 11 = 105 -> clamped
        assertThat(score.score()).isEqualTo(100);
        assertThat(score.stars()).isEqualTo(5);
    }

    @Test
    void scoreIsClampedToZeroAndStarsToOne() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.NO_WEBSITE, SignalImportance.HIGH),
            signal(SignalType.NO_HTTPS, SignalImportance.MEDIUM),
            signal(SignalType.MOBILE_ONLY_PHONE, SignalImportance.MEDIUM),
            signal(SignalType.GENERIC_EMAIL, SignalImportance.LOW),
            signal(SignalType.NO_WEBSITE, SignalImportance.HIGH),
            signal(SignalType.NO_WEBSITE, SignalImportance.HIGH)
        ));

        // raw = 50 + 5 * -14 = -20 -> clamped
        assertThat(score.score()).isEqualTo(0);
        assertThat(score.stars()).isEqualTo(1);
    }

    @Test
    void neutralSignalsAreExcludedFromReasons() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.WORDPRESS, SignalImportance.LOW),
            signal(SignalType.FLEET_DETECTED, SignalImportance.HIGH)
        ));

        assertThat(score.reasons()).hasSize(1);
        assertThat(score.reasons().getFirst().type()).isEqualTo(SignalType.FLEET_DETECTED);
        assertThat(score.score()).isEqualTo(65);
    }

    @Test
    void reasonsAreSortedByAbsolutePointsDescending() {
        ProspectScore score = service.computeScore(List.of(
            signal(SignalType.GENERIC_EMAIL, SignalImportance.LOW),
            signal(SignalType.FLEET_DETECTED, SignalImportance.HIGH),
            signal(SignalType.NO_HTTPS, SignalImportance.MEDIUM)
        ));

        assertThat(score.reasons())
            .extracting(ScoreReason::points)
            .containsExactly(3, -2, -1);
    }

    @Test
    void reasonsCarrySignalParams() {
        ProspectScore score = service.computeScore(List.of(
            ProspectSignal.of(SignalType.MANY_REVIEWS, SignalImportance.HIGH, "google-places", Map.of("count", 250))
        ));

        assertThat(score.reasons().getFirst().params()).containsEntry("count", 250);
    }

    @Test
    void scoreAndSortOrdersByScoreDescendingWithUnscoredLast() {
        SearchResultDto strong = SearchResultDto.builder()
            .name("Strong")
            .signals(List.of(toDto(SignalType.FLEET_DETECTED, SignalImportance.HIGH)))
            .build();
        SearchResultDto weak = SearchResultDto.builder()
            .name("Weak")
            .signals(List.of(toDto(SignalType.NO_WEBSITE, SignalImportance.HIGH)))
            .build();
        SearchResultDto unscored = SearchResultDto.builder()
            .name("Unscored")
            .build();

        List<SearchResultDto> sorted = service.scoreAndSort(List.of(weak, unscored, strong));

        assertThat(sorted).extracting(SearchResultDto::name)
            .containsExactly("Strong", "Weak", "Unscored");
        assertThat(sorted.getFirst().score()).isEqualTo(65);
        assertThat(sorted.getFirst().scoreDetails().stars()).isEqualTo(4);
        assertThat(sorted.getLast().score()).isNull();
        assertThat(sorted.getLast().scoreDetails()).isNull();
    }

    private static SignalDto toDto(SignalType type, SignalImportance importance) {
        return SignalDto.builder()
            .type(type)
            .importance(importance)
            .source("test")
            .build();
    }
}
