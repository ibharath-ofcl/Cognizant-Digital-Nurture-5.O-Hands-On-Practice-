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
class DepartmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Test
    void testCreateDepartment() throws Exception {
        Department dept = Department.builder().name("HR").build();

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("HR")));
    }

    @Test
    void testCreateDepartmentInvalid() throws Exception {
        Department dept = Department.builder().name("").build();

        mockMvc.perform(post("/api/departments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllDepartments() throws Exception {
        Department hr = departmentRepository.save(Department.builder().name("HR").build());
        Department it = departmentRepository.save(Department.builder().name("IT").build());

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", anyOf(is("HR"), is("IT"))))
                .andExpect(jsonPath("$[1].name", anyOf(is("HR"), is("IT"))));
    }

    @Test
    void testGetDepartmentById() throws Exception {
        Department hr = departmentRepository.save(Department.builder().name("HR").build());

        mockMvc.perform(get("/api/departments/" + hr.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(hr.getId().intValue())))
                .andExpect(jsonPath("$.name", is("HR")));
    }

    @Test
    void testGetDepartmentByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/departments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDepartment() throws Exception {
        Department hr = departmentRepository.save(Department.builder().name("HR").build());
        Department updatedDept = Department.builder().name("Human Resources").build();

        mockMvc.perform(put("/api/departments/" + hr.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Human Resources")));
    }

    @Test
    void testUpdateDepartmentNotFound() throws Exception {
        Department updatedDept = Department.builder().name("Human Resources").build();

        mockMvc.perform(put("/api/departments/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDept)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDepartment() throws Exception {
        Department hr = departmentRepository.save(Department.builder().name("HR").build());

        mockMvc.perform(delete("/api/departments/" + hr.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/departments/" + hr.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDepartmentNotFound() throws Exception {
        mockMvc.perform(delete("/api/departments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchDepartmentsByNameContains() throws Exception {
        departmentRepository.save(Department.builder().name("Engineering").build());

        mockMvc.perform(get("/api/departments/search/name-contains").param("keyword", "engine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Engineering")));
    }

    @Test
    void testSearchDepartmentsWithMinEmployees() throws Exception {
        Department dept = departmentRepository.save(Department.builder().name("Engineering").build());
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(dept)
                .build());

        mockMvc.perform(get("/api/departments/search/min-employees").param("minCount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Engineering")));
    }

    @Test
    void testSearchDepartmentByNameNamed() throws Exception {
        departmentRepository.save(Department.builder().name("Engineering").build());

        mockMvc.perform(get("/api/departments/search/name-named").param("name", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Engineering")));
    }
}
