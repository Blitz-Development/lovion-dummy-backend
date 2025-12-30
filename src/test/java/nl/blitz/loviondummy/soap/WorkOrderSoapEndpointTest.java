package nl.blitz.loviondummy.soap;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.service.WorkOrderQueryService;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersRequest;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersResponse;
import org.junit.jupiter.api.Test;

class WorkOrderSoapEndpointTest {SoapFaultSimulator simulator;

    @Test
    void getWorkOrdersReturnsAtLeastOneOrder() throws SoapFaultException{

        SoapFaultSimulator simulator = new SoapFaultSimulator() {
            @Override
            public SoapFaultException simulateFault() {
                return null;
            }
        };

        WorkOrderSoapEndpoint endpoint =
                new WorkOrderSoapEndpoint(
                        new StubWorkOrderService(),
                        simulator
                );

        GetWorkOrdersRequest request = new GetWorkOrdersRequest();

        GetWorkOrdersResponse response = endpoint.getWorkOrders(request);

        assertThat(response.getWorkOrders()).hasSize(1);
        assertThat(response.getWorkOrders().get(0).getExternalWorkOrderId()).isEqualTo("WO-STUB");
    }

    private static class StubWorkOrderService implements WorkOrderQueryService {

        private final List<WorkOrder> workOrders;

        StubWorkOrderService() {
            Asset asset = new Asset();
            asset.setExternalAssetRef("EXT-STUB");
            WorkOrder workOrder = new WorkOrder();
            workOrder.setExternalWorkOrderId("WO-STUB");
            workOrder.setAsset(asset);
            workOrder.setWorkType("INSPECTION");
            workOrder.setPriority("HIGH");
            workOrder.setStatus("NEW");
            workOrder.setScheduledDate(LocalDate.now());
            this.workOrders = List.of(workOrder);
        }

        @Override
        public List<WorkOrder> getWorkOrders(String status, Long assetId) {
            return workOrders;
        }

        @Override
        public WorkOrder getWorkOrder(Long id) {
            return workOrders.get(0);
        }

        @Override
        public WorkOrder getByExternalId(String externalWorkOrderId) {
            return workOrders.get(0);
        }
    }
}

