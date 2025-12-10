package nl.blitz.loviondummy.service;

import java.util.List;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.exception.ResourceNotFoundException;
import nl.blitz.loviondummy.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkOrderService implements WorkOrderQueryService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

    private final WorkOrderRepository workOrderRepository;

    public WorkOrderService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    public List<WorkOrder> getWorkOrders(String status, Long assetId) {
        if (status != null && assetId != null) {
            log.info("Fetching work orders by status {} and asset {}", status, assetId);
            return workOrderRepository.findByStatusIgnoreCaseAndAsset_Id(status, assetId);
        }
        if (status != null) {
            log.info("Fetching work orders by status {}", status);
            return workOrderRepository.findByStatusIgnoreCase(status);
        }
        if (assetId != null) {
            log.info("Fetching work orders by asset {}", assetId);
            return workOrderRepository.findByAsset_Id(assetId);
        }
        log.info("Fetching all work orders");
        return workOrderRepository.findAll();
    }

    public WorkOrder getWorkOrder(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder with id %d not found".formatted(id)));
    }

    public WorkOrder getByExternalId(String externalWorkOrderId) {
        return workOrderRepository.findByExternalWorkOrderId(externalWorkOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WorkOrder with external id %s not found".formatted(externalWorkOrderId)));
    }
}

