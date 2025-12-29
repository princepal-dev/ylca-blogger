package com.princeworks.blogger.security.response;

import lombok.Getter;
import lombok.Setter;
import java.sql.Timestamp;

@Getter
@Setter
public class UserInfoResponse {
  private Long userId;
  private String username;
  private String role;
  private String token;
  private Timestamp createdAt;

  public UserInfoResponse(Long userId, String username, String role) {
    this.userId = userId;
    this.username = username;
    this.role = role;
  }

  public UserInfoResponse(Long userId, String username, String role, String token) {
    this.userId = userId;
    this.username = username;
    this.role = role;
    this.token = token;
  }

  public UserInfoResponse(Long userId, String username, String role, String token, Timestamp createdAt) {
    this.userId = userId;
    this.username = username;
    this.role = role;
    this.token = token;
    this.createdAt = createdAt;
  }
}
