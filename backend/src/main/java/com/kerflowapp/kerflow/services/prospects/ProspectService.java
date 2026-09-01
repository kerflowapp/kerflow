package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.api.prospects.domain.CreateProspectRequest;
import com.kerflowapp.kerflow.api.prospects.domain.SocialLinksDto;
import com.kerflowapp.kerflow.api.prospects.domain.UpdateProspectRequest;
import com.kerflowapp.kerflow.domain.*;
import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.domain.enums.ProspectSource;
import com.kerflowapp.kerflow.exceptions.KerflowException;
import com.kerflowapp.kerflow.mappers.ProspectMapper;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentContext;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentEngine;
import com.kerflowapp.kerflow.services.enrichment.EnrichmentMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.kerflowapp.kerflow.exceptions.KerflowException.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectService {

    private final ProspectRepository prospectRepository;
    private final ProspectMessageRepository prospectMessageRepository;
    private final ProspectMapper prospectMapper;
    private final EnrichmentEngine enrichmentEngine;

    public List<Prospect> getAllProspects(User user) {
        // Fetched newest-first, then reordered so each column shows its cards by explicit
        // position (nulls last keep legacy rows in creation-date order until first reordered).
        return prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId()).stream()
            .sorted(Comparator.comparing(Prospect::getPosition,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    public List<Prospect> getProspectsByStatus(User user, KanbanStatus status) {
        return prospectRepository.findAllByUserIdAndStatusOrderByCreationDateDesc(user.getId(), status);
    }

    public Prospect getProspect(User user, UUID prospectId) {
        return prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));
    }

    public Prospect createProspect(User user, CreateProspectRequest request) {
        Prospect prospect = prospectMapper.toEntity(request);
        prospect.setUser(user);
        if (prospect.getSource() == null) {
            prospect.setSource(ProspectSource.MANUAL);
        }
        if (prospect.getStatusKey() == null) {
            prospect.setStatusKey(request.statusKey() != null ? request.statusKey() : KanbanStatus.NEW.name());
        }
        // Append to the end of its column
        prospect.setPosition((int) prospectRepository.countByUserIdAndStatusKey(user.getId(), prospect.getStatusKey()));
        applySocialLinks(prospect, request.socialLinks());
        runEnrichment(prospect, EnrichmentMode.CREATION);
        return prospectRepository.save(prospect);
    }

    /**
     * Re-runs the whole enrichment engine on a prospect and replaces everything it produces.
     * Entry point for CSV/MANUAL prospects, refreshes, and future slow enrichers.
     */
    public Prospect enrichProspect(User user, UUID prospectId) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        runEnrichment(prospect, EnrichmentMode.FULL);
        return prospectRepository.save(prospect);
    }

    /**
     * Runs the engine on a prospect and reports back everything the enrichers produced.
     * <p>
     * The Google data is read back from the entity rather than re-fetched: Place Details is
     * a paid call, and the fields we need (types, rating, review count) barely move.
     */
    private void runEnrichment(Prospect prospect, EnrichmentMode mode) {
        // On creation the Google and website signals already came from the search, so they
        // seed the run (the size heuristic reads them). A full re-enrichment recomputes them
        // all, so it starts from an empty list instead of duplicating them.
        List<ProspectSignal> seedSignals = mode == EnrichmentMode.CREATION && prospect.getSignals() != null
            ? new ArrayList<>(prospect.getSignals())
            : new ArrayList<>();

        EnrichmentContext context = EnrichmentContext.builder()
            .mode(mode)
            .name(prospect.getName())
            .address(prospect.getAddress())
            .phone(prospect.getPhone())
            .website(prospect.getWebsite())
            .googlePlaceId(prospect.getGooglePlaceId())
            .rating(prospect.getGoogleRating())
            .userRatingsTotal(prospect.getGoogleUserRatingsTotal())
            .googleTypes(prospect.getGoogleTypes() != null ? prospect.getGoogleTypes() : new ArrayList<>())
            .signals(seedSignals)
            .build();

        enrichmentEngine.run(context);

        prospect.setSignals(context.getSignals());
        prospect.setSiren(context.getSiren());
        prospect.setSizeEstimate(context.getSizeEstimate());
        prospect.setBusinessProfile(context.getBusinessProfile());
    }

    /**
     * Persists an agent-written qualification analysis (from an MCP client) on the prospect.
     */
    public Prospect saveAnalysis(User user, UUID prospectId, ProspectAnalysis analysis) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        prospect.setAnalysis(analysis);
        return prospectRepository.save(prospect);
    }

    /**
     * Persists an agent-written profile sheet (from an MCP client) on the prospect.
     * Overwrites any previous sheet.
     */
    public Prospect saveProfileSheet(User user, UUID prospectId, ProfileSheet profileSheet) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        prospect.setProfileSheet(profileSheet);
        return prospectRepository.save(prospect);
    }

    public Prospect updateProspect(User user, UUID prospectId, UpdateProspectRequest request) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        prospectMapper.updateEntity(prospect, request);
        applySocialLinks(prospect, request.socialLinks());
        return prospectRepository.save(prospect);
    }

    public Prospect updateStatus(User user, UUID prospectId, String statusKey) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        if (statusKey == null || statusKey.isBlank()) {
            throw new KerflowException(PROSPECT_INVALID_STATUS);
        }
        prospect.setStatusKey(statusKey);
        return prospectRepository.save(prospect);
    }

    /**
     * Reorders the cards of a single kanban column: each prospect in {@code orderedIds} gets the
     * given {@code statusKey} and its index as position. Also handles cross-column moves, since a
     * card dragged from another column just appears in the target column's ordered ids.
     */
    @Transactional
    public List<Prospect> reorder(User user, String statusKey, List<UUID> orderedIds) {
        if (statusKey == null || statusKey.isBlank()) {
            throw new KerflowException(PROSPECT_INVALID_STATUS);
        }
        List<Prospect> reordered = new ArrayList<>();
        for (int i = 0; i < orderedIds.size(); i++) {
            Prospect prospect = prospectRepository.findByIdAndUserId(orderedIds.get(i), user.getId())
                .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));
            prospect.setStatusKey(statusKey);
            prospect.setPosition(i);
            reordered.add(prospect);
        }
        return prospectRepository.saveAll(reordered);
    }

    public void deleteProspect(User user, UUID prospectId) {
        Prospect prospect = prospectRepository.findByIdAndUserId(prospectId, user.getId())
            .orElseThrow(() -> new KerflowException(PROSPECT_NOT_FOUND));

        // ddl-auto creates the messages FK without ON DELETE CASCADE
        prospectMessageRepository.deleteAllByProspectId(prospect.getId());
        prospectRepository.delete(prospect);
    }

    public List<Prospect> importFromCsv(User user, MultipartFile file) {
        List<Prospect> prospects = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new KerflowException(CSV_IMPORT_FAILED, "Empty CSV file");
            }

            String[] headers = headerLine.split(",");
            int nameIdx = findColumnIndex(headers, "name", "nom");
            int emailIdx = findColumnIndex(headers, "email", "mail");
            int phoneIdx = findColumnIndex(headers, "phone", "telephone", "tel");
            int addressIdx = findColumnIndex(headers, "address", "adresse");
            int websiteIdx = findColumnIndex(headers, "website", "site");
            int notesIdx = findColumnIndex(headers, "notes", "note", "commentaire");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = parseCsvLine(line);
                Prospect prospect = Prospect.builder()
                    .name(getValueSafe(values, nameIdx))
                    .email(getValueSafe(values, emailIdx))
                    .phone(getValueSafe(values, phoneIdx))
                    .address(getValueSafe(values, addressIdx))
                    .website(getValueSafe(values, websiteIdx))
                    .notes(getValueSafe(values, notesIdx))
                    .status(KanbanStatus.NEW)
                    .statusKey(KanbanStatus.NEW.name())
                    .source(ProspectSource.CSV)
                    .user(user)
                    .build();

                prospects.add(prospect);
            }

            return prospectRepository.saveAll(prospects);
        } catch (KerflowException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to import CSV", e);
            throw new KerflowException(CSV_IMPORT_FAILED, e.getMessage());
        }
    }

    private int findColumnIndex(String[] headers, String... possibleNames) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].trim().toLowerCase().replace("\"", "");
            for (String name : possibleNames) {
                if (header.equals(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getValueSafe(String[] values, int index) {
        if (index < 0 || index >= values.length) return null;
        String value = values[index].trim().replace("\"", "");
        return value.isEmpty() ? null : value;
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    private void applySocialLinks(Prospect prospect, SocialLinksDto socialLinks) {
        if (socialLinks == null) return;
        if (socialLinks.instagram() != null) {
            prospect.setInstagram(socialLinks.instagram());
        }
        if (socialLinks.facebook() != null) {
            prospect.setFacebook(socialLinks.facebook());
        }
        if (socialLinks.linkedin() != null) {
            prospect.setLinkedin(socialLinks.linkedin());
        }
    }
}
