package nl.blitz.loviondummy.dto;

import java.util.List;

public class AssetDto {

    private Long id;
    private String externalAssetRef;
    private String type;
    private String description;
    private String location;
    private List<WorkOrderSummaryDto> workOrders;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalAssetRef() {
        return externalAssetRef;
    }

    public void setExternalAssetRef(String externalAssetRef) {
        this.externalAssetRef = externalAssetRef;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<WorkOrderSummaryDto> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<WorkOrderSummaryDto> workOrders) {
        this.workOrders = workOrders;
    }
}

