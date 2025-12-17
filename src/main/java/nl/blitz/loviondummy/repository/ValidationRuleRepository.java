package nl.blitz.loviondummy.repository;

import nl.blitz.loviondummy.domain.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {

    List<ValidationRule> findByIsActiveTrue();

    List<ValidationRule> findByRuleType(String ruleType);

    List<ValidationRule> findBySeverity(String severity);
}
