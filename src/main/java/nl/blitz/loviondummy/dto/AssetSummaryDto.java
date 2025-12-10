package nl.blitz.loviondummy.dto;

public class AssetSummaryDto {

    private Long id;
    private String externalAssetRef;
    private String type;
    private String location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalAssetRef() {
        return externalAssetRef;
    }

    public void setExternalAssetRef(String externalAssetRef) {
        this.externalAssetRef = externalAssetRef;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

