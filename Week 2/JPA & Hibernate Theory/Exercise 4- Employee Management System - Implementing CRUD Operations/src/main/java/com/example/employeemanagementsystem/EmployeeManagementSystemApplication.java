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

			System.out.println("\n-------------------------------\n");
		};
	}
}
