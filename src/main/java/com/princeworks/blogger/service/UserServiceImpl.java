package com.princeworks.blogger.service;

import com.princeworks.blogger.exceptions.UnauthorizedAccessException;
import com.princeworks.blogger.model.AppRole;
import com.princeworks.blogger.model.User;
import com.princeworks.blogger.repositories.UserRepository;
import com.princeworks.blogger.security.request.CreateUserByAdminDTO;
import com.princeworks.blogger.security.request.UpdateUserDTO;
import com.princeworks.blogger.security.response.UserCredentialsDTO;
import com.princeworks.blogger.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthUtil authUtil;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int USERNAME_LENGTH = 8;
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public UserCredentialsDTO createUserByAdmin(CreateUserByAdminDTO createUserDTO) {
        User currentUser = authUtil.loggedInUser();

        // Check if current user is admin
        if (!currentUser.getRole().equals(AppRole.ROLE_ADMIN)) {
            throw new UnauthorizedAccessException("Only admins can create users");
        }

        // Generate unique username
        String username;
        do {
            username = generateRandomString(USERNAME_LENGTH);
        } while (userRepository.existsByUserName(username));

        // Generate random password
        String password = generateRandomString(PASSWORD_LENGTH);

        // Create user
        User user = new User();
        user.setUserName(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(AppRole.ROLE_COLLABORATOR);
        user.setFullName(createUserDTO.getFullName());
        user.setPhoneNumber(createUserDTO.getPhoneNumber());

        userRepository.save(user);

        return new UserCredentialsDTO(username, password,
            "User created successfully. Please share these credentials with the user.");
    }

    @Override
    public User updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        User currentUser = authUtil.loggedInUser();

        // Check if current user is admin or updating themselves
        if (!currentUser.getRole().equals(AppRole.ROLE_ADMIN) && !currentUser.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only update your own profile or must be an admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update fields if provided
        if (updateUserDTO.getFullName() != null) {
            user.setFullName(updateUserDTO.getFullName());
        }
        if (updateUserDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateUserDTO.getPhoneNumber());
        }

        return userRepository.save(user);
    }

    @Override
    public User updateOwnProfile(UpdateUserDTO updateUserDTO) {
        User currentUser = authUtil.loggedInUser();
        return updateUser(currentUser.getUserId(), updateUserDTO);
    }

    @Override
    public void deleteUser(Long userId) {
        User currentUser = authUtil.loggedInUser();

        // Check if current user is admin
        if (!currentUser.getRole().equals(AppRole.ROLE_ADMIN)) {
            throw new UnauthorizedAccessException("Only admins can delete users");
        }

        // Prevent admin from deleting themselves
        if (currentUser.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You cannot delete your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        userRepository.delete(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    @Override
    public User getCurrentUser() {
        return authUtil.loggedInUser();
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
