package nl.blitz.loviondummy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_asset_ref", nullable = false, unique = true)
    private String externalAssetRef;

    @Column(nullable = false)
    private String type;

    private String description;

    private String location;

    @OneToMany(mappedBy = "asset")
    private List<WorkOrder> workOrders = new ArrayList<>();

    public Long getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<WorkOrder> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<WorkOrder> workOrders) {
        this.workOrders = workOrders;
    }
}


