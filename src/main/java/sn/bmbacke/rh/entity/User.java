package sn.bmbacke.rh.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import sn.bmbacke.rh.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Collection;
import java.util.List;
import java.security.Principal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@SuperBuilder
@Table(name = "users")
@NoArgsConstructor
@ToString(exclude = {"employee", "roles"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity implements UserDetails, Principal{

    private String username;
    private String password;
    private String email;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn()
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Employee employee;
    @ManyToMany(fetch = EAGER)
    @JsonBackReference
    private List<Role> roles;

    private LocalDateTime lastLogin;

    private boolean accountLocked;
    private boolean enabled;

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(
                        role -> new SimpleGrantedAuthority(role.getName())
                )
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getFullName() {
        if (this.employee == null) return this.email;
        return this.employee.getFirstName() + " " + this.employee.getLastName();
    }
}
