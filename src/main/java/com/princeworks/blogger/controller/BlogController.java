package com.princeworks.blogger.controller;

import com.princeworks.blogger.exceptions.BlogNotFoundException;
import com.princeworks.blogger.exceptions.UnauthorizedAccessException;
import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.payload.CreateBlogDTO;
import com.princeworks.blogger.payload.UpdateBlogDTO;
import com.princeworks.blogger.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

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

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBlogs() {
        try {
            blogService.deleteAllBlogs();
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedAccessException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // PDF-related endpoints

    @PostMapping("/{blogId}/pdf")
    public ResponseEntity<?> uploadPdf(
            @PathVariable Long blogId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new com.princeworks.blogger.security.response.MessageResponse("Error: PDF file is required"));
            }
            
            String pdfPath = blogService.uploadPdf(blogId, file);
            return ResponseEntity.ok()
                .body(new java.util.HashMap<String, String>() {{
                    put("message", "PDF uploaded successfully");
                    put("pdfPath", pdfPath);
                }});
        } catch (BlogNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            System.err.println("PDF Upload Error - Blog ID: " + blogId);
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Error Class: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: Failed to upload PDF - " + e.getMessage()));
        }
    }

    @PostMapping("/{blogId}/author-image")
    public ResponseEntity<?> uploadAuthorImage(
            @PathVariable Long blogId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new com.princeworks.blogger.security.response.MessageResponse("Error: Author image file is required"));
            }
            
            String imagePath = blogService.uploadAuthorImage(blogId, file);
            return ResponseEntity.ok()
                .body(new java.util.HashMap<String, String>() {{
                    put("message", "Author image uploaded successfully");
                    put("imagePath", imagePath);
                }});
        } catch (BlogNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            System.err.println("Author Image Upload Error - Blog ID: " + blogId);
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Error Class: " + e.getClass().getName());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new com.princeworks.blogger.security.response.MessageResponse("Error: Failed to upload author image - " + e.getMessage()));
        }
    }
}
