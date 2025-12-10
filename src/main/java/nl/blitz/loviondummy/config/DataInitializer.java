package nl.blitz.loviondummy.config;

import java.time.LocalDate;
import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.repository.AssetRepository;
import nl.blitz.loviondummy.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a few demo assets and work orders at startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;

    public DataInitializer(AssetRepository assetRepository, WorkOrderRepository workOrderRepository) {
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (assetRepository.count() > 0) {
            log.info("Skipping demo data seeding (already present)");
            return;
        }

        log.info("Seeding demo assets and work orders");

        Asset pipeline = new Asset();
        pipeline.setExternalAssetRef("EXT-PIPE-001");
        pipeline.setType("PIPE");
        pipeline.setDescription("Underground pipeline in district north");
        pipeline.setLocation("North District");

        Asset station = new Asset();
        station.setExternalAssetRef("EXT-STATION-002");
        station.setType("STATION");
        station.setDescription("Distribution station near central square");
        station.setLocation("Central Square");

        Asset cable = new Asset();
        cable.setExternalAssetRef("EXT-CABLE-003");
        cable.setType("CABLE");
        cable.setDescription("Medium voltage cable section");
        cable.setLocation("Industrial Park");

        assetRepository.saveAll(List.of(pipeline, station, cable));

        WorkOrder wo1 = new WorkOrder();
        wo1.setExternalWorkOrderId("WO-1001");
        wo1.setWorkType("INSPECTION");
        wo1.setPriority("HIGH");
        wo1.setScheduledDate(LocalDate.now().plusDays(2));
        wo1.setAsset(pipeline);
        wo1.setStatus("PLANNED");
        wo1.setDescription("Inspect pipeline corrosion status");

        WorkOrder wo2 = new WorkOrder();
        wo2.setExternalWorkOrderId("WO-1002");
        wo2.setWorkType("MAINTENANCE");
        wo2.setPriority("MEDIUM");
        wo2.setScheduledDate(LocalDate.now().plusDays(5));
        wo2.setAsset(station);
        wo2.setStatus("NEW");
        wo2.setDescription("Replace filter units");

        WorkOrder wo3 = new WorkOrder();
        wo3.setExternalWorkOrderId("WO-1003");
        wo3.setWorkType("INSPECTION");
        wo3.setPriority("LOW");
        wo3.setScheduledDate(LocalDate.now().plusDays(1));
        wo3.setAsset(cable);
        wo3.setStatus("IN_PROGRESS");
        wo3.setDescription("Check insulation integrity");

        WorkOrder wo4 = new WorkOrder();
        wo4.setExternalWorkOrderId("WO-1004");
        wo4.setWorkType("MAINTENANCE");
        wo4.setPriority("HIGH");
        wo4.setScheduledDate(LocalDate.now().minusDays(1));
        wo4.setAsset(pipeline);
        wo4.setStatus("DONE");
        wo4.setDescription("Valve replacement");

        workOrderRepository.saveAll(List.of(wo1, wo2, wo3, wo4));

        log.info("Demo data seeded: {} assets, {} work orders", assetRepository.count(),
                workOrderRepository.count());
    }
}

