package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectMessage;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.MessageChannel;
import com.kerflowapp.kerflow.domain.enums.MessageDirection;
import com.kerflowapp.kerflow.domain.enums.MessageStatus;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ProspectMessageService {

    private final ProspectRepository prospectRepository;
    private final ProspectMessageRepository prospectMessageRepository;

    /**
     * Records any exchange of the thread. Defaults keep the "paste a reply" case a one-liner:
     * no direction means INBOUND, no channel means EMAIL, no date means now.
     * Status only exists on outbound messages; sentAt and receivedAt follow from it.
     */
    public ProspectMessage recordMessage(User user, UUID prospectId, MessageDirection direction,
                                         MessageChannel channel, MessageStatus status, String subject,
                                         String body, Instant occurredAt, String generatedBy) {
        Prospect prospect = getOwnedProspect(user, prospectId);

        ProspectMessage message = ProspectMessage.builder()
            .prospect(prospect)
            .generatedBy(generatedBy)
            .build();
        apply(message, direction, channel, status, subject, body, occurredAt == null ? Instant.now() : occurredAt);
        return prospectMessageRepository.save(message);
    }

    /**
     * Rewrites every editable field of an existing message, with the same defaults as {@link #recordMessage}.
     * No date keeps the message's current one. generatedBy is left untouched.
     */
    public ProspectMessage updateMessage(User user, UUID prospectId, UUID messageId, MessageDirection direction,
                                         MessageChannel channel, MessageStatus status, String subject,
                                         String body, Instant occurredAt) {
        ProspectMessage message = getOwnedMessage(user, prospectId, messageId);

        Instant when = occurredAt != null ? occurredAt
            : message.getSentAt() != null ? message.getSentAt()
            : message.getReceivedAt() != null ? message.getReceivedAt()
            : Instant.now();
        apply(message, direction, channel, status, subject, body, when);
        return prospectMessageRepository.save(message);
    }

    public ProspectMessage createOutboundDraft(User user, UUID prospectId, String subject, String body, String generatedBy) {
        return recordMessage(user, prospectId, MessageDirection.OUTBOUND, MessageChannel.EMAIL,
            MessageStatus.DRAFT, subject, body, null, generatedBy);
    }

    public ProspectMessage recordInbound(User user, UUID prospectId, String subject, String body, String generatedBy) {
        return recordMessage(user, prospectId, MessageDirection.INBOUND, MessageChannel.EMAIL,
            null, subject, body, null, generatedBy);
    }

    /**
     * Thread in the order the exchanges happened, not the order they were logged: a call recorded
     * today for last week sits last week. Drafts have no date yet and sit where they were written.
     */
    public List<ProspectMessage> listMessages(User user, UUID prospectId) {
        Prospect prospect = getOwnedProspect(user, prospectId);
        return prospectMessageRepository.findAllByProspectIdOrderByCreationDateAsc(prospect.getId()).stream()
            .sorted(Comparator.comparing(ProspectMessageService::occurredAt))
            .toList();
    }

    static Instant occurredAt(ProspectMessage message) {
        if (message.getSentAt() != null) return message.getSentAt();
        if (message.getReceivedAt() != null) return message.getReceivedAt();
        if (message.getCreationDate() != null) return message.getCreationDate().atZone(ZoneId.systemDefault()).toInstant();
        return Instant.MAX;
    }

    public long countMessages(UUID prospectId) {
        return prospectMessageRepository.countByProspectId(prospectId);
    }

    public ProspectMessage markSent(User user, UUID prospectId, UUID messageId) {
        ProspectMessage message = getOwnedMessage(user, prospectId, messageId);

        if (message.getDirection() != MessageDirection.OUTBOUND) {
            throw new KerflowException(MESSAGE_NOT_OUTBOUND);
        }
        message.setStatus(MessageStatus.SENT);
        message.setSentAt(Instant.now());
        return prospectMessageRepository.save(message);
    }

    public void deleteMessage(User user, UUID prospectId, UUID messageId) {
        ProspectMessage message = getOwnedMessage(user, prospectId, messageId);
        prospectMessageRepository.delete(message);
    }

    /**
     * Status only exists on outbound messages; sentAt and receivedAt follow from direction and status.
     */
    private void apply(ProspectMessage message, MessageDirection direction, MessageChannel channel,
                       MessageStatus status, String subject, String body, Instant when) {
        boolean outbound = direction == MessageDirection.OUTBOUND;
        MessageStatus finalStatus = outbound ? (status == null ? MessageStatus.DRAFT : status) : null;

        message.setDirection(outbound ? MessageDirection.OUTBOUND : MessageDirection.INBOUND);
        message.setChannel(channel == null ? MessageChannel.EMAIL : channel);
        message.setStatus(finalStatus);
        message.setSubject(subject);
        message.setBody(body);
        message.setSentAt(finalStatus == MessageStatus.SENT ? when : null);
        message.setReceivedAt(outbound ? null : when);
    }

    private Prospect getOwnedProspect(User user, UUID prospectId) {
        return prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));
    }

    private ProspectMessage getOwnedMessage(User user, UUID prospectId, UUID messageId) {
        Prospect prospect = getOwnedProspect(user, prospectId);
        return prospectMessageRepository.findByIdAndProspectId(messageId, prospect.getId())
            .orElseThrow(() -> new KerflowException(MESSAGE_NOT_FOUND));
    }
}
