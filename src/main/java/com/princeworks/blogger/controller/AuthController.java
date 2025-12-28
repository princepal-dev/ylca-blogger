package com.princeworks.blogger.controller;

import com.princeworks.blogger.model.AppRole;
import com.princeworks.blogger.model.User;
import com.princeworks.blogger.repositories.UserRepository;
import com.princeworks.blogger.security.jwt.JwtUtils;
import com.princeworks.blogger.security.request.CreateUserByAdminDTO;
import com.princeworks.blogger.security.request.LoginRequest;
import com.princeworks.blogger.security.request.SignUpRequest;
import com.princeworks.blogger.security.request.UpdateUserDTO;
import com.princeworks.blogger.security.response.MessageResponse;
import com.princeworks.blogger.security.response.UserCredentialsDTO;
import com.princeworks.blogger.security.response.UserInfoResponse;
import com.princeworks.blogger.security.services.UserDetailsImpl;
import com.princeworks.blogger.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Value("${spring.app.authKey}")
  private String authKey;

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private JwtUtils jwtUtils;
  @Autowired private UserService userService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
          .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: Invalid username or password"));
    }
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignUpRequest signUpRequest) {
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

  @PostMapping("/users")
  public ResponseEntity<?> createUserByAdmin(@Valid @RequestBody CreateUserByAdminDTO createUserDTO) {
    try {
      UserCredentialsDTO credentials = userService.createUserByAdmin(createUserDTO);
      return ResponseEntity.ok(credentials);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
    try {
      User updatedUser = userService.updateUser(userId, updateUserDTO);
      return ResponseEntity.ok(new UserInfoResponse(updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getRole().name()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @PutMapping("/profile")
  public ResponseEntity<?> updateOwnProfile(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
    try {
      User updatedUser = userService.updateOwnProfile(updateUserDTO);
      return ResponseEntity.ok(new UserInfoResponse(updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getRole().name()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @DeleteMapping("/users/{userId}")
  public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
    try {
      userService.deleteUser(userId);
      return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }
}
