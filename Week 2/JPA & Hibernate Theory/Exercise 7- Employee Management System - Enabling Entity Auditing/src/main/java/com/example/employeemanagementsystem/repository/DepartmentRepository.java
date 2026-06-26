package com.example.employeemanagementsystem.repository;

import com.example.employeemanagementsystem.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Derived query method to find a department by its name
    Optional<Department> findByName(String name);

    // Derived query method with 'Containing' and 'IgnoreCase' keywords
    List<Department> findByNameContainingIgnoreCase(String keyword);

    // Custom query using @Query to find departments with at least minCount employees
    @Query("SELECT d FROM Department d WHERE SIZE(d.employees) >= :minCount")
    List<Department> findDepartmentsWithMinEmployees(@Param("minCount") int minCount);

    // Execute Named Query: Department.findByNameNamed
    Optional<Department> findByNameNamed(@Param("name") String name);
}
