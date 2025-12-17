# Assignment 9: Student Implementation Guide (.NET Client)

## Overview

In this assignment, you'll implement **Advanced XSD Validation** in your .NET client. The backend now returns work orders that intentionally violate XSD constraints. Your job is to catch these violations BEFORE they cause deserialization errors.

## Learning Objectives

By completing this assignment, you will learn:
- How to validate XML against XSD schemas programmatically
- The difference between structural (XSD) and business rule validation
- How to handle validation errors gracefully
- How to create detailed validation reports
- How to work with multi-file XSD schemas

## Background: The Problem

When you call the SOAP endpoint, you might receive XML like this:

```xml
<workOrder>
    <externalWorkOrderId>INVALID-ID</externalWorkOrderId>  <!-- ❌ Doesn't match WO-\d{8} pattern -->
    <workType>INVALID_TYPE</workType>                      <!-- ❌ Not in enum -->
    <description>Hi</description>                          <!-- ❌ Too short (min 5 chars) -->
    <priority>HIGH</priority>
    <status>PENDING</status>
</workOrder>
```

If you try to deserialize this directly, you'll get:
- Runtime exceptions
- Partial data corruption
- No clear error messages
- No way to track which work orders failed

## Solution: Two-Tier Validation

### Tier 1: XSD Validation (Structural)
Validates XML structure against XSD schemas:
- Pattern matching (ID format)
- Enumerations (valid values)
- Length restrictions
- Required fields
- Data types

### Tier 2: Business Rule Validation (Semantic)
Validates business logic:
- Dates not in the past
- High-priority orders scheduled within 7 days
- Cross-field dependencies
- Custom business constraints

## Step-by-Step Implementation

### Step 1: Download and Organize XSD Schemas

The backend provides multiple XSD files. Download them from:
```
http://localhost:8080/ws/workorders.wsdl
```

Or directly from the backend source:
- `common-types.xsd` - Shared types (patterns, enums, restrictions)
- `workorder-types.xsd` - WorkOrder structure
- `asset-types.xsd` - Asset references
- `integration-schema.xsd` - Main schema that imports others

**Organize them in your project:**
```
YourProject/
  Infrastructure/
    XmlSchemas/
      common-types.xsd
      workorder-types.xsd
      asset-types.xsd
      integration-schema.xsd
```

### Step 2: Create XmlValidationService

Create a service that validates XML against XSD schemas:

```csharp
public class XmlValidationService
{
    private readonly XmlSchemaSet _schemaSet;
    private readonly List<ValidationError> _validationErrors;

    public XmlValidationService(string schemaDirectory)
    {
        _schemaSet = new XmlSchemaSet();
        _validationErrors = new List<ValidationError>();
        
        // Load all XSD files
        LoadSchemas(schemaDirectory);
    }

    private void LoadSchemas(string directory)
    {
        // Load main schema (which imports others)
        var mainSchemaPath = Path.Combine(directory, "integration-schema.xsd");
        _schemaSet.Add(null, mainSchemaPath);
        _schemaSet.Compile();
    }

    public ValidationResult ValidateXml(string xmlContent)
    {
        _validationErrors.Clear();
        
        var settings = new XmlReaderSettings
        {
            ValidationType = ValidationType.Schema,
            Schemas = _schemaSet
        };
        
        settings.ValidationEventHandler += ValidationCallback;
        
        try
        {
            using var stringReader = new StringReader(xmlContent);
            using var xmlReader = XmlReader.Create(stringReader, settings);
            
            // Read through entire document to trigger validation
            while (xmlReader.Read()) { }
            
            return new ValidationResult
            {
                IsValid = _validationErrors.Count == 0,
                Errors = _validationErrors.ToList()
            };
        }
        catch (Exception ex)
        {
            _validationErrors.Add(new ValidationError
            {
                Severity = "ERROR",
                Message = $"XML parsing failed: {ex.Message}"
            });
            
            return new ValidationResult
            {
                IsValid = false,
                Errors = _validationErrors.ToList()
            };
        }
    }

    private void ValidationCallback(object sender, ValidationEventArgs e)
    {
        _validationErrors.Add(new ValidationError
        {
            Severity = e.Severity == XmlSeverityType.Error ? "ERROR" : "WARNING",
            Message = e.Message,
            LineNumber = e.Exception?.LineNumber,
            LinePosition = e.Exception?.LinePosition
        });
    }
}

public class ValidationResult
{
    public bool IsValid { get; set; }
    public List<ValidationError> Errors { get; set; }
}

public class ValidationError
{
    public string Severity { get; set; }
    public string Message { get; set; }
    public int? LineNumber { get; set; }
    public int? LinePosition { get; set; }
}
```

### Step 3: Create Business Rule Validator

Create a validator for business logic:

