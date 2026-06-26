package com.example.employeemanagementsystem;

import com.example.employeemanagementsystem.model.Department;
import com.example.employeemanagementsystem.model.Employee;
import com.example.employeemanagementsystem.repository.DepartmentRepository;
import com.example.employeemanagementsystem.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmployeeManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeManagementSystemApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
		return args -> {
			if (departmentRepository.count() > 0) {
				System.out.println("Database already populated. Skipping demo data insertion.");
				return;
			}

			System.out.println("\n--- POPULATING DATA ---");
			
			// Save departments
			Department sales = Department.builder().name("Sales").build();
			Department engineering = Department.builder().name("Engineering").build();
			
			departmentRepository.save(sales);
			departmentRepository.save(engineering);

			// Save employees
			Employee john = Employee.builder()
					.name("John Doe")
					.email("john.doe@example.com")
					.department(engineering)
					.build();
			Employee jane = Employee.builder()
					.name("Jane Smith")
					.email("jane.smith@example.com")
					.department(sales)
					.build();
			Employee jack = Employee.builder()
					.name("Jack Ryan")
					.email("jack.ryan@example.com")
					.department(engineering)
					.build();

			employeeRepository.save(john);
			employeeRepository.save(jane);
			employeeRepository.save(jack);

			System.out.println("Data saved successfully!");

			System.out.println("\n--- RUNNING DERIVED QUERIES ---");

			// 1. Find department by name
			System.out.println("Finding department by name 'Engineering':");
			departmentRepository.findByName("Engineering")
					.ifPresent(dept -> System.out.println("Found: " + dept.getName() + " (ID: " + dept.getId() + ")"));

			// 2. Find employee by email
			System.out.println("\nFinding employee by email 'john.doe@example.com':");
			employeeRepository.findByEmail("john.doe@example.com")
					.ifPresent(emp -> System.out.println("Found: " + emp.getName() + ", Department: " + emp.getDepartment().getName()));

			// 3. Find employees by department name
			System.out.println("\nFinding employees in 'Engineering' department:");
			employeeRepository.findByDepartmentName("Engineering")
					.forEach(emp -> System.out.println("- " + emp.getName() + " (" + emp.getEmail() + ")"));

			// 4. Find employees by name
			System.out.println("\nFinding employees with name 'Jane Smith':");
			employeeRepository.findByName("Jane Smith")
					.forEach(emp -> System.out.println("- Found: " + emp.getName() + " (" + emp.getEmail() + ")"));

			System.out.println("\n--- RUNNING EXERCISE 5 CUSTOM QUERIES ---");

			// 5. Derived: findByNameContaining
			System.out.println("\n1. Derived findByNameContaining('Jack'):");
			employeeRepository.findByNameContaining("Jack")
					.forEach(emp -> System.out.println("- Found: " + emp.getName()));

			// 6. Derived: findByEmailEndingWith
			System.out.println("\n2. Derived findByEmailEndingWith('example.com'):");
			employeeRepository.findByEmailEndingWith("example.com")
					.forEach(emp -> System.out.println("- Found: " + emp.getName() + " (" + emp.getEmail() + ")"));

			// 7. @Query JPQL: findEmployeesByDeptName
			System.out.println("\n3. JPQL @Query findEmployeesByDeptName('Engineering'):");
			employeeRepository.findEmployeesByDeptName("Engineering")
					.forEach(emp -> System.out.println("- Found: " + emp.getName()));

			// 8. @Query Native SQL: findByEmailNative
			System.out.println("\n4. Native SQL @Query findByEmailNative('john.doe@example.com'):");
			employeeRepository.findByEmailNative("john.doe@example.com")
					.ifPresent(emp -> System.out.println("- Found: " + emp.getName()));

			// 9. Named Query: findByEmailNamed
			System.out.println("\n5. Named Query findByEmailNamed('jack.ryan@example.com'):");
			employeeRepository.findByEmailNamed("jack.ryan@example.com")
					.forEach(emp -> System.out.println("- Found: " + emp.getName()));

			// 10. Named Query: findByDepartmentNamed
			System.out.println("\n6. Named Query findByDepartmentNamed(Engineering ID):");
			employeeRepository.findByDepartmentNamed(engineering.getId())
					.forEach(emp -> System.out.println("- Found: " + emp.getName()));

			// 11. Department Derived: findByNameContainingIgnoreCase
			System.out.println("\n7. Department Derived findByNameContainingIgnoreCase('sal'):");
			departmentRepository.findByNameContainingIgnoreCase("sal")
					.forEach(dept -> System.out.println("- Found Department: " + dept.getName()));

			// 12. Department @Query: findDepartmentsWithMinEmployees
			System.out.println("\n8. Department @Query findDepartmentsWithMinEmployees(2) (Engineering has 2, Sales has 1):");
			departmentRepository.findDepartmentsWithMinEmployees(2)
					.forEach(dept -> System.out.println("- Found Department with min 2 employees: " + dept.getName()));

			// 13. Department Named Query: findByNameNamed
			System.out.println("\n9. Department Named Query findByNameNamed('Sales'):");
			departmentRepository.findByNameNamed("Sales")
					.ifPresent(dept -> System.out.println("- Found Department: " + dept.getName()));

			System.out.println("\n-------------------------------\n");
		};
	}
}
