package com.sentinelforensic.repository;

import com.sentinelforensic.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {
    List<LogEntry> findByInvestigationIdOrderByTimestampAscIdAsc(Long investigationId);
    List<LogEntry> findByInvestigationIdOrderByTimestampDescIdDesc(Long investigationId);
    long countByInvestigationId(Long investigationId);
    void deleteByInvestigationId(Long investigationId);

    @Query("SELECT DISTINCT l.username FROM LogEntry l WHERE l.investigationId = :investigationId AND l.username IS NOT NULL")
    List<String> findDistinctUsernamesByInvestigationId(@Param("investigationId") Long investigationId);

    @Query("SELECT COUNT(DISTINCT l.username) FROM LogEntry l WHERE l.investigationId = :investigationId AND l.username IS NOT NULL")
    long countDistinctUsersByInvestigationId(@Param("investigationId") Long investigationId);

    @Query("SELECT COUNT(DISTINCT l.username) FROM LogEntry l WHERE l.username IS NOT NULL")
    long countDistinctUsersGlobal();

    @Query("SELECT l.eventType, COUNT(l) FROM LogEntry l WHERE l.investigationId = :investigationId GROUP BY l.eventType ORDER BY COUNT(l) DESC")
    List<Object[]> countByEventTypeForInvestigation(@Param("investigationId") Long investigationId);

    @Query("SELECT l.eventType, COUNT(l) FROM LogEntry l GROUP BY l.eventType ORDER BY COUNT(l) DESC")
    List<Object[]> countByEventTypeGlobal();
}