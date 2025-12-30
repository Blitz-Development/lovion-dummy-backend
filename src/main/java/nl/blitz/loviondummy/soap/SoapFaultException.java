package nl.blitz.loviondummy.soap;

public class SoapFaultException extends Exception {

    private final String faultCode;
    private final String faultString;
    private final String faultDetail;
    private final boolean isTransient;

    public SoapFaultException(String faultCode,
                              String faultString,
                              String faultDetail,
                              boolean isTransient) {
        super(faultString);
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultDetail = faultDetail;
        this.isTransient = isTransient;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public String getFaultString() {
        return faultString;
    }

    public String getFaultDetail() {
        return faultDetail;
    }

    public boolean isTransient() {
        return isTransient;
    }
}
