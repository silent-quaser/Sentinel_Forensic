package com.sentinelforensic.repository;

import com.sentinelforensic.model.ThreatEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatEvidenceRepository extends JpaRepository<ThreatEvidence, Long> {
    List<ThreatEvidence> findByThreatId(Long threatId);
    List<ThreatEvidence> findByLogEntryId(Long logEntryId);
    void deleteByThreatId(Long threatId);

    @Query("SELECT te.threatId FROM ThreatEvidence te WHERE te.logEntryId = :logEntryId")
    List<Long> findThreatIdsByLogEntryId(@Param("logEntryId") Long logEntryId);

    @Query("SELECT te.logEntryId FROM ThreatEvidence te WHERE te.threatId = :threatId")
    List<Long> findLogEntryIdsByThreatId(@Param("threatId") Long threatId);
}