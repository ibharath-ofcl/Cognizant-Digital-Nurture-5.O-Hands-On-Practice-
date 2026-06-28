package com.cognizant.springlearn.dao;

import java.util.ArrayList;
import java.util.List;
import com.cognizant.springlearn.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeDao.class);
    
    private static ArrayList<Employee> EMPLOYEE_LIST;

    @SuppressWarnings("unchecked")
    public EmployeeDao() {
        LOGGER.info("START");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("employee.xml");
        try {
            EMPLOYEE_LIST = (ArrayList<Employee>) context.getBean("employeeList");
            LOGGER.info("Loaded {} employees.", EMPLOYEE_LIST.size());
        } finally {
            context.close();
        }
        LOGGER.info("END");
    }

    public List<Employee> getAllEmployees() {
        return EMPLOYEE_LIST;
    }
}
