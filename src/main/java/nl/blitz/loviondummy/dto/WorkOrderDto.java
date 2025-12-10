package nl.blitz.loviondummy.dto;

import java.time.LocalDate;

public class WorkOrderDto {

    private Long id;
    private String externalWorkOrderId;
    private String workType;
    private String priority;
    private LocalDate scheduledDate;
    private String status;
    private String description;
    private AssetSummaryDto asset;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public AssetSummaryDto getAsset() {
        return asset;
    }

    public void setAsset(AssetSummaryDto asset) {
        this.asset = asset;
    }
}

