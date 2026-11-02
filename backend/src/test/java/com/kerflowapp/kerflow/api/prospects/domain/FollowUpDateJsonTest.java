package com.kerflowapp.kerflow.api.prospects.domain;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** The web app parses and sends this date as a plain yyyy-MM-dd string. */
class FollowUpDateJsonTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void theDtoWritesAPlainDay() {
        ProspectDto dto = ProspectDto.builder().followUpDate(LocalDate.of(2026, 11, 5)).build();

        assertThat(jsonMapper.readTree(jsonMapper.writeValueAsString(dto)).get("followUpDate").asString())
            .isEqualTo("2026-11-05");
    }

    @Test
    void theRequestReadsAPlainDay() {
        UpdateFollowUpDateRequest request =
            jsonMapper.readValue("{\"followUpDate\":\"2026-11-05\"}", UpdateFollowUpDateRequest.class);

        assertThat(request.followUpDate()).isEqualTo(LocalDate.of(2026, 11, 5));
    }

    @Test
    void theRequestReadsNullAsAClear() {
        UpdateFollowUpDateRequest request =
            jsonMapper.readValue("{\"followUpDate\":null}", UpdateFollowUpDateRequest.class);

        assertThat(request.followUpDate()).isNull();
    }
}
