package com.princeworks.blogger.service;

import com.princeworks.blogger.payload.ImageResponseDTO;
import com.princeworks.blogger.payload.ImageUploadDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ImageResponseDTO uploadImage(Long blogId, MultipartFile file, ImageUploadDTO uploadDTO);
    List<ImageResponseDTO> uploadMultipleImages(Long blogId, List<MultipartFile> files, List<ImageUploadDTO> uploadDTOs);
    void deleteImage(Long imageId);
    void deleteAllImagesForBlog(Long blogId);
    ImageResponseDTO getImageById(Long imageId);
    List<ImageResponseDTO> getImagesByBlogId(Long blogId);
    void updateImageOrder(Long imageId, Integer newDisplayOrder);
}
