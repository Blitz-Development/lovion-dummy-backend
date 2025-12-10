package nl.blitz.loviondummy.repository;

import nl.blitz.loviondummy.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
}

