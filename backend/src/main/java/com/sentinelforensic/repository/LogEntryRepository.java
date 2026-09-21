package com.sentinelforensic.repository;

import com.sentinelforensic.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
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

    @Query("SELECT l FROM LogEntry l WHERE l.investigationId = :investigationId " +
           "AND (:eventType IS NULL OR l.eventType = :eventType) " +
           "AND (:username IS NULL OR LOWER(l.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:source IS NULL OR LOWER(l.source) LIKE LOWER(CONCAT('%', :source, '%'))) " +
           "AND (:startTime IS NULL OR l.timestamp >= :startTime) " +
           "AND (:endTime IS NULL OR l.timestamp <= :endTime) " +
           "AND (:keyword IS NULL OR LOWER(l.rawMessage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY l.timestamp ASC, l.id ASC")
    Page<LogEntry> filterInvestigationLogs(
            @Param("investigationId") Long investigationId,
            @Param("eventType") String eventType,
            @Param("username") String username,
            @Param("source") String source,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE " +
           "(:eventType IS NULL OR l.eventType = :eventType) " +
           "AND (:username IS NULL OR LOWER(l.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:source IS NULL OR LOWER(l.source) LIKE LOWER(CONCAT('%', :source, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(l.rawMessage) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY l.timestamp DESC, l.id DESC")
    Page<LogEntry> searchGlobalLogs(
            @Param("eventType") String eventType,
            @Param("username") String username,
            @Param("source") String source,
            @Param("keyword") String keyword,
            Pageable pageable);
}