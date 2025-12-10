package nl.blitz.loviondummy.soap;

import java.util.List;
import nl.blitz.loviondummy.config.WsConfig;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.exception.ResourceNotFoundException;
import nl.blitz.loviondummy.service.WorkOrderQueryService;
import nl.blitz.loviondummy.soap.schema.GetWorkOrderDetailsRequest;
import nl.blitz.loviondummy.soap.schema.GetWorkOrderDetailsResponse;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersRequest;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersResponse;
import nl.blitz.loviondummy.soap.schema.WorkOrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class WorkOrderSoapEndpoint {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderSoapEndpoint.class);
    private static final String NAMESPACE_URI = WsConfig.NAMESPACE_URI;

    private final WorkOrderQueryService workOrderService;

    public WorkOrderSoapEndpoint(WorkOrderQueryService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetWorkOrdersRequest")
    @ResponsePayload
    public GetWorkOrdersResponse getWorkOrders(@RequestPayload GetWorkOrdersRequest request) {
        log.info("SOAP request: GetWorkOrders status={}", request.getStatus());
        List<WorkOrder> workOrders = workOrderService.getWorkOrders(request.getStatus(), null);
        GetWorkOrdersResponse response = new GetWorkOrdersResponse();
        workOrders.stream().map(this::mapToType).forEach(response.getWorkOrders()::add);
        log.info("SOAP response: returning {} work orders", response.getWorkOrders().size());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetWorkOrderDetailsRequest")
    @ResponsePayload
    public GetWorkOrderDetailsResponse getWorkOrderDetails(
            @RequestPayload GetWorkOrderDetailsRequest request) {
        log.info("SOAP request: GetWorkOrderDetails externalId={}", request.getExternalWorkOrderId());
        GetWorkOrderDetailsResponse response = new GetWorkOrderDetailsResponse();
        try {
            WorkOrder workOrder = workOrderService.getByExternalId(request.getExternalWorkOrderId());
            response.setWorkOrder(mapToType(workOrder));
            log.info("SOAP response: found work order {}", workOrder.getExternalWorkOrderId());
        } catch (ResourceNotFoundException ex) {
            log.warn("SOAP request work order not found: {}", request.getExternalWorkOrderId());
        }
        return response;
    }

    private WorkOrderType mapToType(WorkOrder workOrder) {
        WorkOrderType type = new WorkOrderType();
        type.setExternalWorkOrderId(workOrder.getExternalWorkOrderId());
        if (workOrder.getAsset() != null) {
            type.setExternalAssetRef(workOrder.getAsset().getExternalAssetRef());
        }
        type.setDescription(workOrder.getDescription());
        type.setScheduledDate(workOrder.getScheduledDate());
        type.setWorkType(workOrder.getWorkType());
        type.setPriority(workOrder.getPriority());
        type.setStatus(workOrder.getStatus());
        return type;
    }
}

