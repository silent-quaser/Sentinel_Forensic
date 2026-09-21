package com.sentinelforensic.repository;

import com.sentinelforensic.model.Investigation;
import com.sentinelforensic.model.InvestigationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    Optional<Investigation> findByInvestigationId(String investigationId);
    boolean existsByInvestigationId(String investigationId);
    long countByStatus(InvestigationStatus status);
    List<Investigation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT i FROM Investigation i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.investigatorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.investigationId) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY i.createdAt DESC")
    List<Investigation> searchInvestigations(@Param("query") String query);
}