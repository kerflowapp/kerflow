package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.ProspectPipelineColumn;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.repositories.ProspectPipelineColumnRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectPipelineService {

    private final ProspectPipelineColumnRepository columnRepository;
    private final ProspectRepository prospectRepository;

    @Transactional
    public List<ProspectPipelineColumn> getOrCreateDefaultColumns(User user) {
        List<ProspectPipelineColumn> existing = columnRepository.findAllByUserIdOrderBySortOrderAsc(user.getId());
        if (!existing.isEmpty()) {
            return existing;
        }

        List<ProspectPipelineColumn> defaults = new ArrayList<>();
        defaults.add(systemColumn(user, KanbanStatus.NEW.name(), 0, "info", "mdi-star-outline"));
        defaults.add(systemColumn(user, KanbanStatus.CONTACTED.name(), 1, "warning", "mdi-email-outline"));
        defaults.add(systemColumn(user, KanbanStatus.IN_DISCUSSION.name(), 2, "primary", "mdi-message-text-outline"));
        defaults.add(systemColumn(user, KanbanStatus.WON.name(), 3, "success", "mdi-check-circle-outline"));
        defaults.add(systemColumn(user, KanbanStatus.LOST.name(), 4, "error", "mdi-close-circle-outline"));

        return columnRepository.saveAll(defaults);
    }

    @Transactional
    public ProspectPipelineColumn createCustomColumn(User user, String name, String color, String icon) {
        if (name == null || name.isBlank()) {
            throw new KerflowException(PIPELINE_COLUMN_INVALID, "name is required");
        }

        int nextOrder = columnRepository.findAllByUserIdOrderBySortOrderAsc(user.getId()).size();
        String key = "CUSTOM_" + UUID.randomUUID();

        ProspectPipelineColumn column = ProspectPipelineColumn.builder()
            .user(user)
            .system(false)
            .key(key)
            .name(name)
            .color(color)
            .icon(icon)
            .sortOrder(nextOrder)
            .build();

        return columnRepository.save(column);
    }

    @Transactional
    public ProspectPipelineColumn updateColumn(User user, UUID id, String name, String color, String icon) {
        ProspectPipelineColumn column = columnRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new KerflowException(PIPELINE_COLUMN_NOT_FOUND));

        if (name == null || name.isBlank()) {
            throw new KerflowException(PIPELINE_COLUMN_INVALID, "name is required");
        }

        column.setName(name);
        column.setColor(color);
        column.setIcon(icon);
        return columnRepository.save(column);
    }

    @Transactional
    public void deleteColumn(User user, UUID id) {
        ProspectPipelineColumn column = columnRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new KerflowException(PIPELINE_COLUMN_NOT_FOUND));

        long usedCount = prospectRepository.countByUserIdAndStatusKey(user.getId(), column.getKey());
        if (usedCount > 0) {
            throw new KerflowException(PIPELINE_COLUMN_NOT_EMPTY, "Column has prospects");
        }

        columnRepository.delete(column);
    }

    @Transactional
    public List<ProspectPipelineColumn> reorder(User user, List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new KerflowException(PIPELINE_COLUMN_INVALID, "orderedIds is required");
        }

        List<ProspectPipelineColumn> columns = columnRepository.findAllByUserIdOrderBySortOrderAsc(user.getId());
        Map<UUID, ProspectPipelineColumn> byId = new HashMap<>();
        for (ProspectPipelineColumn c : columns) {
            byId.put(c.getId(), c);
        }

        if (orderedIds.size() != columns.size()) {
            throw new KerflowException(PIPELINE_COLUMN_INVALID, "orderedIds size mismatch");
        }

        for (UUID id : orderedIds) {
            if (!byId.containsKey(id)) {
                throw new KerflowException(PIPELINE_COLUMN_INVALID, "orderedIds contains unknown id");
            }
        }

        for (int i = 0; i < orderedIds.size(); i++) {
            ProspectPipelineColumn column = byId.get(orderedIds.get(i));
            column.setSortOrder(i);
        }

        return columnRepository.saveAll(columns).stream()
            .sorted(Comparator.comparing(ProspectPipelineColumn::getSortOrder))
            .toList();
    }

    private ProspectPipelineColumn systemColumn(User user, String key, int order, String color, String icon) {
        return ProspectPipelineColumn.builder()
            .user(user)
            .system(true)
            .key(key)
            .name(null)
            .sortOrder(order)
            .color(color)
            .icon(icon)
            .build();
    }
}

