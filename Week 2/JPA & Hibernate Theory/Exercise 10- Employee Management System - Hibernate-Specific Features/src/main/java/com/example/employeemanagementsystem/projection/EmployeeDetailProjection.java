package com.example.employeemanagementsystem.projection;

import org.springframework.beans.factory.annotation.Value;

public interface EmployeeDetailProjection {
    Long getId();
    String getName();
    String getEmail();

    @Value("#{target.name + ' - ' + target.department.name}")
    String getFullNameWithDept();
}
