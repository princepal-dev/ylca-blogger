package com.princeworks.blogger.service;

import com.princeworks.blogger.exceptions.BlogNotFoundException;
import com.princeworks.blogger.exceptions.UnauthorizedAccessException;
import com.princeworks.blogger.model.Blog;
import com.princeworks.blogger.model.Image;
import com.princeworks.blogger.model.User;
import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.payload.ImageResponseDTO;
import com.princeworks.blogger.payload.ImageUploadDTO;
import com.princeworks.blogger.repositories.BlogRepository;
import com.princeworks.blogger.repositories.ImageRepository;
import com.princeworks.blogger.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional
    public ImageResponseDTO uploadImage(Long blogId, MultipartFile file, ImageUploadDTO uploadDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();
        if (!blog.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedAccessException("You can only upload images to your own blogs");
        }

        validateImageFile(file);

        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path filePath = saveFile(file, fileName);
            String fileUrl = imageBaseUrl + "/" + fileName;

            Image image = new Image();
            image.setFileName(file.getOriginalFilename());
            image.setFileUrl(fileUrl);
            image.setContentType(file.getContentType());
            image.setFileSize(file.getSize());
            image.setDisplayOrder(uploadDTO != null ? uploadDTO.getDisplayOrder() : 0);
            image.setBlog(blog);

            Image savedImage = imageRepository.save(image);
            return mapToImageResponseDTO(savedImage);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    @Override
    @Transactional
    public List<ImageResponseDTO> uploadMultipleImages(Long blogId, List<MultipartFile> files, List<ImageUploadDTO> uploadDTOs) {
        List<ImageResponseDTO> uploadedImages = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ImageUploadDTO uploadDTO = (uploadDTOs != null && i < uploadDTOs.size()) ? uploadDTOs.get(i) : null;
            uploadedImages.add(uploadImage(blogId, file, uploadDTO));
        }

        return uploadedImages;
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        User currentUser = authUtil.loggedInUser();
        if (!image.getBlog().getUser().getUserId().equals(currentUser.getUserId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("You can only delete images from your own blogs or must be an admin");
        }

        // Delete physical file
        try {
            Path filePath = Paths.get(uploadDir, extractFileNameFromUrl(image.getFileUrl()));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue with database deletion
            System.err.println("Failed to delete physical file: " + e.getMessage());
        }

        imageRepository.delete(image);
    }

    @Override
    @Transactional
    public void deleteAllImagesForBlog(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();
        if (!blog.getUser().getUserId().equals(currentUser.getUserId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("You can only delete images from your own blogs or must be an admin");
        }

        List<Image> images = imageRepository.findByBlogBlogId(blogId);

        // Delete physical files
        for (Image image : images) {
            try {
                Path filePath = Paths.get(uploadDir, extractFileNameFromUrl(image.getFileUrl()));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete physical file: " + e.getMessage());
            }
        }

        imageRepository.deleteByBlogBlogId(blogId);
    }

    @Override
    public ImageResponseDTO getImageById(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        return mapToImageResponseDTO(image);
    }

    @Override
    public List<ImageResponseDTO> getImagesByBlogId(Long blogId) {
        List<Image> images = imageRepository.findByBlogBlogIdOrderByDisplayOrderAsc(blogId);
        return images.stream()
                .map(this::mapToImageResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateImageOrder(Long imageId, Integer newDisplayOrder) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        User currentUser = authUtil.loggedInUser();
        if (!image.getBlog().getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedAccessException("You can only update images in your own blogs");
        }

        image.setDisplayOrder(newDisplayOrder);
        imageRepository.save(image);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only image files are allowed.");
        }

        // Check file size (10MB limit as configured)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/jpg");
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    private Path saveFile(MultipartFile file, String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        // Create directory if it doesn't exist
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }

    private String extractFileNameFromUrl(String fileUrl) {
        // Extract filename from URL like "http://localhost:8080/images/filename.jpg"
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }

    private ImageResponseDTO mapToImageResponseDTO(Image image) {
        return modelMapper.map(image, ImageResponseDTO.class);
    }
}
