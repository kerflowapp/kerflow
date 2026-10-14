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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProspectMessageServiceTest {

    private static final Instant ORIGINAL = Instant.parse("2026-09-01T12:00:00Z");
    private static final Instant EDITED = Instant.parse("2026-09-15T12:00:00Z");

    private ProspectRepository prospectRepository;
    private ProspectMessageRepository prospectMessageRepository;
    private ProspectMessageService service;
    private User user;
    private Prospect prospect;

    @BeforeEach
    void setUp() {
        prospectRepository = mock(ProspectRepository.class);
        prospectMessageRepository = mock(ProspectMessageRepository.class);
        service = new ProspectMessageService(prospectRepository, prospectMessageRepository);

        user = new User();
        user.setId(UUID.randomUUID());
        prospect = Prospect.builder().id(UUID.randomUUID()).name("Boulangerie").build();

        when(prospectRepository.findByIdAndUserId(prospect.getId(), user.getId())).thenReturn(Optional.of(prospect));
        when(prospectMessageRepository.save(any(ProspectMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ProspectMessage stored(ProspectMessage message) {
        message.setId(UUID.randomUUID());
        message.setProspect(prospect);
        when(prospectMessageRepository.findByIdAndProspectId(message.getId(), prospect.getId()))
            .thenReturn(Optional.of(message));
        return message;
    }

    private ProspectMessage inbound() {
        return stored(ProspectMessage.builder()
            .direction(MessageDirection.INBOUND)
            .channel(MessageChannel.EMAIL)
            .subject("Re: hello")
            .body("Thanks")
            .receivedAt(ORIGINAL)
            .build());
    }

    private ProspectMessage draft() {
        return stored(ProspectMessage.builder()
            .direction(MessageDirection.OUTBOUND)
            .channel(MessageChannel.EMAIL)
            .status(MessageStatus.DRAFT)
            .body("Hello")
            .generatedBy("mcp-agent")
            .build());
    }

    private ProspectMessage sent() {
        return stored(ProspectMessage.builder()
            .direction(MessageDirection.OUTBOUND)
            .channel(MessageChannel.EMAIL)
            .status(MessageStatus.SENT)
            .body("Hello")
            .sentAt(ORIGINAL)
            .build());
    }

    @Test
    void editingAReplyRewritesContentChannelAndDate() {
        ProspectMessage message = inbound();

        ProspectMessage updated = service.updateMessage(user, prospect.getId(), message.getId(),
            MessageDirection.INBOUND, MessageChannel.PHONE, null, null, "Called back", EDITED);

        assertThat(updated.getChannel()).isEqualTo(MessageChannel.PHONE);
        assertThat(updated.getBody()).isEqualTo("Called back");
        assertThat(updated.getSubject()).isNull();
        assertThat(updated.getReceivedAt()).isEqualTo(EDITED);
        assertThat(updated.getStatus()).isNull();
        assertThat(updated.getSentAt()).isNull();
    }

    @Test
    void markingADraftSentThroughAnEditStampsTheChosenDate() {
        ProspectMessage message = draft();

        ProspectMessage updated = service.updateMessage(user, prospect.getId(), message.getId(),
            MessageDirection.OUTBOUND, MessageChannel.EMAIL, MessageStatus.SENT, "Hello", "Hello again", EDITED);

        assertThat(updated.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(updated.getSentAt()).isEqualTo(EDITED);
        assertThat(updated.getReceivedAt()).isNull();
        assertThat(updated.getGeneratedBy()).isEqualTo("mcp-agent");
    }

    @Test
    void turningASentMessageIntoAReplyClearsItsOutboundFields() {
        ProspectMessage message = sent();

        ProspectMessage updated = service.updateMessage(user, prospect.getId(), message.getId(),
            MessageDirection.INBOUND, MessageChannel.EMAIL, MessageStatus.SENT, null, "Hello", EDITED);

        assertThat(updated.getDirection()).isEqualTo(MessageDirection.INBOUND);
        assertThat(updated.getStatus()).isNull();
        assertThat(updated.getSentAt()).isNull();
        assertThat(updated.getReceivedAt()).isEqualTo(EDITED);
    }

    @Test
    void withoutADateTheMessageKeepsItsOwn() {
        ProspectMessage message = sent();

        ProspectMessage updated = service.updateMessage(user, prospect.getId(), message.getId(),
            MessageDirection.OUTBOUND, MessageChannel.LINKEDIN, MessageStatus.SENT, null, "Hello", null);

        assertThat(updated.getSentAt()).isEqualTo(ORIGINAL);
        assertThat(updated.getChannel()).isEqualTo(MessageChannel.LINKEDIN);
    }

    @Test
    void aMessageOutsideTheUsersProspectIsNotFound() {
        UUID unknownMessage = UUID.randomUUID();
        when(prospectMessageRepository.findByIdAndProspectId(unknownMessage, prospect.getId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMessage(user, prospect.getId(), unknownMessage,
            MessageDirection.INBOUND, null, null, null, "Hello", EDITED))
            .isInstanceOf(KerflowException.class)
            .extracting("code")
            .isEqualTo(KerflowException.ErrorCode.MESSAGE_NOT_FOUND);
        verify(prospectMessageRepository, never()).save(any());
    }

    @Test
    void anotherUsersProspectIsNotFound() {
        User stranger = new User();
        stranger.setId(UUID.randomUUID());

        assertThatThrownBy(() -> service.updateMessage(stranger, prospect.getId(), UUID.randomUUID(),
            MessageDirection.INBOUND, null, null, null, "Hello", EDITED))
            .isInstanceOf(KerflowException.class)
            .extracting("code")
            .isEqualTo(KerflowException.ErrorCode.PROSPECT_NOT_FOUND);
    }

    @Test
    void anAgentEditingADraftOnlyChangesWhatItPasses() {
        ProspectMessage message = draft();
        message.setSubject("Hello");

        ProspectMessage updated = service.editMessage(user, prospect.getId(), message.getId(),
            null, null, "Hello, revised", EDITED);

        assertThat(updated.getBody()).isEqualTo("Hello, revised");
        assertThat(updated.getSubject()).isEqualTo("Hello");
        assertThat(updated.getDirection()).isEqualTo(MessageDirection.OUTBOUND);
        assertThat(updated.getStatus()).isEqualTo(MessageStatus.DRAFT);
        assertThat(updated.getChannel()).isEqualTo(MessageChannel.EMAIL);
        assertThat(updated.getSentAt()).isNull();
        assertThat(updated.getGeneratedBy()).isEqualTo("mcp-agent");
    }

    @Test
    void anAgentEditingASentMessageKeepsItSent() {
        ProspectMessage message = sent();

        ProspectMessage updated = service.editMessage(user, prospect.getId(), message.getId(),
            null, "Fixed subject", null, null);

        assertThat(updated.getSubject()).isEqualTo("Fixed subject");
        assertThat(updated.getBody()).isEqualTo("Hello");
        assertThat(updated.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(updated.getSentAt()).isEqualTo(ORIGINAL);

        ProspectMessage redated = service.editMessage(user, prospect.getId(), message.getId(),
            null, null, null, EDITED);

        assertThat(redated.getSentAt()).isEqualTo(EDITED);
    }

    @Test
    void anAgentEditingAReplyKeepsItAReply() {
        ProspectMessage message = inbound();

        ProspectMessage updated = service.editMessage(user, prospect.getId(), message.getId(),
            MessageChannel.LINKEDIN, null, "Thanks!", null);

        assertThat(updated.getDirection()).isEqualTo(MessageDirection.INBOUND);
        assertThat(updated.getStatus()).isNull();
        assertThat(updated.getChannel()).isEqualTo(MessageChannel.LINKEDIN);
        assertThat(updated.getBody()).isEqualTo("Thanks!");
        assertThat(updated.getSubject()).isEqualTo("Re: hello");
        assertThat(updated.getReceivedAt()).isEqualTo(ORIGINAL);
    }

    @Test
    void anAgentEditingAnUnknownMessageGetsNotFound() {
        UUID unknownMessage = UUID.randomUUID();
        when(prospectMessageRepository.findByIdAndProspectId(unknownMessage, prospect.getId()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editMessage(user, prospect.getId(), unknownMessage,
            null, null, "Hello", null))
            .isInstanceOf(KerflowException.class)
            .extracting("code")
            .isEqualTo(KerflowException.ErrorCode.MESSAGE_NOT_FOUND);
        verify(prospectMessageRepository, never()).save(any());
    }

    @Test
    void theThreadFollowsTheDatesOfTheExchangesNotTheirLogging() {
        ProspectMessage loggedFirstHappenedLast = ProspectMessage.builder().body("reply")
            .direction(MessageDirection.INBOUND).receivedAt(EDITED).build();
        ProspectMessage loggedLaterHappenedFirst = ProspectMessage.builder().body("call")
            .direction(MessageDirection.OUTBOUND).status(MessageStatus.SENT).sentAt(ORIGINAL).build();
        ProspectMessage draft = ProspectMessage.builder().body("draft")
            .direction(MessageDirection.OUTBOUND).status(MessageStatus.DRAFT).build();
        draft.setCreationDate(LocalDateTime.ofInstant(EDITED.plusSeconds(3600), ZoneId.systemDefault()));
        when(prospectMessageRepository.findAllByProspectIdOrderByCreationDateAsc(prospect.getId()))
            .thenReturn(List.of(loggedFirstHappenedLast, draft, loggedLaterHappenedFirst));

        assertThat(service.listMessages(user, prospect.getId()))
            .extracting(ProspectMessage::getBody)
            .containsExactly("call", "reply", "draft");
    }
}
