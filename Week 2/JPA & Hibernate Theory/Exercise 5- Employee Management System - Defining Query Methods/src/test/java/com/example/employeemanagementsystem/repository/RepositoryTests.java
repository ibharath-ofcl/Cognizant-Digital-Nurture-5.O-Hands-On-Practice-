package com.example.employeemanagementsystem.repository;

import com.example.employeemanagementsystem.model.Department;
import com.example.employeemanagementsystem.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryTests {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Department hrDepartment;
    private Department itDepartment;
    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        // Setup Departments
        hrDepartment = Department.builder()
                .name("HR")
                .build();
        itDepartment = Department.builder()
                .name("IT")
                .build();

        departmentRepository.save(hrDepartment);
        departmentRepository.save(itDepartment);

        // Setup Employees
        alice = Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(hrDepartment)
                .build();
        bob = Employee.builder()
                .name("Bob")
                .email("bob@example.com")
                .department(itDepartment)
                .build();

        employeeRepository.save(alice);
        employeeRepository.save(bob);
    }

    @Test
    void testCreateAndFindDepartment() {
        Department finance = Department.builder()
                .name("Finance")
                .build();
        Department saved = departmentRepository.save(finance);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Finance");

        Optional<Department> found = departmentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Finance");
    }

    @Test
    void testFindDepartmentByName() {
        Optional<Department> found = departmentRepository.findByName("HR");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("HR");

        Optional<Department> notFound = departmentRepository.findByName("NonExistent");
        assertThat(notFound).isEmpty();
    }

    @Test
    void testFindEmployeeByEmail() {
        Optional<Employee> found = employeeRepository.findByEmail("alice@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");

        Optional<Employee> notFound = employeeRepository.findByEmail("nonexistent@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    void testFindEmployeesByDepartmentName() {
        List<Employee> hrEmployees = employeeRepository.findByDepartmentName("HR");
        assertThat(hrEmployees).hasSize(1);
        assertThat(hrEmployees.get(0).getName()).isEqualTo("Alice");

        List<Employee> itEmployees = employeeRepository.findByDepartmentName("IT");
        assertThat(itEmployees).hasSize(1);
        assertThat(itEmployees.get(0).getName()).isEqualTo("Bob");
    }

    @Test
    void testFindEmployeesByName() {
        List<Employee> found = employeeRepository.findByName("Alice");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testFindEmployeesByDepartmentId() {
        List<Employee> hrEmployees = employeeRepository.findByDepartmentId(hrDepartment.getId());
        assertThat(hrEmployees).hasSize(1);
        assertThat(hrEmployees.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testUpdateEmployee() {
        alice.setName("Alice Cooper");
        Employee updated = employeeRepository.save(alice);
        assertThat(updated.getName()).isEqualTo("Alice Cooper");

        Optional<Employee> found = employeeRepository.findById(alice.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Cooper");
    }

    @Test
    void testDeleteEmployee() {
        employeeRepository.delete(alice);
        Optional<Employee> found = employeeRepository.findById(alice.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByNameContaining() {
        List<Employee> results = employeeRepository.findByNameContaining("li");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testFindByEmailEndingWith() {
        List<Employee> results = employeeRepository.findByEmailEndingWith("example.com");
        assertThat(results).hasSize(2);
    }

    @Test
    void testFindEmployeesByDeptName() {
        List<Employee> results = employeeRepository.findEmployeesByDeptName("HR");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testFindByEmailNative() {
        Optional<Employee> result = employeeRepository.findByEmailNative("bob@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Bob");
    }

    @Test
    void testFindByEmailNamed() {
        List<Employee> results = employeeRepository.findByEmailNamed("alice@example.com");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testFindByDepartmentNamed() {
        List<Employee> results = employeeRepository.findByDepartmentNamed(hrDepartment.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Department> results = departmentRepository.findByNameContainingIgnoreCase("it");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("IT");
    }

    @Test
    void testFindDepartmentsWithMinEmployees() {
        List<Department> results = departmentRepository.findDepartmentsWithMinEmployees(1);
        assertThat(results).hasSize(2);

        List<Department> noResults = departmentRepository.findDepartmentsWithMinEmployees(2);
        assertThat(noResults).isEmpty();
    }

    @Test
    void testFindDepartmentByNameNamed() {
        Optional<Department> result = departmentRepository.findByNameNamed("HR");
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("HR");
    }
}
