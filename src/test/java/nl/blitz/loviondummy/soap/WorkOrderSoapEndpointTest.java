package nl.blitz.loviondummy.soap;

import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import nl.blitz.loviondummy.config.WsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.test.server.MockWebServiceClient;

@SpringBootTest
class WorkOrderSoapEndpointTest {

    @Autowired
    private ApplicationContext applicationContext;

    private MockWebServiceClient client;

    @BeforeEach
    void setup() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void getWorkOrdersReturnsAtLeastOneOrder() {
        Source requestPayload = new StreamSource(
                """
                        <GetWorkOrdersRequest xmlns="http://www.loviondummy.nl/workorders">
                        </GetWorkOrdersRequest>
                        """);

        client.sendRequest(withPayload(requestPayload))
                .andExpect(xpath("//w:workOrder", namespace()).exists());
    }

    private static org.springframework.xml.namespace.SimpleNamespaceContext namespace() {
        org.springframework.xml.namespace.SimpleNamespaceContext context =
                new org.springframework.xml.namespace.SimpleNamespaceContext();
        context.bindNamespaceUri("w", WsConfig.NAMESPACE_URI);
        return context;
    }
}

