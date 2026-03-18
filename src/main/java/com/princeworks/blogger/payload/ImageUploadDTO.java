package com.princeworks.blogger.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDTO {
    private Integer displayOrder = 0;
}
