package com.princeworks.blogger.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDTO {
    private Long blogId;
    private String title;
    private String description;
    private AuthorDTO author;
    private List<ImageResponseDTO> images;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
