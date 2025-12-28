package com.princeworks.blogger.controller;

import com.princeworks.blogger.exceptions.BlogNotFoundException;
import com.princeworks.blogger.exceptions.UnauthorizedAccessException;
import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.payload.CreateBlogDTO;
import com.princeworks.blogger.payload.ImageResponseDTO;
import com.princeworks.blogger.payload.ImageUploadDTO;
import com.princeworks.blogger.payload.UpdateBlogDTO;
import com.princeworks.blogger.service.BlogService;
import com.princeworks.blogger.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private ImageService imageService;

    @PostMapping
    public ResponseEntity<BlogResponseDTO> createBlog(@Valid @RequestBody CreateBlogDTO createBlogDTO) {
        try {
            BlogResponseDTO createdBlog = blogService.createBlog(createBlogDTO);
            return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> getBlogById(@PathVariable Long id) {
        try {
            BlogResponseDTO blog = blogService.getBlogById(id);
            return ResponseEntity.ok(blog);
        } catch (BlogNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogs() {
        List<BlogResponseDTO> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByAuthor(@PathVariable Long authorId) {
        List<BlogResponseDTO> blogs = blogService.getBlogsByAuthor(authorId);
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BlogResponseDTO>> getMyBlogs() {
        List<BlogResponseDTO> blogs = blogService.getMyBlogs();
        return ResponseEntity.ok(blogs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBlogDTO updateBlogDTO) {
        try {
            BlogResponseDTO updatedBlog = blogService.updateBlog(id, updateBlogDTO);
            return ResponseEntity.ok(updatedBlog);
        } catch (BlogNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.noContent().build();
        } catch (BlogNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Image-related endpoints

    @PostMapping("/{blogId}/images")
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @PathVariable Long blogId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "displayOrder", required = false, defaultValue = "0") Integer displayOrder) {
        try {
            ImageUploadDTO uploadDTO = new ImageUploadDTO();
            uploadDTO.setDisplayOrder(displayOrder);

            ImageResponseDTO uploadedImage = imageService.uploadImage(blogId, file, uploadDTO);
            return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
        } catch (BlogNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{blogId}/images/multiple")
    public ResponseEntity<List<ImageResponseDTO>> uploadMultipleImages(
            @PathVariable Long blogId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "displayOrders", required = false) List<Integer> displayOrders) {
        try {
            List<ImageUploadDTO> uploadDTOs = null;
            if (displayOrders != null && !displayOrders.isEmpty()) {
                uploadDTOs = displayOrders.stream()
                        .map(order -> {
                            ImageUploadDTO dto = new ImageUploadDTO();
                            dto.setDisplayOrder(order);
                            return dto;
                        })
                        .collect(Collectors.toList());
            }

            List<ImageResponseDTO> uploadedImages = imageService.uploadMultipleImages(blogId, files, uploadDTOs);
            return new ResponseEntity<>(uploadedImages, HttpStatus.CREATED);
        } catch (BlogNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{blogId}/images")
    public ResponseEntity<List<ImageResponseDTO>> getBlogImages(@PathVariable Long blogId) {
        try {
            List<ImageResponseDTO> images = imageService.getImagesByBlogId(blogId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<ImageResponseDTO> getImage(@PathVariable Long imageId) {
        try {
            ImageResponseDTO image = imageService.getImageById(imageId);
            return ResponseEntity.ok(image);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        try {
            imageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("only delete")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/images/{imageId}/order")
    public ResponseEntity<Void> updateImageOrder(
            @PathVariable Long imageId,
            @RequestParam("displayOrder") Integer displayOrder) {
        try {
            imageService.updateImageOrder(imageId, displayOrder);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("only update")) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
