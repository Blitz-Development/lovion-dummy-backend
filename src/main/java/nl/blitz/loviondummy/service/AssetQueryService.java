package nl.blitz.loviondummy.service;

import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;

public interface AssetQueryService {
    List<Asset> getAllAssets();

    Asset getAsset(Long id);

    List<WorkOrder> getWorkOrdersForAsset(Long assetId);
}

