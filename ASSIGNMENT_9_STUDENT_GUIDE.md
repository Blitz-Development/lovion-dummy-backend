# Assignment 9: Student Implementation Guide (.NET Client)

## Overview

In this assignment, you'll implement **Advanced XSD Validation** in your .NET client. The backend now returns work orders that intentionally violate XSD constraints. Your job is to catch these violations BEFORE they cause deserialization errors.

## The Core Concept

The backend now returns work orders that **intentionally violate XSD constraints**. Students need to build a .NET client that:

1. **Validates XML against XSD schemas BEFORE deserializing**
2. **Catches validation errors and reports them**
3. **Implements business rule validation on top of XSD validation**
4. **Tracks validation issues in their database**

## The Two-Tier Validation Approach

```
┌─────────────────────────────┐
│ SOAP Response from Backend  │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  Tier 1: XSD Validation     │
└──────┬──────────────┬───────┘
       │              │
       │ Valid XML    │ Invalid XML
       │ Structure    │
       ▼              ▼
┌──────────────┐  ┌──────────────┐
│ Deserialize  │  │ Log XSD      │
│ to C# Objects│  │ Errors       │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────────────────────┐
│ Tier 2: Business Rule        │
│ Validation                   │
└──────┬──────────────┬────────┘
       │              │
       │ Valid        │ Invalid
       │ Business     │ Business
       │ Rules        │ Rules
       ▼              ▼
┌──────────────┐  ┌──────────────┐
│ Save to      │  │ Log Business │
│ Database     │  │ Warnings     │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│ WorkOrder    │  │ ImportError  │
│ Table        │  │ Table        │
└──────────────┘  └──────────────┘
```

## What the Backend Now Provides

The backend returns work orders like these in the SOAP response:

| Work Order ID | Issue | What's Wrong |
|---------------|-------|--------------|
| `WO-12345678` | ✅ Valid | Meets all constraints |
| `INVALID-ID` | ❌ XSD Error | Doesn't match pattern `WO-\d{8}` |
| `WO-87654321` | ❌ XSD Error | WorkType = "INVALID_TYPE" (not in enum) |
| `WO-11111111` | ❌ XSD Error | Description = "Hi" (too short, min 5 chars) |
| `WO-22222222` | ⚠️ Business Warning | Scheduled 5 days ago (past date) |
| `WO-33333333` | ⚠️ Business Warning | URGENT priority but 15 days away |
| `BAD` | ❌ Multiple Errors | Multiple constraint violations |

**Total**: 11 work orders in the database (4 original + 7 with validation issues)

## Learning Objectives

By completing this assignment, you will learn:
- How to validate XML against XSD schemas programmatically
- The difference between structural (XSD) and business rule validation
- How to handle validation errors gracefully
- How to create detailed validation reports
- How to work with multi-file XSD schemas

## Why This Matters: Real-World Scenario

In production systems, external data sources often send invalid data:
- Legacy systems with outdated validation
- Manual data entry errors
- Integration bugs
- Schema mismatches between systems

**Without proper validation**, your application will:
- ❌ Crash on deserialization
- ❌ Store corrupted data
- ❌ Have no audit trail of failures
- ❌ Be difficult to debug

**With two-tier validation**, your application will:
- ✅ Catch errors before they cause problems
- ✅ Provide detailed error reports
- ✅ Continue processing valid records
- ✅ Maintain data quality

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

The backend provides 7 work orders with validation issues. Test your implementation against each one:

### Test Case 1: Valid Work Order ✅
- **Work Order ID**: `WO-12345678`
- **Description**: "Valid work order with proper format and all constraints met"
- **Expected**: Should pass both XSD and business validation
- **Result**: Imported successfully

### Test Case 2: Invalid ID Pattern ❌
- **Work Order ID**: `INVALID-ID`
- **Issue**: Doesn't match pattern `WO-\d{8}`
- **Expected**: XSD validation error
- **Error Message**: "externalWorkOrderId does not match pattern WO-\\d{8}"
- **Severity**: ERROR
- **Result**: Should NOT be imported

### Test Case 3: Invalid WorkType ❌
- **Work Order ID**: `WO-87654321`
- **Issue**: WorkType = "INVALID_TYPE" (not in enum)
- **Expected**: XSD validation error
- **Error Message**: "workType must be one of: MAINTENANCE, REPAIR, INSPECTION, INSTALLATION"
- **Severity**: ERROR
- **Result**: Should NOT be imported

### Test Case 4: Too Short Description ❌
- **Work Order ID**: `WO-11111111`
- **Issue**: Description = "Hi" (only 2 characters, min is 5)
- **Expected**: XSD validation error
- **Error Message**: "description must be at least 5 characters long"
- **Severity**: ERROR
- **Result**: Should NOT be imported

### Test Case 5: Past Scheduled Date ⚠️
- **Work Order ID**: `WO-22222222`
- **Issue**: Scheduled 5 days ago
- **Expected**: Business rule warning
- **Error Message**: "scheduledDate is in the past"
- **Severity**: WARNING
- **Result**: Should be imported with warning logged

