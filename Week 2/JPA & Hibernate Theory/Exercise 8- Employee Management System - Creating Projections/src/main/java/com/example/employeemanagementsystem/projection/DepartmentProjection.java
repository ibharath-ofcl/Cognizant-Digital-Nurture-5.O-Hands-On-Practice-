package com.example.employeemanagementsystem.projection;

import org.springframework.beans.factory.annotation.Value;

public interface DepartmentProjection {
    Long getId();
    String getName();

    @Value("#{target.employees.size()}")
    Integer getEmployeeCount();
}
