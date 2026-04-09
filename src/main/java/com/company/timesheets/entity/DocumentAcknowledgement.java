package com.company.timesheets.entity;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "TS_DOCUMENT_ACKNOWLEDGEMENT", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_TS_DOC_ACK_UNQ", columnNames = {"DOCUMENT_ID", "USER_ID"})
})
@Entity(name = "ts_DocumentAcknowledgement")
public class DocumentAcknowledgement {
    // Персональная карточка ознакомления: один сотрудник, один опубликованный документ, один жизненный цикл подписи.

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @NotNull
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Document document;

    @InstanceName
    @NotNull
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @NotNull
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "SENT_AT")
    private OffsetDateTime sentAt;

    @Column(name = "VIEWED_AT")
    private OffsetDateTime viewedAt;

    @Column(name = "SIGNED_AT")
    private OffsetDateTime signedAt;

    @Column(name = "SIGNATURE_INFO", length = 1000)
    private String signatureInfo;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;

    @DeletedDate
    @Column(name = "DELETED_DATE")
    private OffsetDateTime deletedDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DocumentAcknowledgementStatus getStatus() {
        return status == null ? null : DocumentAcknowledgementStatus.fromId(status);
    }

    public void setStatus(DocumentAcknowledgementStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public OffsetDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(OffsetDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public OffsetDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(OffsetDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getSignatureInfo() {
        return signatureInfo;
    }

    public void setSignatureInfo(String signatureInfo) {
        this.signatureInfo = signatureInfo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public OffsetDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(OffsetDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }

    @jakarta.annotation.PostConstruct
    public void postConstruct() {
        // Все новые карточки начинаются со статуса NEW.
        setStatus(DocumentAcknowledgementStatus.NEW);
    }
}
