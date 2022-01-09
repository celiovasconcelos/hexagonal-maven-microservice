package com.thesystem.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thesystem.app.UserApplicationService;
import com.thesystem.app.dtos.UserDTO;

@RestController
@RequestMapping("/user")
public class UserRestConstroller {

  private @Autowired UserApplicationService userApplicationService;

  @GetMapping("/find")
  public UserDTO find() {
    return userApplicationService.find();
  }

}
