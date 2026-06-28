package com.cognizant.springlearn.dao;

import java.util.ArrayList;
import java.util.List;
import com.cognizant.springlearn.model.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Repository;

@Repository
public class DepartmentDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentDao.class);

    private static ArrayList<Department> DEPARTMENT_LIST;

    @SuppressWarnings("unchecked")
    public DepartmentDao() {
        LOGGER.info("START");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("employee.xml");
        try {
            DEPARTMENT_LIST = (ArrayList<Department>) context.getBean("departmentList");
            LOGGER.info("Loaded {} departments.", DEPARTMENT_LIST.size());
        } finally {
            context.close();
        }
        LOGGER.info("END");
    }

    public List<Department> getAllDepartments() {
        return DEPARTMENT_LIST;
    }
}
