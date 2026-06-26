package com.example.employeemanagementsystem.repository;

import com.example.employeemanagementsystem.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Derived query method using keyword 'Containing' to search by name partial match
    List<Employee> findByNameContaining(String namePart);

    // Derived query method using keyword 'EndingWith' to search by email domain/suffix
    List<Employee> findByEmailEndingWith(String emailSuffix);

    // Custom query using @Query (JPQL) to find employees by department name
    @Query("SELECT e FROM Employee e WHERE e.department.name = :deptName")
    List<Employee> findEmployeesByDeptName(@Param("deptName") String deptName);

    // Custom query using @Query (Native SQL) to find an employee by email
    @Query(value = "SELECT * FROM employees WHERE email = :email", nativeQuery = true)
    Optional<Employee> findByEmailNative(@Param("email") String email);

    // Execute Named Query: Employee.findByEmailNamed
    List<Employee> findByEmailNamed(@Param("email") String email);

    // Execute Named Query: Employee.findByDepartmentNamed
    List<Employee> findByDepartmentNamed(@Param("deptId") Long deptId);
}
