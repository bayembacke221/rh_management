package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;
import sn.bmbacke.rh.entity.enums.Categorie;
import sn.bmbacke.rh.entity.enums.PermissionEnum;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "document")
@Data
@NoArgsConstructor
public class Permission extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private PermissionEnum name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Categorie category;

    @ManyToMany(mappedBy = "permissions")
    @JsonBackReference
    private List<Role> roles;
}
