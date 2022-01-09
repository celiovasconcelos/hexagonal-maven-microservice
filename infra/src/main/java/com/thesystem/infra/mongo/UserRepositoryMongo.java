package com.thesystem.infra.mongo;

import org.springframework.stereotype.Repository;

import com.thesystem.domain.User;
import com.thesystem.domain.UserRepository;

@Repository
public class UserRepositoryMongo implements UserRepository {

  @Override
  public User findUser() {
    return User.fromName("Jorge");
  }

}
