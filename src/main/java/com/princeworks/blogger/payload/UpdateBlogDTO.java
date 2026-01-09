package com.princeworks.blogger.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBlogDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @Size(max = 100, message = "Author name must not exceed 100 characters")
    private String authorName;

    @Size(max = 100, message = "Author title must not exceed 100 characters")
    private String authorTitle;
}
