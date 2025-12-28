package com.princeworks.blogger.security.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 10, message = "Phone number must be maximum 10 characters")
    private String phoneNumber;
}
