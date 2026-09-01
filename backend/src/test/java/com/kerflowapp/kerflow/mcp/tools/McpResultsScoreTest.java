package com.kerflowapp.kerflow.mcp.tools;

import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.mcp.tools.McpResults.Score;
import com.kerflowapp.kerflow.services.scoring.ProspectScore;
import com.kerflowapp.kerflow.services.scoring.ScoreReason;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class McpResultsScoreTest {

    @Test
    void fromNullScoreIsNull() {
        assertThat(Score.from(null)).isNull();
    }

    @Test
    void fromMapsScoreStarsAndSlimReasons() {
        ProspectScore prospectScore = new ProspectScore(65, 4, List.of(
            new ScoreReason(SignalType.FLEET_DETECTED, SignalImportance.HIGH, 3, null),
            new ScoreReason(SignalType.NO_HTTPS, SignalImportance.MEDIUM, -2, null)
        ));

        Score score = Score.from(prospectScore);

        assertThat(score.score()).isEqualTo(65);
        assertThat(score.stars()).isEqualTo(4);
        assertThat(score.reasons()).containsExactly(
            new Score.Reason("FLEET_DETECTED", 3),
            new Score.Reason("NO_HTTPS", -2));
    }
}