```csharp
public class WorkOrderBusinessValidator
{
    public List<ValidationError> Validate(WorkOrderDto workOrder)
    {
        var errors = new List<ValidationError>();

        // Rule 1: Scheduled date should not be in the past
        if (workOrder.ScheduledDate.HasValue && 
            workOrder.ScheduledDate.Value < DateTime.Today)
        {
            errors.Add(new ValidationError
            {
                Severity = "WARNING",
                Message = $"Work order {workOrder.ExternalWorkOrderId} is scheduled in the past ({workOrder.ScheduledDate.Value:yyyy-MM-dd})"
            });
        }

        // Rule 2: High/Urgent priority should be scheduled within 7 days
        if ((workOrder.Priority == "HIGH" || workOrder.Priority == "URGENT") &&
            workOrder.ScheduledDate.HasValue &&
            workOrder.ScheduledDate.Value > DateTime.Today.AddDays(7))
        {
            errors.Add(new ValidationError
            {
                Severity = "WARNING",
                Message = $"Work order {workOrder.ExternalWorkOrderId} has {workOrder.Priority} priority but is scheduled {(workOrder.ScheduledDate.Value - DateTime.Today).Days} days away"
            });
        }

        // Add more business rules as needed...

        return errors;
    }
}
```

### Step 4: Update ImportRun Entity

Add fields to track validation:

```csharp
public class ImportRun
{
    public int Id { get; set; }
    public DateTime StartedAt { get; set; }
    public DateTime? CompletedAt { get; set; }
    public string Status { get; set; }
    public int TotalRecords { get; set; }
    public int SuccessCount { get; set; }
    public int ErrorCount { get; set; }
    public int WarningCount { get; set; }  // NEW
    
    // Navigation property
    public List<ImportError> Errors { get; set; }
}
```

### Step 5: Create ImportError Entity

Track individual validation errors:

```csharp
public class ImportError
{
    public int Id { get; set; }
    public int ImportRunId { get; set; }
    public string ExternalWorkOrderId { get; set; }
    public string ErrorType { get; set; }  // "XSD_VALIDATION", "BUSINESS_RULE"
    public string Severity { get; set; }   // "ERROR", "WARNING", "INFO"
    public string ErrorMessage { get; set; }
    public int? LineNumber { get; set; }
    public int? LinePosition { get; set; }
    public DateTime OccurredAt { get; set; }
    
    // Navigation property
    public ImportRun ImportRun { get; set; }
}
```

### Step 6: Integrate Validation into Import Process

Update your SOAP client to use validation:

```csharp
public class WorkOrderImportService
{
    private readonly SoapWorkOrderClient _soapClient;
    private readonly XmlValidationService _xmlValidator;
    private readonly WorkOrderBusinessValidator _businessValidator;
    private readonly ApplicationDbContext _dbContext;

    public async Task<ImportRun> ImportWorkOrdersAsync()
    {
        var importRun = new ImportRun
        {
            StartedAt = DateTime.UtcNow,
            Status = "IN_PROGRESS"
        };
        _dbContext.ImportRuns.Add(importRun);
        await _dbContext.SaveChangesAsync();

        try
        {
            // Step 1: Get raw XML from SOAP service
            var rawXml = await _soapClient.GetWorkOrdersRawXmlAsync();
            
            // Step 2: Validate XML against XSD
            var xsdValidation = _xmlValidator.ValidateXml(rawXml);
            
            if (!xsdValidation.IsValid)
            {
                // Log XSD validation errors
                foreach (var error in xsdValidation.Errors)
                {
                    _dbContext.ImportErrors.Add(new ImportError
                    {
                        ImportRunId = importRun.Id,
                        ErrorType = "XSD_VALIDATION",
                        Severity = error.Severity,
                        ErrorMessage = error.Message,
                        LineNumber = error.LineNumber,
                        LinePosition = error.LinePosition,
                        OccurredAt = DateTime.UtcNow
                    });
                    
                    if (error.Severity == "ERROR")
                        importRun.ErrorCount++;
                    else
                        importRun.WarningCount++;
                }
            }
            
            // Step 3: Deserialize (only if XSD validation passed or only warnings)
            var workOrders = await _soapClient.GetWorkOrdersAsync();
            importRun.TotalRecords = workOrders.Count;
            
            // Step 4: Business rule validation
            foreach (var workOrder in workOrders)
            {
                var businessErrors = _businessValidator.Validate(workOrder);
                
                if (businessErrors.Any())
                {
                    foreach (var error in businessErrors)
                    {
                        _dbContext.ImportErrors.Add(new ImportError
                        {
                            ImportRunId = importRun.Id,
                            ExternalWorkOrderId = workOrder.ExternalWorkOrderId,
                            ErrorType = "BUSINESS_RULE",
                            Severity = error.Severity,
                            ErrorMessage = error.Message,
                            OccurredAt = DateTime.UtcNow
                        });
                        
                        if (error.Severity == "ERROR")
                            importRun.ErrorCount++;
                        else
                            importRun.WarningCount++;
                    }
                }
                
                // Step 5: Save valid work orders (or all if only warnings)
                if (!businessErrors.Any(e => e.Severity == "ERROR"))
                {
                    // Save work order to database
                    await SaveWorkOrderAsync(workOrder);
                    importRun.SuccessCount++;
                }
            }
            
            importRun.Status = "COMPLETED";
            importRun.CompletedAt = DateTime.UtcNow;
        }
        catch (Exception ex)
        {
            importRun.Status = "FAILED";
            importRun.CompletedAt = DateTime.UtcNow;
            
            _dbContext.ImportErrors.Add(new ImportError
            {
                ImportRunId = importRun.Id,
                ErrorType = "SYSTEM_ERROR",
                Severity = "ERROR",
                ErrorMessage = ex.Message,
                OccurredAt = DateTime.UtcNow
            });
        }
        
        await _dbContext.SaveChangesAsync();
        return importRun;
    }
}
```

