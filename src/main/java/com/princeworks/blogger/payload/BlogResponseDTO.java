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
    private List<ImageDTO> images;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDTO {
        private Long userId;
        private String username;
        private String fullName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDTO {
        private Long imageId;
        private String fileName;
        private String fileUrl;
        private String contentType;
        private Long fileSize;
        private Integer displayOrder;
        private Timestamp createdAt;
    }
}
