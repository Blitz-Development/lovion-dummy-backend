package nl.blitz.loviondummy.soap.schema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"externalWorkOrderId"})
@XmlRootElement(name = "GetWorkOrderDetailsRequest", namespace = "http://www.loviondummy.nl/workorders")
public class GetWorkOrderDetailsRequest {

    private String externalWorkOrderId;

    public String getExternalWorkOrderId() {
        return externalWorkOrderId;
    }

    public void setExternalWorkOrderId(String externalWorkOrderId) {
        this.externalWorkOrderId = externalWorkOrderId;
    }
}

