package com.resumeats.repository;

import com.resumeats.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    //List<Resume> findByUserId(Long id);  // Fetch all resumes for a specific user
}
