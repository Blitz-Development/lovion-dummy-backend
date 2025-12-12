package nl.blitz.loviondummy.soap.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"workOrders"})
@XmlRootElement(name = "GetWorkOrdersResponse", namespace = "http://www.loviondummy.nl/workorders")
public class GetWorkOrdersResponse {

    @XmlElement(name = "workOrder")
    private List<WorkOrderType> workOrders;

    public List<WorkOrderType> getWorkOrders() {
        if (workOrders == null) {
            workOrders = new ArrayList<>();
        }
        return workOrders;
    }
}


