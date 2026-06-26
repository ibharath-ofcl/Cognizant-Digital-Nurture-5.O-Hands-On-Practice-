package com.example.employeemanagementsystem.controller;

import com.example.employeemanagementsystem.model.primary.Department;
import com.example.employeemanagementsystem.model.primary.Employee;
import com.example.employeemanagementsystem.repository.primary.DepartmentRepository;
import com.example.employeemanagementsystem.repository.primary.EmployeeRepository;
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

    @Test
    void testSearchEmployeesByName() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/name").param("keyword", "lic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void testSearchEmployeesByEmailSuffix() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/email-suffix").param("suffix", "example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void testSearchEmployeesByDeptName() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/dept-name").param("deptName", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void testSearchEmployeeByEmailNative() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/email-native").param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice")));
    }

    @Test
    void testSearchEmployeeByEmailNamed() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/email-named").param("email", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void testSearchEmployeesByDeptNamed() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/search/dept-named").param("deptId", department.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")));
    }

    @Test
    void testGetEmployeesPaginated() throws Exception {
        employeeRepository.save(Employee.builder().name("Charlie").email("charlie@example.com").department(department).build());
        employeeRepository.save(Employee.builder().name("Alice").email("alice@example.com").department(department).build());
        employeeRepository.save(Employee.builder().name("Bob").email("bob@example.com").department(department).build());

        mockMvc.perform(get("/api/employees/paginated")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Alice")))
                .andExpect(jsonPath("$.content[1].name", is("Bob")))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)));

        mockMvc.perform(get("/api/employees/paginated")
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Charlie")));
    }

    @Test
    void testSearchEmployeesByNamePaginated() throws Exception {
        employeeRepository.save(Employee.builder().name("Alice").email("alice@example.com").department(department).build());
        employeeRepository.save(Employee.builder().name("Charlie").email("charlie@example.com").department(department).build());
        employeeRepository.save(Employee.builder().name("Dave").email("dave@example.com").department(department).build());

        mockMvc.perform(get("/api/employees/search/name-paginated")
                .param("keyword", "e")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Dave")))
                .andExpect(jsonPath("$.content[1].name", is("Charlie")))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void testSearchEmployeesByDeptPaginated() throws Exception {
        Department hr = departmentRepository.save(Department.builder().name("HR").build());
        employeeRepository.save(Employee.builder().name("Alice").email("alice@example.com").department(hr).build());
        employeeRepository.save(Employee.builder().name("Dave").email("dave@example.com").department(hr).build());
        employeeRepository.save(Employee.builder().name("Bob").email("bob@example.com").department(department).build());

        mockMvc.perform(get("/api/employees/search/dept-paginated")
                .param("deptName", "HR")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Alice")))
                .andExpect(jsonPath("$.content[1].name", is("Dave")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    void testGetClosedProjections() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/projections/closed").param("deptId", department.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[0].email", is("alice@example.com")))
                .andExpect(jsonPath("$[0].department").doesNotExist());
    }

    @Test
    void testGetOpenProjections() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/projections/open").param("deptId", department.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[0].fullNameWithDept", is("Alice - Engineering")));
    }

    @Test
    void testGetDtoProjections() throws Exception {
        employeeRepository.save(Employee.builder()
                .name("Alice")
                .email("alice@example.com")
                .department(department)
                .build());

        mockMvc.perform(get("/api/employees/projections/dto").param("deptId", department.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice")))
                .andExpect(jsonPath("$[0].email", is("alice@example.com")))
                .andExpect(jsonPath("$[0].departmentName", is("Engineering")));
    }
}
