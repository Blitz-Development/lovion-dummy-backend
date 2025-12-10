package nl.blitz.loviondummy.rest;

import java.util.List;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.dto.DtoMapper;
import nl.blitz.loviondummy.dto.WorkOrderDto;
import nl.blitz.loviondummy.service.WorkOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderController {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderController.class);

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public ResponseEntity<List<WorkOrderDto>> getWorkOrders(
            @RequestParam(required = false) String status, @RequestParam(required = false) Long assetId) {
        log.info("REST GET /api/workorders with status {} and asset {}", status, assetId);
        List<WorkOrder> workOrders = workOrderService.getWorkOrders(status, assetId);
        List<WorkOrderDto> result = workOrders.stream().map(DtoMapper::toWorkOrderDto).toList();
        log.info("Returning {} work orders", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDto> getWorkOrder(@PathVariable Long id) {
        log.info("REST GET /api/workorders/{}", id);
        WorkOrder workOrder = workOrderService.getWorkOrder(id);
        return ResponseEntity.ok(DtoMapper.toWorkOrderDto(workOrder));
    }
}

