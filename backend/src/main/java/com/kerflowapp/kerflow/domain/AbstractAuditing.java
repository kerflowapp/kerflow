package com.kerflowapp.kerflow.domain;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Getter
@Setter
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners({AuditingEntityListener.class})
public class AbstractAuditing implements Serializable {

    @Column(updatable = false)
    @CreatedBy
    protected String creationProcessId;
    @Column(updatable = false)
    @CreatedDate
    protected LocalDateTime creationDate;
    @LastModifiedBy
    protected String lastModificationProcessId;
    @LastModifiedDate
    protected LocalDateTime lastModificationDate;

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
    }

}
