package com.resumeats.service;

import com.resumeats.model.Resume;
import com.resumeats.repository.ResumeRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AIService aiService;

    public ResumeService(ResumeRepository resumeRepository, AIService aiService) {
        this.resumeRepository = resumeRepository;
        this.aiService = aiService;
    }

    public Resume processAndSaveResume(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("resume", file.getOriginalFilename());
        file.transferTo(tempFile);

        String extractedText;
        try (PDDocument document = PDDocument.load(tempFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractedText = pdfStripper.getText(document);
        }

        tempFile.delete(); // clean up

        // 🔍 Analyze using AI
        String aiAnalysis = aiService.analyzeResume(extractedText);
        System.out.println("✅ Raw AI Analysis:\n" + aiAnalysis);

        int score = extractScoreFromAnalysis(aiAnalysis);
        String suggestions = extractSuggestionsFromAnalysis(aiAnalysis);

        System.out.println("🎯 Extracted Score: " + score);
        System.out.println("📌 Extracted Suggestions:\n" + suggestions);

        Resume resume = new Resume(null, file.getOriginalFilename(), extractedText, score, suggestions);
        return resumeRepository.save(resume);
    }

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public String extractText(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("resume", file.getOriginalFilename());
        file.transferTo(tempFile);

        String text;
        try (PDDocument document = PDDocument.load(tempFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            text = pdfStripper.getText(document);
        }

        tempFile.delete();
        return text;
    }

    private int extractScoreFromAnalysis(String aiAnalysis) {
        Pattern pattern = Pattern.compile("(?i)score[:\\-]?\\s*(\\d{1,3})\\s*/\\s*100");
        Matcher matcher = pattern.matcher(aiAnalysis);

        if (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            return (value <= 100) ? value : 0;
        }

        return 0;
    }

    private String extractSuggestionsFromAnalysis(String aiAnalysis) {
        int resumeIndex = aiAnalysis.lastIndexOf("Resume:");
        if (resumeIndex == -1) {
            return "⚠️ Could not find 'Resume:' in the AI response.";
        }
    
        String afterResume = aiAnalysis.substring(resumeIndex);
    
        // Extract suggestions block using better regex
        Pattern pattern = Pattern.compile("Suggestions:\\s*((?:-\\s+.*(?:\\r?\\n|$))+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(afterResume);
    
        if (matcher.find()) {
            String suggestionsBlock = matcher.group(1).trim();
    
            // Return clean suggestions
            return suggestionsBlock;
        } else {
            return "⚠️ Could not extract suggestions from the AI response.";
        }
    }
    
    
    

    
    
}
