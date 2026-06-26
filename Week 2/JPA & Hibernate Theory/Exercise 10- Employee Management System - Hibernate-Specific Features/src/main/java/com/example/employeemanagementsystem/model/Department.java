package com.example.employeemanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@DynamicInsert
@DynamicUpdate
@NamedQuery(
    name = "Department.findByNameNamed",
    query = "SELECT d FROM Department d WHERE d.name = :name"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Department extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_seq")
    @SequenceGenerator(name = "department_seq", sequenceName = "department_sequence", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties("department")
    @BatchSize(size = 10)
    private List<Employee> employees = new ArrayList<>();
}
