package com.kerflowapp.kerflow.mappers;

import com.kerflowapp.kerflow.api.prospects.domain.ProspectDto;
import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectSignal;
import com.kerflowapp.kerflow.domain.enums.SignalImportance;
import com.kerflowapp.kerflow.domain.enums.SignalType;
import com.kerflowapp.kerflow.services.scoring.ProspectScoringService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectMapperTest {

    private final ProspectMapper mapper = new ProspectMapperImpl();

    ProspectMapperTest() {
        mapper.prospectScoringService = new ProspectScoringService();
    }

    @Test
    void toDtoComputesScoreFromSignals() {
        Prospect prospect = Prospect.builder()
            .name("Taxi Breizh")
            .signals(List.of(
                ProspectSignal.of(SignalType.FLEET_DETECTED, SignalImportance.HIGH, "website", null),
                ProspectSignal.of(SignalType.NO_HTTPS, SignalImportance.MEDIUM, "website", null)
            ))
            .build();

        ProspectDto dto = mapper.toDto(prospect);

        // 50 + 5 * (3 - 2) = 55
        assertThat(dto.score()).isNotNull();
        assertThat(dto.score().score()).isEqualTo(55);
        assertThat(dto.score().stars()).isEqualTo(3);
        assertThat(dto.score().reasons()).hasSize(2);
        assertThat(dto.score().reasons().getFirst().type()).isEqualTo(SignalType.FLEET_DETECTED);
        assertThat(dto.score().reasons().getFirst().points()).isEqualTo(3);
    }

    @Test
    void toDtoLeavesScoreNullWhenNoSignals() {
        Prospect prospect = Prospect.builder().name("Empty").build();

        ProspectDto dto = mapper.toDto(prospect);

        assertThat(dto.score()).isNull();
    }
}
