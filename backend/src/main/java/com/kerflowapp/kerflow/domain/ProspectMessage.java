package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.domain.enums.MessageDirection;
import com.kerflowapp.kerflow.domain.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * One exchange of a prospect's outreach thread, whatever the support: an outbound email
 * (drafted here, sent by the user from their own mailbox), an inbound reply pasted back
 * in, or any interaction logged after the fact (a call, a LinkedIn message, a meeting).
 */
@Entity
@Table(name = "prospect_messages")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProspectMessage extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prospect_id", nullable = false)
    private Prospect prospect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageDirection direction;

    /**
     * Nullable in database only: rows written before channels existed have none.
     * Always set on write, and read as EMAIL when null.
     */
    @Enumerated(EnumType.STRING)
    private MessageChannel channel;

    /**
     * Outbound only: DRAFT until the user actually sends it and marks it sent.
     * Null for inbound messages.
     */
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    /**
     * "mcp-agent" when written by an MCP client; null when typed by the user.
     */
    private String generatedBy;

    private Instant sentAt;

    private Instant receivedAt;

}
