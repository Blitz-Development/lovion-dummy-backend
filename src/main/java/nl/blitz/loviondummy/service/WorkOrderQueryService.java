package nl.blitz.loviondummy.service;

import java.util.List;
import nl.blitz.loviondummy.domain.WorkOrder;

public interface WorkOrderQueryService {
    List<WorkOrder> getWorkOrders(String status, Long assetId);

    WorkOrder getWorkOrder(Long id);

    WorkOrder getByExternalId(String externalWorkOrderId);
}