### Step 7: Create Validation Report API

Add an endpoint to view validation reports:

```csharp
[ApiController]
[Route("api/imports")]
public class ImportController : ControllerBase
{
    private readonly ApplicationDbContext _dbContext;

    [HttpGet("{id}/validation-report")]
    public async Task<ActionResult<ValidationReport>> GetValidationReport(int id)
    {
        var importRun = await _dbContext.ImportRuns
            .Include(i => i.Errors)
            .FirstOrDefaultAsync(i => i.Id == id);

        if (importRun == null)
            return NotFound();

        var report = new ValidationReport
        {
            ImportRunId = importRun.Id,
            StartedAt = importRun.StartedAt,
            CompletedAt = importRun.CompletedAt,
            Status = importRun.Status,
            TotalRecords = importRun.TotalRecords,
            SuccessCount = importRun.SuccessCount,
            ErrorCount = importRun.ErrorCount,
            WarningCount = importRun.WarningCount,
            Errors = importRun.Errors
                .GroupBy(e => e.Severity)
                .ToDictionary(
                    g => g.Key,
                    g => g.Select(e => new ErrorDetail
                    {
                        ExternalWorkOrderId = e.ExternalWorkOrderId,
                        ErrorType = e.ErrorType,
                        Message = e.ErrorMessage,
                        LineNumber = e.LineNumber,
                        OccurredAt = e.OccurredAt
                    }).ToList()
                )
        };

        return Ok(report);
    }
}
```

## Testing Your Implementation

### Test Case 1: Valid Work Order
**Expected**: Should pass both XSD and business validation

### Test Case 2: Invalid ID Pattern
**Work Order**: `INVALID-ID`
**Expected**: XSD validation error - "does not match pattern WO-\d{8}"

### Test Case 3: Invalid WorkType
**Work Order**: `WO-87654321` with WorkType = "INVALID_TYPE"
**Expected**: XSD validation error - "not a valid enumeration value"

### Test Case 4: Too Short Description
**Work Order**: `WO-11111111` with Description = "Hi"
**Expected**: XSD validation error - "length must be at least 5"

### Test Case 5: Past Date
**Work Order**: `WO-22222222` scheduled 5 days ago
**Expected**: Business rule warning - "scheduled in the past"

### Test Case 6: High Priority Far Future
**Work Order**: `WO-33333333` URGENT priority, 15 days away
**Expected**: Business rule warning - "high priority should be within 7 days"

## Deliverables

1. **XmlValidationService** - XSD validation implementation
2. **WorkOrderBusinessValidator** - Business rule validation
3. **Updated ImportRun entity** - With validation tracking
4. **ImportError entity** - For error details
5. **Validation report API** - GET endpoint for viewing errors
6. **Unit tests** - For both validation tiers
7. **Documentation** - Explaining validation rules

## Bonus Challenges

1. **Configurable Rules**: Store business rules in database (like backend does)
2. **Validation Dashboard**: UI showing validation statistics
3. **Auto-correction**: Attempt to fix common validation errors
4. **Validation Profiles**: Different validation rules for different sources
5. **Performance**: Validate large XML files efficiently

## Common Pitfalls

❌ **Don't**: Deserialize first, then validate
✅ **Do**: Validate XML first, then deserialize

❌ **Don't**: Ignore warnings
✅ **Do**: Log warnings for review

❌ **Don't**: Fail entire import on one error
✅ **Do**: Continue processing, log errors per work order

❌ **Don't**: Hard-code validation rules
✅ **Do**: Make rules configurable

## Resources

- [XmlSchemaSet Documentation](https://docs.microsoft.com/en-us/dotnet/api/system.xml.schema.xmlschemaset)
- [XmlReader Validation](https://docs.microsoft.com/en-us/dotnet/standard/data/xml/xml-schema-xsd-validation-with-xmlschemaset)
- Backend validation rules: Check `validation_rules` table in H2 console

## Success Criteria

Your implementation is complete when:
- ✅ All XSD validation errors are caught before deserialization
- ✅ Business rules are validated separately
- ✅ Errors are logged with severity levels
- ✅ Validation reports are accessible via API
- ✅ Valid work orders are imported despite other errors
- ✅ Tests cover all validation scenarios

## Questions?

Review the backend's `ASSIGNMENT_9_README.md` for details on what test data is available and what validation rules are implemented.
