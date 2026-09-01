package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "prospect_pipeline_columns")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProspectPipelineColumn extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String key;

    private String name;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;

    private String color;

    private String icon;

    @Builder.Default
    @Column(nullable = false)
    private Boolean system = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

