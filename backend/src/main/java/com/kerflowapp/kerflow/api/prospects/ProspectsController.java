package com.kerflowapp.kerflow.api.prospects;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.api.prospects.domain.*;
import com.kerflowapp.kerflow.configuration.rest.authorization.RequiresSubscription;
import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.ProspectMessage;
import com.kerflowapp.kerflow.domain.ProspectPipelineColumn;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.mappers.ProspectMapper;
import com.kerflowapp.kerflow.mappers.ProspectMessageMapper;
import com.kerflowapp.kerflow.mappers.ProspectPipelineColumnMapper;
import com.kerflowapp.kerflow.services.prospects.ProspectMessageService;
import com.kerflowapp.kerflow.services.prospects.ProspectPipelineService;
import com.kerflowapp.kerflow.services.prospects.ProspectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/prospects")
public class ProspectsController {

    private final ProspectService prospectService;
    private final ProspectMessageService prospectMessageService;
    private final ProspectMapper prospectMapper;
    private final ProspectMessageMapper prospectMessageMapper;
    private final ProspectPipelineService pipelineService;
    private final ProspectPipelineColumnMapper pipelineColumnMapper;

    @GetMapping
    public ResponseEntity<List<ProspectDto>> getAllProspects(@CurrentLoggedUser User loggedUser) {
        List<Prospect> prospects = prospectService.getAllProspects(loggedUser);
        return ResponseEntity.ok(prospectMapper.toDtoList(prospects));
    }

