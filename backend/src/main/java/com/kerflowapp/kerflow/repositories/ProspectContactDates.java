package com.kerflowapp.kerflow.repositories;

import java.time.Instant;
import java.util.UUID;

/**
 * Latest exchange dates of a prospect's thread: the last outbound message actually sent (drafts
 * have no sentAt) and the last reply received.
 */
public record ProspectContactDates(UUID prospectId, Instant lastSentAt, Instant lastReceivedAt) {
}