### Test Case 6: High Priority Far Future ⚠️
- **Work Order ID**: `WO-33333333`
- **Issue**: URGENT priority but scheduled 15 days away
- **Expected**: Business rule warning
- **Error Message**: "URGENT priority work orders should be scheduled within 7 days"
- **Severity**: WARNING
- **Result**: Should be imported with warning logged

### Test Case 7: Multiple Validation Errors ❌
- **Work Order ID**: `BAD`
- **Issues**: 
  - Invalid ID pattern
  - Invalid workType: "UNKNOWN"
  - Invalid priority: "CRITICAL"
  - Invalid status: "INVALID"
  - Description too short: "Bad"
  - Date in past (10 days ago)
- **Expected**: Multiple XSD validation errors
- **Error Message**: "Multiple errors: Invalid ID pattern, Invalid workType, Invalid priority, Invalid status, Description too short, Date in past"
- **Severity**: ERROR
- **Result**: Should NOT be imported

### Expected Test Results Summary

After running a full import, you should see:

```
Import Run Statistics:
- Total Records: 11 (4 original + 7 test cases)
- Success Count: 5 (4 original + 1 valid test case + 2 with warnings)
- Error Count: 4 (invalid ID, invalid type, short description, multiple errors)
- Warning Count: 2 (past date, high priority far future)
```

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

## Backend Validation Rules Reference

The backend has 8 validation rules configured in the database:

| Rule Name | Type | Expression | Severity |
|-----------|------|------------|----------|
| WorkOrder ID Pattern | XSD_PATTERN | `WO-\d{8}` | ERROR |
| Description Minimum Length | XSD_RESTRICTION | minLength=5 | ERROR |
| Description Maximum Length | XSD_RESTRICTION | maxLength=500 | ERROR |
| WorkType Enumeration | XSD_ENUMERATION | MAINTENANCE\|REPAIR\|INSPECTION\|INSTALLATION | ERROR |
| Priority Enumeration | XSD_ENUMERATION | LOW\|MEDIUM\|HIGH\|URGENT | ERROR |
| Status Enumeration | XSD_ENUMERATION | PENDING\|SCHEDULED\|IN_PROGRESS\|COMPLETED\|CANCELLED | ERROR |
| Scheduled Date Not In Past | BUSINESS_RULE | scheduledDate >= today | WARNING |
| High Priority Within 7 Days | BUSINESS_RULE | priority=URGENT\|HIGH => scheduledDate <= today+7days | WARNING |

You can view these rules in the backend's H2 console at `http://localhost:8080/h2-console` (table: `validation_rules`)

## Resources

- [XmlSchemaSet Documentation](https://docs.microsoft.com/en-us/dotnet/api/system.xml.schema.xmlschemaset)
- [XmlReader Validation](https://docs.microsoft.com/en-us/dotnet/standard/data/xml/xml-schema-xsd-validation-with-xmlschemaset)
- Backend validation rules: Check `validation_rules` table in H2 console
- Backend test data: Review `ASSIGNMENT_9_README.md` in the backend project

## Success Criteria

Your implementation is complete when:
- ✅ All XSD validation errors are caught before deserialization
- ✅ Business rules are validated separately
- ✅ Errors are logged with severity levels
- ✅ Validation reports are accessible via API
- ✅ Valid work orders are imported despite other errors
- ✅ Tests cover all validation scenarios

## Example: Complete Validation Flow

Here's what should happen when you import work orders:

```
1. Call SOAP endpoint → Receive 11 work orders

2. XSD Validation Phase:
   ✅ WO-12345678 → Valid
   ❌ INVALID-ID → Pattern violation
   ❌ WO-87654321 → Invalid enum value
   ❌ WO-11111111 → Description too short
   ✅ WO-22222222 → Valid (XSD passed)
   ✅ WO-33333333 → Valid (XSD passed)
   ❌ BAD → Multiple violations
   ✅ 4 original work orders → Valid

3. Deserialize valid/warning records → 7 work orders

4. Business Rule Validation Phase:
   ✅ WO-12345678 → No issues
   ⚠️ WO-22222222 → Past date warning
   ⚠️ WO-33333333 → Priority/date mismatch warning
   ✅ 4 original work orders → No issues

5. Database Results:
   - WorkOrder table: 7 records (5 clean + 2 with warnings)
   - ImportError table: 6 error records + 2 warning records
   - ImportRun: Success=7, Errors=4, Warnings=2

6. Validation Report Available:
   GET /api/imports/1/validation-report
   {
     "totalRecords": 11,
     "successCount": 7,
     "errorCount": 4,
     "warningCount": 2,
     "errors": {
       "ERROR": [...4 XSD errors...],
       "WARNING": [...2 business warnings...]
     }
   }
```

## Questions?

Review the backend's `ASSIGNMENT_9_README.md` for details on what test data is available and what validation rules are implemented.
