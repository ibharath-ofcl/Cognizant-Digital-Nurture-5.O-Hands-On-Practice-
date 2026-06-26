package com.example.employeemanagementsystem.controller;

import com.example.employeemanagementsystem.model.primary.Department;
import com.example.employeemanagementsystem.projection.DepartmentProjection;
import com.example.employeemanagementsystem.repository.primary.DepartmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Create a new Department
    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) {
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Department savedDepartment = departmentRepository.save(department);
        return new ResponseEntity<>(savedDepartment, HttpStatus.CREATED);
    }

    // Get all Departments
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Get Department by ID
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update an existing Department
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        if (departmentDetails.getName() == null || departmentDetails.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return departmentRepository.findById(id)
                .map(department -> {
                    department.setName(departmentDetails.getName());
                    Department updatedDepartment = departmentRepository.save(department);
                    return ResponseEntity.ok(updatedDepartment);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a Department
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    departmentRepository.delete(department);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Search departments by name containing keyword (case-insensitive)
    @GetMapping("/search/name-contains")
    public List<Department> searchDepartmentsByNameContains(@RequestParam String keyword) {
        return departmentRepository.findByNameContainingIgnoreCase(keyword);
    }

    // Search departments with at least minCount employees
    @GetMapping("/search/min-employees")
    public List<Department> searchDepartmentsWithMinEmployees(@RequestParam int minCount) {
        return departmentRepository.findDepartmentsWithMinEmployees(minCount);
    }

    // Search department by name using Named Query
    @GetMapping("/search/name-named")
    public ResponseEntity<Department> searchDepartmentByNameNamed(@RequestParam String name) {
        return departmentRepository.findByNameNamed(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get projections of departments by name
    @GetMapping("/projections")
    public List<DepartmentProjection> getDepartmentProjections(@RequestParam String name) {
        return departmentRepository.findProjectedByName(name);
    }
}
