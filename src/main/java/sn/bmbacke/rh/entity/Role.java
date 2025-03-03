package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.RoleEnum;

import java.util.List;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "document")
@Data
@NoArgsConstructor
public class Role  extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private RoleEnum name;
    private String description;
    @ManyToMany(fetch = EAGER)
    @JsonBackReference
    private List<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    private List<User> user;
}
