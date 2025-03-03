package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.User;

import java.util.Optional;

public interface UserRepository extends GenericRepository<User, Long> {

    Optional<User> findByEmail(String email);

}
