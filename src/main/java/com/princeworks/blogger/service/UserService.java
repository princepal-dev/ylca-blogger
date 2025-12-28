package com.princeworks.blogger.service;

import com.princeworks.blogger.model.User;
import com.princeworks.blogger.security.request.CreateUserByAdminDTO;
import com.princeworks.blogger.security.request.UpdateUserDTO;
import com.princeworks.blogger.security.response.UserCredentialsDTO;

public interface UserService {
    UserCredentialsDTO createUserByAdmin(CreateUserByAdminDTO createUserDTO);
    User updateUser(Long userId, UpdateUserDTO updateUserDTO);
    User updateOwnProfile(UpdateUserDTO updateUserDTO);
    void deleteUser(Long userId);
    User getUserById(Long userId);
    User getCurrentUser();
}
