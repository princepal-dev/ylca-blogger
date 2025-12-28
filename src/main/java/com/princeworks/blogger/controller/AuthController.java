package com.princeworks.blogger.controller;

import com.princeworks.blogger.model.AppRole;
import com.princeworks.blogger.model.User;
import com.princeworks.blogger.repositories.UserRepository;
import com.princeworks.blogger.security.request.LoginRequest;
import com.princeworks.blogger.security.request.SignUpRequest;
import com.princeworks.blogger.security.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api")
public class AuthController {
  @Value("${spring.app.authKey}")
  private String authKey;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  // TODO : Sign in
  public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {}

  @PostMapping("/signup")
  public ResponseEntity<?> registerAdmin(@RequestBody SignUpRequest signUpRequest) {
    if (userRepository.existsByUserName(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }
    if (!Objects.equals(authKey, signUpRequest.getSecretKey())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Auth-key is incorrect"));
    }
    User user =
        new User(
            signUpRequest.getUsername(),
            passwordEncoder.encode(signUpRequest.getPassword()),
            AppRole.ROLE_ADMIN);
    userRepository.save(user);
    return ResponseEntity.ok(new MessageResponse("Admin registered successfully!"));
  }

  // TODO : Update Yourself
  // TODO : Create Normal Users
  // TODO : Delete Users
  // TODO : Update User
}
