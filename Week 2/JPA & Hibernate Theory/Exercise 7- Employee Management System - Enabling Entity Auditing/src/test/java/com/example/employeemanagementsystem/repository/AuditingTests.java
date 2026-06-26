package com.example.employeemanagementsystem.repository;

import com.example.employeemanagementsystem.config.AuditConfig;
import com.example.employeemanagementsystem.model.Department;
import com.example.employeemanagementsystem.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AuditConfig.class)
class AuditingTests {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void testDepartmentAuditing() throws InterruptedException {
        Department department = Department.builder()
                .name("HR")
                .build();

        // 1. Create and Save
        Department saved = departmentRepository.save(department);

        assertThat(saved.getCreatedBy()).isEqualTo("Admin");
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getLastModifiedBy()).isEqualTo("Admin");
        assertThat(saved.getLastModifiedDate()).isNotNull();
        assertThat(saved.getCreatedDate()).isEqualTo(saved.getLastModifiedDate());

        LocalDateTime originalCreatedDate = saved.getCreatedDate();

        Thread.sleep(10);

        // 2. Update
        saved.setName("Human Resources");
        Department updated = departmentRepository.saveAndFlush(saved);

        assertThat(updated.getCreatedBy()).isEqualTo("Admin");
        assertThat(updated.getCreatedDate()).isEqualTo(originalCreatedDate);
        assertThat(updated.getLastModifiedBy()).isEqualTo("Admin");
        assertThat(updated.getLastModifiedDate()).isAfterOrEqualTo(originalCreatedDate);
    }

    @Test
    void testEmployeeAuditing() {
        Department department = departmentRepository.save(Department.builder().name("IT").build());

        Employee employee = Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build();

        Employee saved = employeeRepository.save(employee);

        assertThat(saved.getCreatedBy()).isEqualTo("Admin");
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getLastModifiedBy()).isEqualTo("Admin");
        assertThat(saved.getLastModifiedDate()).isNotNull();
    }
}
