package com.sentinelforensic.service;

import com.sentinelforensic.dto.CreateNoteRequest;
import com.sentinelforensic.dto.InvestigationNoteDto;
import com.sentinelforensic.exception.ResourceNotFoundException;
import com.sentinelforensic.model.InvestigationNote;
import com.sentinelforensic.repository.InvestigationNoteRepository;
import com.sentinelforensic.repository.InvestigationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestigationNoteService {
    private static final Logger log = LoggerFactory.getLogger(InvestigationNoteService.class);

    private final InvestigationNoteRepository noteRepository;
    private final InvestigationRepository investigationRepository;

    public InvestigationNoteService(InvestigationNoteRepository noteRepository, InvestigationRepository investigationRepository) {
        this.noteRepository = noteRepository;
        this.investigationRepository = investigationRepository;
    }

    public List<InvestigationNoteDto> getNotes(Long investigationId) {
        if (!investigationRepository.existsById(investigationId)) {
            throw new ResourceNotFoundException("Investigation", "id", investigationId);
        }
        return noteRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvestigationNoteDto addNote(Long investigationId, CreateNoteRequest request) {
        if (!investigationRepository.existsById(investigationId)) {
            throw new ResourceNotFoundException("Investigation", "id", investigationId);
        }

        InvestigationNote note = new InvestigationNote();
        note.setInvestigationId(investigationId);
        note.setAuthor(request.getAuthor() != null && !request.getAuthor().isBlank() ? request.getAuthor() : "Naveen (Investigator)");
        note.setContent(request.getContent());
        note.setCreatedAt(LocalDateTime.now());

        InvestigationNote saved = noteRepository.save(note);
        log.info("Added note to investigation {}: author='{}'", investigationId, saved.getAuthor());
        return toDto(saved);
    }

    private InvestigationNoteDto toDto(InvestigationNote note) {
        InvestigationNoteDto dto = new InvestigationNoteDto();
        dto.setId(note.getId());
        dto.setInvestigationId(note.getInvestigationId());
        dto.setAuthor(note.getAuthor());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        return dto;
    }
}
