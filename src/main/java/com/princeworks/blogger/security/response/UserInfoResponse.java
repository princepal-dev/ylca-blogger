package com.princeworks.blogger.security.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {
  private Long userId;
  private String username;
  private String role;

  public UserInfoResponse(Long userId, String username, String role) {
    this.userId = userId;
    this.username = username;
    this.role = role;
  }
}
