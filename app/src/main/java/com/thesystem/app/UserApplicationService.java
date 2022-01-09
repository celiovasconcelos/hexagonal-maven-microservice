package com.thesystem.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thesystem.app.dtos.UserDTO;
import com.thesystem.domain.User;
import com.thesystem.domain.UserRepository;

@Service
public class UserApplicationService {

  private @Autowired UserRepository userRepository;

  @Transactional
  public UserDTO find() {
    User user = userRepository.findUser();
    return new UserDTO(user);
  }

}
