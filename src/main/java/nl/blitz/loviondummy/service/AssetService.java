package nl.blitz.loviondummy.service;

import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.exception.ResourceNotFoundException;
import nl.blitz.loviondummy.repository.AssetRepository;
import nl.blitz.loviondummy.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AssetService implements AssetQueryService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;

    public AssetService(AssetRepository assetRepository, WorkOrderRepository workOrderRepository) {
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
    }

    public List<Asset> getAllAssets() {
        log.info("Fetching all assets");
        return assetRepository.findAll();
    }

    public Asset getAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset with id %d not found".formatted(id)));
    }

    public List<WorkOrder> getWorkOrdersForAsset(Long assetId) {
        log.info("Fetching work orders for asset {}", assetId);
        return workOrderRepository.findByAsset_Id(assetId);
    }
}

