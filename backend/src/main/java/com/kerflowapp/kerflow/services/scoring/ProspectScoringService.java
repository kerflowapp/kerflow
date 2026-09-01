package com.kerflowapp.kerflow.services.scoring;

import com.kerflowapp.kerflow.api.prospects.domain.SignalDto;
import com.kerflowapp.kerflow.api.search.domain.SearchResultDto;
import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Explainable score engine (roadmap step 1.1): consumes the detected signals
 * and their weights, never raw criteria. Serves both the web API and the MCP tools.
 */
@Service
public class ProspectScoringService {

    private static final int BASE_SCORE = 50;
    private static final int POINTS_SCALE = 5;

    /**
     * Computes the explainable score from persisted signals.
     * Returns null when there is no signal yet (never enriched): no fake neutral score.
     */
    public ProspectScore computeScore(List<ProspectSignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }
        return fromReasons(signals.stream()
            .map(s -> reason(s.type(), s.importance(), s.params()))
            .toList());
    }

    public ProspectScore computeScoreFromDtos(List<SignalDto> signals) {
        if (signals == null || signals.isEmpty()) {
            return null;
        }
        return fromReasons(signals.stream()
            .map(s -> reason(s.type(), s.importance(), s.params()))
            .toList());
    }

    public List<SearchResultDto> scoreAndSort(List<SearchResultDto> results) {
        Comparator<SearchResultDto> byScoreDesc = Comparator
            .comparing(SearchResultDto::score, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(SearchResultDto::name, Comparator.nullsLast(String::compareToIgnoreCase));

        return results.stream()
            .map(r -> {
                ProspectScore score = computeScoreFromDtos(r.signals());
                return r.toBuilder()
                    .score(score != null ? score.score() : null)
                    .scoreDetails(score)
                    .build();
            })
            .sorted(byScoreDesc)
            .toList();
    }

    private static ScoreReason reason(SignalType type, SignalImportance importance, Map<String, Object> params) {
        return new ScoreReason(type, importance, importance.getWeight() * type.getPolarity(), params);
    }

    private static ProspectScore fromReasons(List<ScoreReason> allReasons) {
        List<ScoreReason> reasons = allReasons.stream()
            .filter(r -> r.points() != 0)
            .sorted(Comparator.comparingInt((ScoreReason r) -> Math.abs(r.points())).reversed())
            .toList();

        int sum = reasons.stream().mapToInt(ScoreReason::points).sum();
        int score = Math.clamp(BASE_SCORE + POINTS_SCALE * sum, 0, 100);
        int stars = Math.clamp((int) Math.round(score / 25.0) + 1, 1, 5);
        return new ProspectScore(score, stars, reasons);
    }
}
