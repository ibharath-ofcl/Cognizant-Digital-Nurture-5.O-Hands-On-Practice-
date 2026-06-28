package com.cognizant.springlearn.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Department {

    private static final Logger LOGGER = LoggerFactory.getLogger(Department.class);

    private int id;
    private String name;

    public Department() {
        LOGGER.debug("Inside Department Constructor.");
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

    @Override
    public String toString() {
        return "Department [id=" + id + ", name=" + name + "]";
    }
}
