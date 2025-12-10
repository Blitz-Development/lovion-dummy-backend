# Lovion Dummy Backend

Een kleine Spring Boot demo (Java 17) die een Lovion-achtige backend simuleert met assets en werkorders. Biedt zowel REST- als SOAP-endpoints en start met in-memory H2 demo-data.

## Starten

```bash
mvn spring-boot:run
```

- H2 console: `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:loviondb`, user `sa`, leeg wachtwoord)
- REST basis-URL: `http://localhost:8080/api`
- SOAP WSDL: `http://localhost:8080/ws/workorders.wsdl`

## REST voorbeelden
- `GET /api/assets` — alle assets
- `GET /api/assets/{id}` — asset met gekoppelde werkorders
- `GET /api/workorders` — alle werkorders, optioneel filter `status` en/of `assetId`
- `GET /api/workorders/{id}` — details van één werkorder

## SOAP voorbeelden
- Operatie `GetWorkOrders` (optionele `status`)
- Operatie `GetWorkOrderDetails` (vereist `externalWorkOrderId`)

XSD bevindt zich in `src/main/resources/wsdl/workorders.xsd`. WSDL is bereikbaar via `/ws/workorders.wsdl`.

## Projectstructuur (globaal)
- `domain`, `repository`, `service` — JPA laag
- `rest` — REST controllers + exception afhandeling
- `soap` — SOAP endpoint en JAXB-schema classes
- `config` — WS config, logging filter en demo-data seeder

## Tests
Eenvoudige integratietests voor REST en SOAP zijn aanwezig en draaien mee met `mvn test`.

