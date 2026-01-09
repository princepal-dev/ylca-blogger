package com.princeworks.blogger.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponseDTO {
    private Long blogId;
    private String title;
    private String pdfPath;
    private String authorName;
    private String authorImage;
    private String authorTitle;
    private AuthorDTO author;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
