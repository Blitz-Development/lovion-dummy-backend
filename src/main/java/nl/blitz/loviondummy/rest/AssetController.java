package nl.blitz.loviondummy.rest;

import java.util.List;
import nl.blitz.loviondummy.domain.Asset;
import nl.blitz.loviondummy.dto.AssetDto;
import nl.blitz.loviondummy.dto.DtoMapper;
import nl.blitz.loviondummy.service.AssetQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private static final Logger log = LoggerFactory.getLogger(AssetController.class);

    private final AssetQueryService assetService;

    public AssetController(AssetQueryService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAssets() {
        log.info("REST GET /api/assets");
        List<Asset> assets = assetService.getAllAssets();
        List<AssetDto> result = assets.stream().map(DtoMapper::toAssetDto).toList();
        log.info("Returning {} assets", result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDto> getAsset(@PathVariable Long id) {
        log.info("REST GET /api/assets/{}", id);
        Asset asset = assetService.getAsset(id);
        AssetDto dto = DtoMapper.toAssetDto(asset);
        return ResponseEntity.ok(dto);
    }
}

