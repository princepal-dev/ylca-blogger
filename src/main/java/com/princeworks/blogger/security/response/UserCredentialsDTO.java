package com.princeworks.blogger.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialsDTO {
    private String username;
    private String password;
    private String message;
}
