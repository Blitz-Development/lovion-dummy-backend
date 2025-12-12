package nl.blitz.loviondummy.soap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

public class SoapExceptionResolver extends SoapFaultMappingExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(SoapExceptionResolver.class);

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        log.error("SOAP fault occurred at endpoint: {}", endpoint != null ? endpoint.getClass().getSimpleName() : "unknown", ex);
    }
}

