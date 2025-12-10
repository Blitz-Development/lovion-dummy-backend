package nl.blitz.loviondummy.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.rest.AssetController;
import nl.blitz.loviondummy.service.AssetQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssetControllerTest {

    @Test
    void assetsEndpointReturnsData() throws Exception {
        Asset asset = new Asset();
        asset.setExternalAssetRef("EXT-TEST");
        asset.setType("PIPE");
        asset.setLocation("Test");
        WorkOrder order = new WorkOrder();
        order.setExternalWorkOrderId("WO-TEST");
        order.setStatus("NEW");
        asset.setWorkOrders(List.of(order));

        AssetController controller = new AssetController(new StubAssetService(asset));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalAssetRef").value("EXT-TEST"))
                .andExpect(jsonPath("$[0].workOrders[0].externalWorkOrderId").value("WO-TEST"));
    }

    private static class StubAssetService implements AssetQueryService {
        private final List<Asset> assets;

        StubAssetService(Asset asset) {
            this.assets = List.of(asset);
        }

        @Override
        public List<Asset> getAllAssets() {
            return assets;
        }

        @Override
        public Asset getAsset(Long id) {
            return assets.get(0);
        }

        @Override
        public List<WorkOrder> getWorkOrdersForAsset(Long assetId) {
            return assets.get(0).getWorkOrders();
        }
    }
}

