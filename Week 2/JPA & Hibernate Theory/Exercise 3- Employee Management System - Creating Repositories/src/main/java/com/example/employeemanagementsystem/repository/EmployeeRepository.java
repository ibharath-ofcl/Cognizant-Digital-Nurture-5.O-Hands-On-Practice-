package com.example.employeemanagementsystem.repository;

import com.example.employeemanagementsystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Derived query method to find an employee by their email
    Optional<Employee> findByEmail(String email);

    // Derived query method to find all employees belonging to a department with a given name
    List<Employee> findByDepartmentName(String departmentName);

    // Derived query method to find all employees with a given name
    List<Employee> findByName(String name);

    // Derived query method to find all employees belonging to a department id
    List<Employee> findByDepartmentId(Long departmentId);
}
