package com.example.employeemanagementsystem.service;

import com.example.employeemanagementsystem.model.Department;
import com.example.employeemanagementsystem.model.Employee;
import com.example.employeemanagementsystem.repository.DepartmentRepository;
import com.example.employeemanagementsystem.repository.EmployeeRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BatchProcessingTests {

    @Autowired
    private EmployeeBatchService employeeBatchService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Department engineering;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        engineering = Department.builder()
                .name("Engineering")
                .build();
        departmentRepository.save(engineering);
    }

    @Test
    void testBatchInsertPerformanceAndFormula() {
        // Create 100 employees
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            employees.add(Employee.builder()
                    .name("Employee " + i)
                    .email("employee" + i + "@example.com")
                    .department(engineering)
                    .build());
        }

        // Get session factory and clear stats
        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        // Perform batch insert
        employeeBatchService.saveEmployeesInBatch(employees);

        // Verify count
        long count = employeeRepository.count();
        assertThat(count).isEqualTo(100);

        // Verify statistics
        System.out.println("--- HIBERNATE STATISTICS FOR BATCH INSERT ---");
        System.out.println("Entity insert count: " + statistics.getEntityInsertCount());
        System.out.println("Prepare statement count: " + statistics.getPrepareStatementCount());
        System.out.println("Flush count: " + statistics.getFlushCount());
        
        // Assert entities inserted and flushed in batches
        assertThat(statistics.getEntityInsertCount()).isEqualTo(100);
        assertThat(statistics.getFlushCount()).isEqualTo(5);

        // Verify Formula annotation works
        List<Employee> savedEmployees = employeeRepository.findAll();
        assertThat(savedEmployees).isNotEmpty();
        Employee first = savedEmployees.get(0);
        assertThat(first.getDisplayName()).isEqualTo(first.getName() + " (" + first.getEmail() + ")");
    }

    @Test
    void testBatchUpdatePerformance() {
        // First, insert 50 employees
        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            employees.add(Employee.builder()
                    .name("Employee Update " + i)
                    .email("employee.update" + i + "@example.com")
                    .department(engineering)
                    .build());
        }
        employeeBatchService.saveEmployeesInBatch(employees);

        // Clear stats
        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        // Retrieve and update them
        List<Employee> savedEmployees = employeeRepository.findAll();
        for (int i = 0; i < savedEmployees.size(); i++) {
            savedEmployees.get(i).setName("Updated Name " + i);
        }

        // Perform batch update
        employeeBatchService.updateEmployeesInBatch(savedEmployees);

        // Verify updates
        List<Employee> updatedEmployees = employeeRepository.findAll();
        assertThat(updatedEmployees).hasSize(50);
        assertThat(updatedEmployees.get(0).getName()).startsWith("Updated Name");

        // Verify statistics
        System.out.println("--- HIBERNATE STATISTICS FOR BATCH UPDATE ---");
        System.out.println("Entity update count: " + statistics.getEntityUpdateCount());
        System.out.println("Prepare statement count: " + statistics.getPrepareStatementCount());
        
        // Assert entities updated and flushed in batches
        assertThat(statistics.getEntityUpdateCount()).isEqualTo(50);
        assertThat(statistics.getFlushCount()).isEqualTo(3);
    }

    @Test
    @Transactional
    void testDepartmentEmployeesBatchSize() {
        // Create 5 departments
        List<Department> departments = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Department dept = Department.builder().name("Dept " + i).build();
            departments.add(dept);
        }
        departmentRepository.saveAll(departments);

        // Add 1 employee to each department
        for (int i = 0; i < 5; i++) {
            Employee emp = Employee.builder()
                    .name("Emp " + i)
                    .email("emp" + i + "@example.com")
                    .department(departments.get(i))
                    .build();
            employeeRepository.save(emp);
        }

        entityManager.flush();
        entityManager.clear();

        // Retrieve all departments
        List<Department> retrieved = departmentRepository.findAll();

        Session session = entityManager.unwrap(Session.class);
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        // Access the employees collection for all retrieved departments
        // This will trigger collection initialization
        for (Department dept : retrieved) {
            dept.getEmployees().size();
        }

        System.out.println("--- HIBERNATE STATISTICS FOR BATCH FETCHING ---");
        System.out.println("Fetch count: " + statistics.getPrepareStatementCount());
        
        // Assert that we did not execute a query for each department (N+1 selects).
        // Since we have 5 departments, N+1 would be 5 queries.
        // With BatchSize, it should be 1 query to fetch the collections.
        assertThat(statistics.getPrepareStatementCount()).isLessThan(5);
    }
}
