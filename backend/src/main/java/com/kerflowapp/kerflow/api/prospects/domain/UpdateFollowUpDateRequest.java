package com.kerflowapp.kerflow.api.prospects.domain;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

/** A null date clears the manual follow-up, and the automatic countdown takes over again. */
public record UpdateFollowUpDateRequest(
    @Nullable
    LocalDate followUpDate
) {
}
