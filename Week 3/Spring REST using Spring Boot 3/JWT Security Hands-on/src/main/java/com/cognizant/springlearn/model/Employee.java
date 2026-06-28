package com.cognizant.springlearn.model;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Employee {

    private static final Logger LOGGER = LoggerFactory.getLogger(Employee.class);

    private int id;
    private String name;
    private double salary;
    private boolean permanent;
    private Date dateOfBirth;
    private Department department;
    private List<Skill> skills;

    public Employee() {
        LOGGER.debug("Inside Employee Constructor.");
    }

    public int getId() {
        LOGGER.debug("Inside getId. Id: {}", id);
        return id;
    }

    public void setId(int id) {
        LOGGER.debug("Inside setId. Setting id to: {}", id);
        this.id = id;
    }

    public String getName() {
        LOGGER.debug("Inside getName. Name: {}", name);
        return name;
    }

    public void setName(String name) {
        LOGGER.debug("Inside setName. Setting name to: {}", name);
        this.name = name;
    }

    public double getSalary() {
        LOGGER.debug("Inside getSalary. Salary: {}", salary);
        return salary;
    }

    public void setSalary(double salary) {
        LOGGER.debug("Inside setSalary. Setting salary to: {}", salary);
        this.salary = salary;
    }

    public boolean isPermanent() {
        LOGGER.debug("Inside isPermanent. Permanent: {}", permanent);
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        LOGGER.debug("Inside setPermanent. Setting permanent to: {}", permanent);
        this.permanent = permanent;
    }

    public Date getDateOfBirth() {
        LOGGER.debug("Inside getDateOfBirth. DateOfBirth: {}", dateOfBirth);
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        LOGGER.debug("Inside setDateOfBirth. Setting dateOfBirth to: {}", dateOfBirth);
        this.dateOfBirth = dateOfBirth;
    }

    public Department getDepartment() {
        LOGGER.debug("Inside getDepartment. Department: {}", department);
        return department;
    }

    public void setDepartment(Department department) {
        LOGGER.debug("Inside setDepartment. Setting department to: {}", department);
        this.department = department;
    }

    public List<Skill> getSkills() {
        LOGGER.debug("Inside getSkills. Skills: {}", skills);
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        LOGGER.debug("Inside setSkills. Setting skills to: {}", skills);
        this.skills = skills;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + 
                ", permanent=" + permanent + ", dateOfBirth=" + dateOfBirth + 
                ", department=" + department + ", skills=" + skills + "]";
    }
}
