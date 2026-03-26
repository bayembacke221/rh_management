package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "employees")
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"departement", "position", "manager", "user"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Employee extends BaseEntity {

    @Column(name = "first_name", columnDefinition = "VARCHAR(255)")
    private String firstName;
    @Column(name = "last_name", columnDefinition = "VARCHAR(255)")
    private String lastName;
    @Column(nullable = true)
    private String phone;
    @Column(nullable = true)
    private LocalDate birthDate;
    @Column(nullable = true)
    private LocalDate hireDate;
    @Column(nullable = true)
    private LocalDate endDate;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Column(nullable = true)
    private String address;
    @Column(nullable = true)
    private String city;
    @Column(nullable = true)
    private String socialSecurityNumber;
    @Column(nullable = true)
    private String emergencyContactName;
    @Column(nullable = true)
    private String emergencyContactPhone;
    @Column(nullable = true)
    private String bankAccountInfo;
    @Column(nullable = true)
    private String cv;
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    @JsonIgnoreProperties({"manager", "parentDepartement"})
    private Departement departement;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    @JsonIgnoreProperties({"department"})
    private Position position;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"departement", "position", "manager", "user"})
    private Employee manager;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
