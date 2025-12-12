package nl.blitz.loviondummy.repository;

import java.util.List;
import java.util.Optional;
import nl.blitz.loviondummy.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    @Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.asset WHERE UPPER(wo.status) = UPPER(:status)")
    List<WorkOrder> findByStatusIgnoreCase(@Param("status") String status);

    @Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.asset WHERE wo.asset.id = :assetId")
    List<WorkOrder> findByAsset_Id(@Param("assetId") Long assetId);

    @Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.asset WHERE UPPER(wo.status) = UPPER(:status) AND wo.asset.id = :assetId")
    List<WorkOrder> findByStatusIgnoreCaseAndAsset_Id(@Param("status") String status, @Param("assetId") Long assetId);

    @Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.asset WHERE wo.externalWorkOrderId = :externalWorkOrderId")
    Optional<WorkOrder> findByExternalWorkOrderId(@Param("externalWorkOrderId") String externalWorkOrderId);

    @Query("SELECT wo FROM WorkOrder wo LEFT JOIN FETCH wo.asset")
    @Override
    List<WorkOrder> findAll();
}


