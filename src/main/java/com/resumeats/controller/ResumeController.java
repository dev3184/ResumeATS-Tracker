package com.resumeats.controller;

import com.resumeats.model.Resume;
import com.resumeats.service.ResumeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@Tag(name = "Resume API", description = "Handles resume upload, analysis and suggestions")
public class ResumeController {

    private final ResumeService resumeService;

    @Autowired
    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // ✅ Upload Resume (No userId required)
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            Resume savedResume = resumeService.processAndSaveResume(file);
            return ResponseEntity.ok(savedResume);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing resume: " + e.getMessage());
        }
    }

    // ✅ Get all resumes (Optional)
    @GetMapping
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    // ✅ Fetch a specific resume by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getResumeById(@PathVariable Long id) {
        return resumeService.getResumeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Fetch AI analysis of a specific resume
    @GetMapping("/{id}/analysis")
    @Operation(summary = "Analyze resume", description = "Accepts a resume and returns score with suggestions")
    public ResponseEntity<?> getResumeAnalysis(@PathVariable Long id) {
        System.out.println("🔍 Request received for resume analysis: ID = " + id);
        return resumeService.getResumeById(id)
                .map(resume -> ResponseEntity.ok(resume.getSuggestions()))
                .orElse(ResponseEntity.notFound().build());
    }
}
