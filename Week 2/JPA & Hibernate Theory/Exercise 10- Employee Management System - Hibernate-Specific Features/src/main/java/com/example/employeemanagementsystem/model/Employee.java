package com.example.employeemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "employees")
@DynamicInsert
@DynamicUpdate
@NamedQueries({
    @NamedQuery(
        name = "Employee.findByEmailNamed",
        query = "SELECT e FROM Employee e WHERE e.email = :email"
    ),
    @NamedQuery(
        name = "Employee.findByDepartmentNamed",
        query = "SELECT e FROM Employee e WHERE e.department.id = :deptId"
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Employee extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @SequenceGenerator(name = "employee_seq", sequenceName = "employee_sequence", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties("employees")
    private Department department;

    @Formula("CONCAT(name, ' (', email, ')')")
    private String displayName;
}
