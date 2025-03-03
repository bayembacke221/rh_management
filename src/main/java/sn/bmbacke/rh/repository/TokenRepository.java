package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Token;

import java.util.Optional;

public interface TokenRepository extends GenericRepository<Token, Integer> {

    Optional<Token> findByToken(String token);
}
