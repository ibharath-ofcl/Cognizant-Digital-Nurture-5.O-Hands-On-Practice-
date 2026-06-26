package com.example.employeemanagementsystem.controller;

import com.example.employeemanagementsystem.model.Department;
import com.example.employeemanagementsystem.model.Employee;
import com.example.employeemanagementsystem.repository.DepartmentRepository;
import com.example.employeemanagementsystem.repository.EmployeeRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Department department;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        department = departmentRepository.save(Department.builder().name("Engineering").build());
    }

    @Test
    void testCreateEmployee() throws Exception {
        Employee employee = Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.department.id", is(department.getId().intValue())));
    }

    @Test
    void testCreateEmployeeInvalidDepartment() throws Exception {
        Department nonExistentDept = Department.builder().id(999L).name("None").build();
        Employee employee = Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(nonExistentDept)
                .build();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployeeDuplicateEmail() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());

        Employee employee = Employee.builder()
                .name("Jane Smith")
                .email("john.doe@example.com")
                .department(department)
                .build();

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetAllEmployees() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());
        employeeRepository.save(Employee.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", anyOf(is("John Doe"), is("Jane Smith"))))
                .andExpect(jsonPath("$[1].name", anyOf(is("John Doe"), is("Jane Smith"))));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        Employee emp = employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(emp.getId().intValue())))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void testGetEmployeeByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateEmployee() throws Exception {
        Employee emp = employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());

        Employee updatedDetails = Employee.builder()
                .name("John Smith")
                .email("john.smith@example.com")
                .department(department)
                .build();

        mockMvc.perform(put("/api/employees/" + emp.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Smith")))
                .andExpect(jsonPath("$.email", is("john.smith@example.com")));
    }

    @Test
    void testUpdateEmployeeDuplicateEmail() throws Exception {
        Employee emp1 = employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());
        Employee emp2 = employeeRepository.save(Employee.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .department(department)
                .build());

        Employee updatedDetails = Employee.builder()
                .name("John Doe")
                .email("jane.smith@example.com")
                .department(department)
                .build();

        mockMvc.perform(put("/api/employees/" + emp1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isConflict());
    }

    @Test
    void testDeleteEmployee() throws Exception {
        Employee emp = employeeRepository.save(Employee.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .department(department)
                .build());

        mockMvc.perform(delete("/api/employees/" + emp.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + emp.getId()))
                .andExpect(status().isNotFound());
    }
}
