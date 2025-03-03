package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

import java.util.List;

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
    private String name;
    private String description;
    @ManyToMany(fetch = EAGER)
    @JsonBackReference
    private List<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    private List<User> user;
}
