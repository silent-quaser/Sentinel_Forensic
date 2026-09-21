package com.sentinelforensic.repository;

import com.sentinelforensic.model.Threat;
import com.sentinelforensic.model.ThreatSeverity;
import com.sentinelforensic.model.ThreatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreatRepository extends JpaRepository<Threat, Long> {
    List<Threat> findByInvestigationIdOrderByDetectedAtDesc(Long investigationId);
    List<Threat> findAllByOrderByDetectedAtDesc();
    long countByInvestigationId(Long investigationId);
    long countBySeverity(ThreatSeverity severity);
    long countByInvestigationIdAndSeverity(Long investigationId, ThreatSeverity severity);
    long countByInvestigationIdAndSeverityIn(Long investigationId, List<ThreatSeverity> severities);
    long countBySeverityIn(List<ThreatSeverity> severities);
    void deleteByInvestigationId(Long investigationId);

    Optional<Threat> findByInvestigationIdAndRuleCodeAndAffectedUser(Long investigationId, String ruleCode, String affectedUser);

    @Query("SELECT t.severity, COUNT(t) FROM Threat t WHERE t.investigationId = :investigationId GROUP BY t.severity")
    List<Object[]> countBySeverityForInvestigation(@Param("investigationId") Long investigationId);

    @Query("SELECT t.severity, COUNT(t) FROM Threat t GROUP BY t.severity")
    List<Object[]> countBySeverityGlobal();

    @Query("SELECT t FROM Threat t WHERE " +
           "(:investigationId IS NULL OR t.investigationId = :investigationId) " +
           "AND (:severity IS NULL OR t.severity = :severity) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:ruleCode IS NULL OR t.ruleCode = :ruleCode) " +
           "AND (:username IS NULL OR LOWER(t.affectedUser) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "ORDER BY t.detectedAt DESC")
    List<Threat> filterThreats(
            @Param("investigationId") Long investigationId,
            @Param("severity") ThreatSeverity severity,
            @Param("status") ThreatStatus status,
            @Param("ruleCode") String ruleCode,
            @Param("username") String username);
}