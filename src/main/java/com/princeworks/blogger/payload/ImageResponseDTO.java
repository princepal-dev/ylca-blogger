package com.princeworks.blogger.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ImageResponseDTO {
    private Long imageId;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private Integer displayOrder;
    private Timestamp createdAt;
}