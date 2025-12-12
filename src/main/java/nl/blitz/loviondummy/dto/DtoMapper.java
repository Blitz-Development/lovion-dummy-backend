package nl.blitz.loviondummy.dto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static AssetDto toAssetDto(Asset asset) {
        AssetDto dto = new AssetDto();
        dto.setId(asset.getId());
        dto.setExternalAssetRef(asset.getExternalAssetRef());
        dto.setType(asset.getType());
        dto.setDescription(asset.getDescription());
        dto.setLocation(asset.getLocation());
        dto.setWorkOrders(toWorkOrderSummaryList(asset.getWorkOrders()));
        return dto;
    }

    public static AssetSummaryDto toAssetSummary(Asset asset) {
        AssetSummaryDto dto = new AssetSummaryDto();
        dto.setId(asset.getId());
        dto.setExternalAssetRef(asset.getExternalAssetRef());
        dto.setType(asset.getType());
        dto.setLocation(asset.getLocation());
        return dto;
    }

    public static WorkOrderDto toWorkOrderDto(WorkOrder workOrder) {
        WorkOrderDto dto = new WorkOrderDto();
        dto.setId(workOrder.getId());
        dto.setExternalWorkOrderId(workOrder.getExternalWorkOrderId());
        dto.setWorkType(workOrder.getWorkType());
        dto.setPriority(workOrder.getPriority());
        dto.setScheduledDate(workOrder.getScheduledDate());
        dto.setStatus(workOrder.getStatus());
        dto.setDescription(workOrder.getDescription());
        if (workOrder.getAsset() != null) {
            dto.setAsset(toAssetSummary(workOrder.getAsset()));
        }
        return dto;
    }

    public static WorkOrderSummaryDto toWorkOrderSummary(WorkOrder workOrder) {
        WorkOrderSummaryDto dto = new WorkOrderSummaryDto();
        dto.setId(workOrder.getId());
        dto.setExternalWorkOrderId(workOrder.getExternalWorkOrderId());
        dto.setStatus(workOrder.getStatus());
        return dto;
    }

    private static List<WorkOrderSummaryDto> toWorkOrderSummaryList(List<WorkOrder> workOrders) {
        if (workOrders == null) {
            return Collections.emptyList();
        }
        return workOrders.stream()
                .map(DtoMapper::toWorkOrderSummary)
                .collect(Collectors.toList());
    }
}


