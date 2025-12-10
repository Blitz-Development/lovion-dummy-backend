package nl.blitz.loviondummy.soap.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.time.LocalDate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkOrderType", propOrder = {
        "externalWorkOrderId",
        "externalAssetRef",
        "description",
        "scheduledDate",
        "workType",
        "priority",
        "status"
})
public class WorkOrderType {

    @XmlElement(required = true)
    private String externalWorkOrderId;

    @XmlElement(required = true)
    private String externalAssetRef;

    private String description;

    private LocalDate scheduledDate;

    @XmlElement(required = true)
    private String workType;

    @XmlElement(required = true)
    private String priority;

    private String status;

    public String getExternalWorkOrderId() {
        return externalWorkOrderId;
    }

    public void setExternalWorkOrderId(String externalWorkOrderId) {
        this.externalWorkOrderId = externalWorkOrderId;
    }

    public String getExternalAssetRef() {
        return externalAssetRef;
    }

    public void setExternalAssetRef(String externalAssetRef) {
        this.externalAssetRef = externalAssetRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

