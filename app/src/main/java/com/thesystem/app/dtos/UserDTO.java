package com.thesystem.app.dtos;

import com.thesystem.domain.User;

import lombok.Data;

@Data
public class UserDTO {

  private final String name;

  public UserDTO(User user) {
    this.name = user.getName();
  }

}
