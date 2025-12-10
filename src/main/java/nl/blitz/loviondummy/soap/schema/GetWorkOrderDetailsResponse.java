package nl.blitz.loviondummy.soap.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"workOrder"})
@XmlRootElement(name = "GetWorkOrderDetailsResponse", namespace = "http://www.loviondummy.nl/workorders")
public class GetWorkOrderDetailsResponse {

    @XmlElement(name = "workOrder")
    private WorkOrderType workOrder;

    public WorkOrderType getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrderType workOrder) {
        this.workOrder = workOrder;
    }
}

