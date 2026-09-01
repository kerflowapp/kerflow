package com.kerflowapp.kerflow.domain;

import com.kerflowapp.kerflow.domain.enums.KanbanStatus;
import com.kerflowapp.kerflow.domain.enums.ProspectSource;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prospects")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Prospect extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;

    private Double lat;
    private Double lng;

    private String instagram;
    private String facebook;
    private String linkedin;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KanbanStatus status = KanbanStatus.NEW;

    /**
     * Dynamic kanban status key (system keys: NEW/CONTACTED/IN_DISCUSSION/WON/LOST, or custom keys).
     * We keep the legacy enum field for backward compatibility/migration.
     */
    @Column(name = "status_key")
    private String statusKey;

    /**
     * Ordering position within its kanban column (statusKey). Nullable: legacy rows have no
     * explicit order and fall back to creation date until the column is first reordered.
     */
    @Column(name = "position")
    private Integer position;

    @Enumerated(EnumType.STRING)
    private ProspectSource source;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<ProspectSignal> signals = new ArrayList<>();

    private String googlePlaceId;

    private String searchQuery;

    /**
     * Google Places data kept at creation time so re-enriching never needs a paid Place
     * Details call: the enrichers read these back instead of re-fetching.
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> googleTypes = new ArrayList<>();

    private Double googleRating;

    private Integer googleUserRatingsTotal;

    @Column(columnDefinition = "TEXT")
    private String googleEditorialSummary;

    private String siren;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private SizeEstimate sizeEstimate;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private BusinessProfile businessProfile;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private ProspectAnalysis analysis;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private ProfileSheet profileSheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
