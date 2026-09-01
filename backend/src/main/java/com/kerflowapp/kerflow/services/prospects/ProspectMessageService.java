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

        boolean outbound = direction == MessageDirection.OUTBOUND;
        MessageStatus finalStatus = outbound ? (status == null ? MessageStatus.DRAFT : status) : null;
        Instant when = occurredAt == null ? Instant.now() : occurredAt;

        ProspectMessage message = ProspectMessage.builder()
            .prospect(prospect)
            .direction(outbound ? MessageDirection.OUTBOUND : MessageDirection.INBOUND)
            .channel(channel == null ? MessageChannel.EMAIL : channel)
            .status(finalStatus)
            .subject(subject)
            .body(body)
            .generatedBy(generatedBy)
            .sentAt(finalStatus == MessageStatus.SENT ? when : null)
            .receivedAt(outbound ? null : when)
            .build();
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

    public List<ProspectMessage> listMessages(User user, UUID prospectId) {
        Prospect prospect = getOwnedProspect(user, prospectId);
        return prospectMessageRepository.findAllByProspectIdOrderByCreationDateAsc(prospect.getId());
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
