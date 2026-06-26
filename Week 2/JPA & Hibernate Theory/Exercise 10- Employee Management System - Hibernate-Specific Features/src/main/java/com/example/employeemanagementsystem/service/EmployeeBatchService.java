package com.example.employeemanagementsystem.service;

import com.example.employeemanagementsystem.model.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeBatchService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Bulk save employees in batches.
     * Flushes and clears the persistence context periodically to free memory.
     */
    @Transactional
    public void saveEmployeesInBatch(List<Employee> employees) {
        int batchSize = 20; // Matches hibernate.jdbc.batch_size
        for (int i = 0; i < employees.size(); i++) {
            entityManager.persist(employees.get(i));
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Bulk update employees in batches.
     * Flushes and clears the persistence context periodically to free memory.
     */
    @Transactional
    public void updateEmployeesInBatch(List<Employee> employees) {
        int batchSize = 20; // Matches hibernate.jdbc.batch_size
        for (int i = 0; i < employees.size(); i++) {
            entityManager.merge(employees.get(i));
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}
