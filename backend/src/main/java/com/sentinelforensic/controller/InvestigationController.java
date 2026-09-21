package com.sentinelforensic.controller;

import com.sentinelforensic.dto.*;
import com.sentinelforensic.model.InvestigationStatus;
import com.sentinelforensic.service.InvestigationNoteService;
import com.sentinelforensic.service.InvestigationService;
import com.sentinelforensic.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investigations")
public class InvestigationController {

    private final InvestigationService investigationService;
    private final InvestigationNoteService investigationNoteService;
    private final ReportService reportService;

    public InvestigationController(
            InvestigationService investigationService,
            InvestigationNoteService investigationNoteService,
            ReportService reportService) {
        this.investigationService = investigationService;
        this.investigationNoteService = investigationNoteService;
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<InvestigationResponse>> getAllInvestigations(
            @RequestParam(required = false) String search) {
        List<InvestigationResponse> list = investigationService.getAllInvestigations(search);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<InvestigationResponse> createInvestigation(
            @Valid @RequestBody CreateInvestigationRequest request) {
        InvestigationResponse response = investigationService.createInvestigation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestigationResponse> getInvestigationById(@PathVariable Long id) {
        InvestigationResponse response = investigationService.getInvestigationById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InvestigationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }
        InvestigationStatus status = InvestigationStatus.valueOf(statusStr.toUpperCase());
        InvestigationResponse response = investigationService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestigation(@PathVariable Long id) {
        investigationService.deleteInvestigation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getReportPdf(@PathVariable Long id) {
        byte[] pdfBytes = reportService.generatePdfReport(id);
        InvestigationResponse inv = investigationService.getInvestigationById(id);

        String filename = String.format("SentinelForensic_Report_%s.pdf", inv.getInvestigationId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<List<InvestigationNoteDto>> getNotes(@PathVariable Long id) {
        List<InvestigationNoteDto> notes = investigationNoteService.getNotes(id);
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<InvestigationNoteDto> addNote(
            @PathVariable Long id,
            @Valid @RequestBody CreateNoteRequest request) {
        InvestigationNoteDto note = investigationNoteService.addNote(id, request);
        return new ResponseEntity<>(note, HttpStatus.CREATED);
    }
}