package com.thesystem.domain;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public class User {

  private final String name;

  private User(String name) {
    this.name = name;
  }

  public static User fromName(String name) {
    if (StringUtils.isBlank(name))
      throw new IllegalArgumentException("The user name is required");
    return new User(name);
  }

}
