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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

      // Fetch the full user entity to get createdAt
      User user = userRepository.findById(userDetails.getId())
          .orElseThrow(() -> new RuntimeException("User not found"));

      ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
      String jwtToken = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
          .body(new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority(), jwtToken, user.getCreatedAt()));
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
      return ResponseEntity.ok(new UserInfoResponse(updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getRole().name(), null, updatedUser.getCreatedAt()));
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @PutMapping("/profile")
  public ResponseEntity<?> updateOwnProfile(@Valid @RequestBody UpdateUserDTO updateUserDTO) {
    try {
      User updatedUser = userService.updateOwnProfile(updateUserDTO);
      return ResponseEntity.ok(new UserInfoResponse(updatedUser.getUserId(), updatedUser.getUserName(), updatedUser.getRole().name(), null, updatedUser.getCreatedAt()));
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

  @GetMapping("/users")
  public ResponseEntity<?> getAllUsers() {
    try {
      List<User> users = userRepository.findAll();
      List<UserInfoResponse> userResponses = users.stream()
          .map(user -> new UserInfoResponse(
              user.getUserId(),
              user.getUserName(),
              user.getRole().name(),
              null, // no token for users list
              user.getCreatedAt()
          ))
          .collect(Collectors.toList());

      return ResponseEntity.ok(userResponses);
    } catch (Exception e) {
      return ResponseEntity.status(500).body(new MessageResponse("Error: " + e.getMessage()));
    }
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser() {
    try {
      // Get the current authenticated user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null || !authentication.isAuthenticated()) {
        System.out.println("AuthController - No authentication found");
        return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
      }

      // Check if principal is UserDetailsImpl (not anonymous user)
      Object principal = authentication.getPrincipal();
      if (!(principal instanceof UserDetailsImpl)) {
        System.out.println("AuthController - Principal is not UserDetailsImpl: " + principal.getClass().getName());
        return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
      }

      UserDetailsImpl userDetails = (UserDetailsImpl) principal;
      System.out.println("AuthController - Current user: " + userDetails.getUsername() +
                        ", Roles: " + userDetails.getAuthorities());

      // Fetch the full user entity to get createdAt
      User user = userRepository.findById(userDetails.getId())
          .orElseThrow(() -> new RuntimeException("User not found"));

      UserInfoResponse response = new UserInfoResponse(
          userDetails.getId(),
          userDetails.getUsername(),
          userDetails.getAuthorities().iterator().next().getAuthority(),
          null, // no token for /me endpoint
          user.getCreatedAt()
      );

      System.out.println("AuthController - Returning user data: " + response.getUsername());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      System.err.println("AuthController - Error getting current user: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
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
