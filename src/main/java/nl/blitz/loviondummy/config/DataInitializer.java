package nl.blitz.loviondummy.config;

import java.time.LocalDate;
import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.domain.WorkOrder;
import nl.blitz.loviondummy.domain.ValidationRule;
import nl.blitz.loviondummy.repository.AssetRepository;
import nl.blitz.loviondummy.repository.WorkOrderRepository;
import nl.blitz.loviondummy.repository.ValidationRuleRepository;
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
    private final ValidationRuleRepository validationRuleRepository;

    public DataInitializer(AssetRepository assetRepository, 
                          WorkOrderRepository workOrderRepository,
                          ValidationRuleRepository validationRuleRepository) {
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
        this.validationRuleRepository = validationRuleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (assetRepository.count() > 0) {
            log.info("Skipping demo data seeding (already present)");
            return;
        }

        log.info("Seeding demo assets, work orders, and validation rules");

        // Seed validation rules
        seedValidationRules();

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

        // Add work orders with validation issues for Assignment 9
        seedWorkOrdersWithValidationIssues(pipeline, station, cable);

        log.info("Demo data seeded: {} assets, {} work orders, {} validation rules", 
                assetRepository.count(), workOrderRepository.count(), validationRuleRepository.count());
    }

    private void seedValidationRules() {
        ValidationRule rule1 = new ValidationRule();
        rule1.setRuleName("WorkOrder ID Pattern");
        rule1.setRuleType("XSD_PATTERN");
        rule1.setRuleExpression("WO-\\d{8}");
        rule1.setSeverity("ERROR");
        rule1.setIsActive(true);

        ValidationRule rule2 = new ValidationRule();
        rule2.setRuleName("Description Minimum Length");
        rule2.setRuleType("XSD_RESTRICTION");
        rule2.setRuleExpression("minLength=5");
        rule2.setSeverity("ERROR");
        rule2.setIsActive(true);

        ValidationRule rule3 = new ValidationRule();
        rule3.setRuleName("Description Maximum Length");
        rule3.setRuleType("XSD_RESTRICTION");
        rule3.setRuleExpression("maxLength=500");
        rule3.setSeverity("ERROR");
        rule3.setIsActive(true);

        ValidationRule rule4 = new ValidationRule();
        rule4.setRuleName("WorkType Enumeration");
        rule4.setRuleType("XSD_ENUMERATION");
        rule4.setRuleExpression("MAINTENANCE|REPAIR|INSPECTION|INSTALLATION");
        rule4.setSeverity("ERROR");
        rule4.setIsActive(true);

        ValidationRule rule5 = new ValidationRule();
        rule5.setRuleName("Priority Enumeration");
        rule5.setRuleType("XSD_ENUMERATION");
        rule5.setRuleExpression("LOW|MEDIUM|HIGH|URGENT");
        rule5.setSeverity("ERROR");
        rule5.setIsActive(true);

        ValidationRule rule6 = new ValidationRule();
        rule6.setRuleName("Status Enumeration");
        rule6.setRuleType("XSD_ENUMERATION");
        rule6.setRuleExpression("PENDING|SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED");
        rule6.setSeverity("ERROR");
        rule6.setIsActive(true);

        ValidationRule rule7 = new ValidationRule();
        rule7.setRuleName("Scheduled Date Not In Past");
        rule7.setRuleType("BUSINESS_RULE");
        rule7.setRuleExpression("scheduledDate >= today");
        rule7.setSeverity("WARNING");
        rule7.setIsActive(true);

        ValidationRule rule8 = new ValidationRule();
        rule8.setRuleName("High Priority Within 7 Days");
        rule8.setRuleType("BUSINESS_RULE");
        rule8.setRuleExpression("priority=URGENT|HIGH => scheduledDate <= today+7days");
        rule8.setSeverity("WARNING");
        rule8.setIsActive(true);

        validationRuleRepository.saveAll(List.of(rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8));
        log.info("Seeded {} validation rules", 8);
    }

    private void seedWorkOrdersWithValidationIssues(Asset pipeline, Asset station, Asset cable) {
        // Valid work order with proper format
        WorkOrder valid = new WorkOrder();
        valid.setExternalWorkOrderId("WO-12345678");
        valid.setWorkType("MAINTENANCE");
        valid.setPriority("HIGH");
        valid.setScheduledDate(LocalDate.now().plusDays(5));
        valid.setAsset(pipeline);
        valid.setStatus("PENDING");
        valid.setDescription("Valid work order with proper format and all constraints met");
        valid.setValidationSeverity(null);
        valid.setValidationErrors(null);

        // Invalid ID format - doesn't match WO-\d{8} pattern
        WorkOrder invalidId = new WorkOrder();
        invalidId.setExternalWorkOrderId("INVALID-ID");
        invalidId.setWorkType("REPAIR");
        invalidId.setPriority("MEDIUM");
        invalidId.setScheduledDate(LocalDate.now().plusDays(3));
        invalidId.setAsset(station);
        invalidId.setStatus("PENDING");
        invalidId.setDescription("Work order with invalid ID format");
        invalidId.setValidationSeverity("ERROR");
        invalidId.setValidationErrors("externalWorkOrderId does not match pattern WO-\\d{8}");

        // Invalid WorkType - not in enumeration
        WorkOrder invalidType = new WorkOrder();
        invalidType.setExternalWorkOrderId("WO-87654321");
        invalidType.setWorkType("INVALID_TYPE");
        invalidType.setPriority("LOW");
        invalidType.setScheduledDate(LocalDate.now().plusDays(7));
        invalidType.setAsset(cable);
        invalidType.setStatus("PENDING");
        invalidType.setDescription("Work order with invalid work type value");
        invalidType.setValidationSeverity("ERROR");
        invalidType.setValidationErrors("workType must be one of: MAINTENANCE, REPAIR, INSPECTION, INSTALLATION");

        // Too short description - less than 5 characters
        WorkOrder shortDesc = new WorkOrder();
        shortDesc.setExternalWorkOrderId("WO-11111111");
        shortDesc.setWorkType("INSPECTION");
        shortDesc.setPriority("MEDIUM");
        shortDesc.setScheduledDate(LocalDate.now().plusDays(2));
        shortDesc.setAsset(pipeline);
        shortDesc.setStatus("PENDING");
        shortDesc.setDescription("Hi");
        shortDesc.setValidationSeverity("ERROR");
        shortDesc.setValidationErrors("description must be at least 5 characters long");

        // Past scheduled date - violates business rule
        WorkOrder pastDate = new WorkOrder();
        pastDate.setExternalWorkOrderId("WO-22222222");
        pastDate.setWorkType("INSTALLATION");
        pastDate.setPriority("HIGH");
        pastDate.setScheduledDate(LocalDate.now().minusDays(5));
        pastDate.setAsset(station);
        pastDate.setStatus("PENDING");
        pastDate.setDescription("Work order scheduled in the past which should not be allowed");
        pastDate.setValidationSeverity("WARNING");
        pastDate.setValidationErrors("scheduledDate is in the past");

        // High priority with far-future date - violates business rule
        WorkOrder highPriorityFar = new WorkOrder();
        highPriorityFar.setExternalWorkOrderId("WO-33333333");
        highPriorityFar.setWorkType("MAINTENANCE");
        highPriorityFar.setPriority("URGENT");
        highPriorityFar.setScheduledDate(LocalDate.now().plusDays(15));
        highPriorityFar.setAsset(cable);
        highPriorityFar.setStatus("PENDING");
        highPriorityFar.setDescription("Urgent priority but scheduled far in the future (15 days)");
        highPriorityFar.setValidationSeverity("WARNING");
        highPriorityFar.setValidationErrors("URGENT priority work orders should be scheduled within 7 days");

        // Multiple validation errors
        WorkOrder multipleErrors = new WorkOrder();
        multipleErrors.setExternalWorkOrderId("BAD");
        multipleErrors.setWorkType("UNKNOWN");
        multipleErrors.setPriority("CRITICAL");
        multipleErrors.setScheduledDate(LocalDate.now().minusDays(10));
        multipleErrors.setAsset(pipeline);
        multipleErrors.setStatus("INVALID");
        multipleErrors.setDescription("Bad");
        multipleErrors.setValidationSeverity("ERROR");
        multipleErrors.setValidationErrors("Multiple errors: Invalid ID pattern, Invalid workType, Invalid priority, Invalid status, Description too short, Date in past");

        workOrderRepository.saveAll(List.of(
            valid, invalidId, invalidType, shortDesc, pastDate, highPriorityFar, multipleErrors
        ));

        log.info("Seeded {} work orders with validation issues", 7);
    }
}


