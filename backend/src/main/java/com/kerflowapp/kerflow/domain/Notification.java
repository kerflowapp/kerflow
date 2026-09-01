package com.kerflowapp.kerflow.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "notification", schema = "public")
@With
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Notification extends AbstractAuditing {

    @Id
    @UuidGenerator
    private UUID id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String type;
    @Column(columnDefinition = "TEXT")
    private String url;
    private boolean isRead;
    private boolean isDeleted;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
