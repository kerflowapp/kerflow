package com.kerflowapp.kerflow.services.prospects;

import com.kerflowapp.kerflow.domain.Prospect;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.repositories.ProspectContactDates;
import com.kerflowapp.kerflow.repositories.ProspectMessageRepository;
import com.kerflowapp.kerflow.repositories.ProspectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Decides which prospects are waiting for a reply, and since when. The countdown starts at the
 * last message sent or, when none was logged, at the moment the card entered its column; any reply
 * received after that stops it. Only open deals (CONTACTED, IN_DISCUSSION) are concerned.
 */
@Service
@RequiredArgsConstructor
public class FollowUpService {

    public static final int DEFAULT_DELAY_DAYS = 5;
    public static final int MIN_DELAY_DAYS = 1;
    public static final int MAX_DELAY_DAYS = 90;

    private static final Set<String> ELIGIBLE_STATUS_KEYS =
        Set.of(KanbanStatus.CONTACTED.name(), KanbanStatus.IN_DISCUSSION.name());

    private final ProspectRepository prospectRepository;
    private final ProspectMessageRepository prospectMessageRepository;

    public record FollowUp(Prospect prospect, Instant waitingSince, long daysWaiting, long daysOverdue) {
    }

    public static int delayDays(User user) {
        Integer delay = user.getFollowUpDelayDays();
        return delay == null ? DEFAULT_DELAY_DAYS : delay;
    }

    /**
     * The date from which the prospect is waiting for a reply, or null when no follow-up applies.
     *
     * @param dates the prospect's thread dates, null when it has no message
     */
    public static Instant followUpSince(Prospect prospect, ProspectContactDates dates) {
        if (!ELIGIBLE_STATUS_KEYS.contains(statusKey(prospect))) {
            return null;
        }
        Instant lastSent = dates == null ? null : dates.lastSentAt();
        Instant lastReceived = dates == null ? null : dates.lastReceivedAt();

        // Messages are logged after the fact and often backdated, so the column move — usually done
        // right after contacting — must not override them: it only stands in when nothing was sent.
        Instant reference = lastSent != null ? lastSent : columnEnteredAt(prospect);
        if (reference == null) {
            return null;
        }
        if (lastReceived != null && !lastReceived.isBefore(reference)) {
            return null;
        }
        return reference;
    }

    /** Runs a single aggregate query for the whole list. */
    public Map<UUID, Instant> followUpSince(Collection<Prospect> prospects) {
        if (prospects.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ProspectContactDates> datesById = prospectMessageRepository
            .findContactDates(prospects.stream().map(Prospect::getId).toList())
            .stream()
            .collect(Collectors.toMap(ProspectContactDates::prospectId, Function.identity()));

        Map<UUID, Instant> result = new HashMap<>();
        for (Prospect prospect : prospects) {
            Instant since = followUpSince(prospect, datesById.get(prospect.getId()));
            if (since != null) {
                result.put(prospect.getId(), since);
            }
        }
        return result;
    }

    /** Prospects whose follow-up is due, most overdue first. */
    public List<FollowUp> dueFollowUps(User user, Instant now) {
        List<Prospect> prospects = prospectRepository.findAllByUserIdOrderByCreationDateDesc(user.getId());
        Map<UUID, Instant> sinceById = followUpSince(prospects);
        int delay = delayDays(user);

        return prospects.stream()
            .filter(p -> sinceById.containsKey(p.getId()))
            .map(p -> {
                Instant since = sinceById.get(p.getId());
                long daysWaiting = daysBetween(since, now);
                return new FollowUp(p, since, daysWaiting, daysWaiting - delay);
            })
            .filter(f -> f.daysOverdue() >= 0)
            .sorted(Comparator.comparingLong(FollowUp::daysOverdue).reversed())
            .toList();
    }

    /** Calendar days, so that "5 days" means the same thing here and on the board. */
    static long daysBetween(Instant from, Instant to) {
        ZoneId zone = ZoneId.systemDefault();
        return ChronoUnit.DAYS.between(from.atZone(zone).toLocalDate(), to.atZone(zone).toLocalDate());
    }

    private static String statusKey(Prospect prospect) {
        if (prospect.getStatusKey() != null) {
            return prospect.getStatusKey();
        }
        return prospect.getStatus() != null ? prospect.getStatus().name() : null;
    }

    /** Legacy rows have no statusChangedAt: their last modification is the closest we have. */
    private static Instant columnEnteredAt(Prospect prospect) {
        if (prospect.getStatusChangedAt() != null) {
            return prospect.getStatusChangedAt();
        }
        return prospect.getLastModificationDate() == null
            ? null
            : prospect.getLastModificationDate().atZone(ZoneId.systemDefault()).toInstant();
    }
}
