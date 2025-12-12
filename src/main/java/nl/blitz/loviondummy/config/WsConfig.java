package nl.blitz.loviondummy.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import nl.blitz.loviondummy.soap.schema.GetWorkOrderDetailsRequest;
import nl.blitz.loviondummy.soap.schema.GetWorkOrderDetailsResponse;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersRequest;
import nl.blitz.loviondummy.soap.schema.GetWorkOrdersResponse;
import nl.blitz.loviondummy.soap.schema.WorkOrderType;
import nl.blitz.loviondummy.soap.SoapExceptionResolver;

@EnableWs
@Configuration
public class WsConfig extends WsConfigurerAdapter {

    public static final String NAMESPACE_URI = "http://www.loviondummy.nl/workorders";

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "workorders")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema workOrdersSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("WorkOrdersPort");
        definition.setTargetNamespace(NAMESPACE_URI);
        definition.setLocationUri("/ws");
        definition.setSchema(workOrdersSchema);
        return definition;
    }

    @Bean
    public XsdSchema workOrdersSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/workorders.xsd"));
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(
                GetWorkOrdersRequest.class,
                GetWorkOrdersResponse.class,
                GetWorkOrderDetailsRequest.class,
                GetWorkOrderDetailsResponse.class,
                WorkOrderType.class);
        return marshaller;
    }

    @Bean
    public SoapExceptionResolver exceptionResolver() {
        return new SoapExceptionResolver();
    }
}