    @RequiresSubscription
    @PostMapping
    public ResponseEntity<ProspectDto> createProspect(@CurrentLoggedUser User loggedUser,
                                                      @RequestBody @Valid CreateProspectRequest request) {

        Prospect prospect = prospectService.createProspect(loggedUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(prospectMapper.toDto(prospect));
    }

    @RequiresSubscription
    @PutMapping("/{id}")
    public ResponseEntity<ProspectDto> updateProspect(@CurrentLoggedUser User loggedUser,
                                                      @PathVariable UUID id,
                                                      @RequestBody @Valid UpdateProspectRequest request) {

        Prospect prospect = prospectService.updateProspect(loggedUser, id, request);
        return ResponseEntity.ok(prospectMapper.toDto(prospect));
    }

    @RequiresSubscription
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProspectDto> updateStatus(@CurrentLoggedUser User loggedUser,
                                                    @PathVariable UUID id,
                                                    @RequestBody @Valid UpdateStatusRequest request
    ) {
        Prospect prospect = prospectService.updateStatus(loggedUser, id, request.statusKey());
        return ResponseEntity.ok(prospectMapper.toDto(prospect));
    }

    @RequiresSubscription
    @PutMapping("/_reorder")
    public ResponseEntity<List<ProspectDto>> reorderProspects(@CurrentLoggedUser User loggedUser,
                                                              @RequestBody @Valid ReorderProspectsRequest request) {

        List<Prospect> prospects = prospectService.reorder(loggedUser, request.statusKey(), request.orderedIds());
        return ResponseEntity.ok(prospectMapper.toDtoList(prospects));
    }

    @RequiresSubscription
    @PostMapping("/{id}/enrich")
    public ResponseEntity<ProspectDto> enrichProspect(@CurrentLoggedUser User loggedUser,
                                                      @PathVariable UUID id) {

        Prospect prospect = prospectService.enrichProspect(loggedUser, id);
        return ResponseEntity.ok(prospectMapper.toDto(prospect));
    }

    @RequiresSubscription
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProspect(@CurrentLoggedUser User loggedUser,
                                               @PathVariable UUID id
    ) {
        prospectService.deleteProspect(loggedUser, id);
        return ResponseEntity.noContent().build();
    }

    @RequiresSubscription
    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProspectDto>> importCsv(@CurrentLoggedUser User loggedUser,
                                                       @RequestPart("file") MultipartFile file) {

        List<Prospect> prospects = prospectService.importFromCsv(loggedUser, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(prospectMapper.toDtoList(prospects));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ProspectMessageDto>> getMessages(@CurrentLoggedUser User loggedUser,
                                                                @PathVariable UUID id) {

        List<ProspectMessage> messages = prospectMessageService.listMessages(loggedUser, id);
        return ResponseEntity.ok(prospectMessageMapper.toDtoList(messages));
    }

    @RequiresSubscription
    @PostMapping("/{id}/messages")
    public ResponseEntity<ProspectMessageDto> createMessage(@CurrentLoggedUser User loggedUser,
                                                            @PathVariable UUID id,
                                                            @RequestBody @Valid CreateProspectMessageRequest request) {

        ProspectMessage message = prospectMessageService.recordMessage(
            loggedUser, id, request.direction(), request.channel(), request.status(),
            request.subject(), request.body(), request.occurredAt(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(prospectMessageMapper.toDto(message));
    }

    @RequiresSubscription
    @PatchMapping("/{id}/messages/{messageId}/sent")
    public ResponseEntity<ProspectMessageDto> markMessageSent(@CurrentLoggedUser User loggedUser,
                                                              @PathVariable UUID id,
                                                              @PathVariable UUID messageId) {

        ProspectMessage message = prospectMessageService.markSent(loggedUser, id, messageId);
        return ResponseEntity.ok(prospectMessageMapper.toDto(message));
    }

    @RequiresSubscription
    @DeleteMapping("/{id}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@CurrentLoggedUser User loggedUser,
                                              @PathVariable UUID id,
                                              @PathVariable UUID messageId) {

        prospectMessageService.deleteMessage(loggedUser, id, messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pipeline-columns")
    public ResponseEntity<List<ProspectPipelineColumnDto>> getPipelineColumns(@CurrentLoggedUser User loggedUser) {
        List<ProspectPipelineColumn> columns = pipelineService.getOrCreateDefaultColumns(loggedUser);
        return ResponseEntity.ok(pipelineColumnMapper.toDtoList(columns));
    }

    @RequiresSubscription
    @PostMapping("/pipeline-columns")
    public ResponseEntity<ProspectPipelineColumnDto> createPipelineColumn(@CurrentLoggedUser User loggedUser,
                                                                          @RequestBody @Valid CreatePipelineColumnRequest request) {

        ProspectPipelineColumn column = pipelineService.createCustomColumn(loggedUser, request.name(), request.color(), request.icon());
        return ResponseEntity.status(HttpStatus.CREATED).body(pipelineColumnMapper.toDto(column));
    }

    @RequiresSubscription
    @PutMapping("/pipeline-columns/{id}")
    public ResponseEntity<ProspectPipelineColumnDto> updatePipelineColumn(@CurrentLoggedUser User loggedUser,
                                                                          @PathVariable UUID id,
                                                                          @RequestBody @Valid UpdatePipelineColumnRequest request) {

        ProspectPipelineColumn column = pipelineService.updateColumn(loggedUser, id, request.name(), request.color(), request.icon());
        return ResponseEntity.ok(pipelineColumnMapper.toDto(column));
    }

    @RequiresSubscription
    @PutMapping("/pipeline-columns/_reorder")
    public ResponseEntity<List<ProspectPipelineColumnDto>> reorderPipelineColumns(@CurrentLoggedUser User loggedUser,
                                                                                  @RequestBody @Valid ReorderPipelineColumnsRequest request) {

        List<ProspectPipelineColumn> columns = pipelineService.reorder(loggedUser, request.orderedIds());
        return ResponseEntity.ok(pipelineColumnMapper.toDtoList(columns));
    }

    @RequiresSubscription
    @DeleteMapping("/pipeline-columns/{id}")
    public ResponseEntity<Void> deletePipelineColumn(@CurrentLoggedUser User loggedUser,
                                                     @PathVariable UUID id) {

        pipelineService.deleteColumn(loggedUser, id);
        return ResponseEntity.noContent().build();
    }
}
