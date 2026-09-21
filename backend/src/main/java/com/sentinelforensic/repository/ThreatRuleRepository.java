package com.sentinelforensic.repository;

import com.sentinelforensic.model.ThreatRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreatRuleRepository extends JpaRepository<ThreatRule, Long> {
    Optional<ThreatRule> findByRuleCode(String ruleCode);
    List<ThreatRule> findByEnabledTrue();
    List<ThreatRule> findAllByOrderByRuleCodeAsc();
}