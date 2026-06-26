package com.example.employeemanagementsystem.repository.secondary;

import com.example.employeemanagementsystem.model.secondary.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByNameContainingIgnoreCase(String namePart);
}
