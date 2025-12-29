package com.princeworks.blogger.payload;

import com.princeworks.blogger.validation.HtmlContentSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBlogDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @HtmlContentSize(min = 150, message = "Description must be at least 150 characters (excluding HTML tags)")
    private String description;
}
