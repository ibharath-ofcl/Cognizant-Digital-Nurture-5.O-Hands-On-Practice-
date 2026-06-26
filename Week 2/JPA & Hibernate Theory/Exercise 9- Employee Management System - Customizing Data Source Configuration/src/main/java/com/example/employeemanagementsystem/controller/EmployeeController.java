package com.example.employeemanagementsystem.controller;

import com.example.employeemanagementsystem.model.primary.Department;
import com.example.employeemanagementsystem.model.primary.Employee;
import com.example.employeemanagementsystem.projection.EmployeeProjection;
import com.example.employeemanagementsystem.projection.EmployeeDetailProjection;
import com.example.employeemanagementsystem.projection.EmployeeDto;
import com.example.employeemanagementsystem.repository.primary.DepartmentRepository;
import com.example.employeemanagementsystem.repository.primary.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeController(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    // Create a new Employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        if (employee.getName() == null || employee.getName().trim().isEmpty() ||
            employee.getEmail() == null || employee.getEmail().trim().isEmpty() ||
            employee.getDepartment() == null || employee.getDepartment().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate department existence
        Optional<Department> department = departmentRepository.findById(employee.getDepartment().getId());
        if (department.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        employee.setDepartment(department.get());

        // Validate email uniqueness
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    // Get all Employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get paginated and sorted Employees
    @GetMapping("/paginated")
    public Page<Employee> getEmployeesPaginated(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    // Get Employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update an existing Employee
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        if (employeeDetails.getName() == null || employeeDetails.getName().trim().isEmpty() ||
            employeeDetails.getEmail() == null || employeeDetails.getEmail().trim().isEmpty() ||
            employeeDetails.getDepartment() == null || employeeDetails.getDepartment().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        return employeeRepository.findById(id)
                .map(employee -> {
                    // Validate department existence
                    Optional<Department> department = departmentRepository.findById(employeeDetails.getDepartment().getId());
                    if (department.isEmpty()) {
                        return new ResponseEntity<Employee>(HttpStatus.BAD_REQUEST);
                    }

                    // Validate email uniqueness
                    Optional<Employee> existingEmailEmp = employeeRepository.findByEmail(employeeDetails.getEmail());
                    if (existingEmailEmp.isPresent() && !existingEmailEmp.get().getId().equals(id)) {
                        return new ResponseEntity<Employee>(HttpStatus.CONFLICT);
                    }

                    employee.setName(employeeDetails.getName());
                    employee.setEmail(employeeDetails.getEmail());
                    employee.setDepartment(department.get());

                    Employee updatedEmployee = employeeRepository.save(employee);
                    return ResponseEntity.ok(updatedEmployee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete an Employee
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    employeeRepository.delete(employee);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Search employees by name containing keyword
    @GetMapping("/search/name")
    public List<Employee> searchEmployeesByName(@RequestParam String keyword) {
        return employeeRepository.findByNameContaining(keyword);
    }

    // Search employees by name containing keyword with pagination and sorting
    @GetMapping("/search/name-paginated")
    public Page<Employee> searchEmployeesByNamePaginated(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return employeeRepository.findByNameContaining(keyword, pageable);
    }

    // Search employees by department name with pagination and sorting
    @GetMapping("/search/dept-paginated")
    public Page<Employee> searchEmployeesByDeptNamePaginated(
            @RequestParam String deptName,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return employeeRepository.findByDepartmentName(deptName, pageable);
    }

    // Search employees by email suffix
    @GetMapping("/search/email-suffix")
    public List<Employee> searchEmployeesByEmailSuffix(@RequestParam String suffix) {
        return employeeRepository.findByEmailEndingWith(suffix);
    }

    // Search employees by department name (JPQL)
    @GetMapping("/search/dept-name")
    public List<Employee> searchEmployeesByDeptName(@RequestParam String deptName) {
        return employeeRepository.findEmployeesByDeptName(deptName);
    }

    // Search employee by email (Native Query)
    @GetMapping("/search/email-native")
    public ResponseEntity<Employee> searchEmployeeByEmailNative(@RequestParam String email) {
        return employeeRepository.findByEmailNative(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Search employee by email using Named Query
    @GetMapping("/search/email-named")
    public List<Employee> searchEmployeeByEmailNamed(@RequestParam String email) {
        return employeeRepository.findByEmailNamed(email);
    }

    // Search employees by department ID using Named Query
    @GetMapping("/search/dept-named")
    public List<Employee> searchEmployeesByDeptNamed(@RequestParam Long deptId) {
        return employeeRepository.findByDepartmentNamed(deptId);
    }

    // Get closed projections of employees for a department
    @GetMapping("/projections/closed")
    public List<EmployeeProjection> getClosedProjections(@RequestParam Long deptId) {
        return employeeRepository.findProjectedByDepartmentId(deptId);
    }

    // Get open projections of employees for a department
    @GetMapping("/projections/open")
    public List<EmployeeDetailProjection> getOpenProjections(@RequestParam Long deptId) {
        return employeeRepository.findDetailedProjectedByDepartmentId(deptId);
    }

    // Get class-based DTO projections of employees for a department
    @GetMapping("/projections/dto")
    public List<EmployeeDto> getDtoProjections(@RequestParam Long deptId) {
        return employeeRepository.findEmployeeDtosByDepartmentId(deptId);
    }
}
