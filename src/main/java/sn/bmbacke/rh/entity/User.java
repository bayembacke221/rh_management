package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "contract")
@Data
@NoArgsConstructor
public class User extends BaseEntity {

    private String username;
    private String password;
    private String email;
    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn()
    private  Employee employee;
    @ManyToMany(fetch = EAGER)
    @JsonBackReference
    private List<Role> roles;

    private LocalDateTime lastLogin;
    private Boolean active;
}
