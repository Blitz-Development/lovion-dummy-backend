package nl.blitz.loviondummy.repository;

import java.util.List;
import java.util.Optional;
import nl.blitz.loviondummy.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findByStatusIgnoreCase(String status);

    List<WorkOrder> findByAsset_Id(Long assetId);

    List<WorkOrder> findByStatusIgnoreCaseAndAsset_Id(String status, Long assetId);

    Optional<WorkOrder> findByExternalWorkOrderId(String externalWorkOrderId);
}

