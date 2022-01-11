package com.thesystem.app.dtos;

import com.thesystem.domain.User;

import lombok.Data;

@Data
public class UserDTO {

  private String name;
  //private User father; //uncomment this to see ArchUnit tests failing

  public UserDTO(User user) {
    this.name = user.getName();
  }

}
