package nl.blitz.loviondummy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_workorder_id", nullable = false, unique = true)
    private String externalWorkOrderId;

    @Column(name = "work_type", nullable = false)
    private String workType;

    @Column(nullable = false)
    private String priority;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false)
    private String status;

    private String description;

    @Column(name = "validation_severity", length = 20)
    private String validationSeverity;

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    public Long getId() {
        return id;
    }

    public String getExternalWorkOrderId() {
        return externalWorkOrderId;
    }

    public void setExternalWorkOrderId(String externalWorkOrderId) {
        this.externalWorkOrderId = externalWorkOrderId;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidationSeverity() {
        return validationSeverity;
    }

    public void setValidationSeverity(String validationSeverity) {
        this.validationSeverity = validationSeverity;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }
}


