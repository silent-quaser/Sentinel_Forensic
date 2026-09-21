package com.sentinelforensic.repository;

import com.sentinelforensic.model.InvestigationNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestigationNoteRepository extends JpaRepository<InvestigationNote, Long> {
    List<InvestigationNote> findByInvestigationIdOrderByCreatedAtDesc(Long investigationId);
    void deleteByInvestigationId(Long investigationId);
}