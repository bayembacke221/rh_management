package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Role;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends GenericRepository<Role, Long> {

    Optional<Role> findByName(String name);

    List<Role> findAllByIdIn(Collection<Long> id);
}
