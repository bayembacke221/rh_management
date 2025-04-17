package sn.bmbacke.rh.entity;

import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import lombok.*;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true, exclude = "user")
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "employees")
@Data @NoArgsConstructor @AllArgsConstructor
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
    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;
    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
