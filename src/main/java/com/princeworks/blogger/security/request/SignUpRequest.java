package com.princeworks.blogger.security.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank
    @Size (min = 3, max = 30)
    private String username;

    @NotBlank
    @Size (max = 60)
    private String password;

    @NotBlank
    private String secretKey;
}
