package com.princeworks.blogger.util;

import com.princeworks.blogger.model.User;
import com.princeworks.blogger.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
  @Autowired private UserRepository userRepository;

  public Long loggedInUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user =
        userRepository
            .findByUserName(authentication.getName())
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        "User not found with username: " + authentication.getName()));
    return user.getUserId();
  }

  public User loggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userRepository
        .findByUserName(authentication.getName())
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    "User not found with username: " + authentication.getName()));
  }
}
