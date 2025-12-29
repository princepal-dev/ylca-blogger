package com.princeworks.blogger.controller;

import com.princeworks.blogger.exceptions.BlogNotFoundException;
import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/blogs")
public class PublicBlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogs() {
        List<BlogResponseDTO> blogs = blogService.getAllBlogs();
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> getBlogById(@PathVariable Long id) {
        try {
            BlogResponseDTO blog = blogService.getBlogById(id);
            return ResponseEntity.ok(blog);
        } catch (BlogNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByAuthor(@PathVariable Long authorId) {
        List<BlogResponseDTO> blogs = blogService.getBlogsByAuthor(authorId);
        return ResponseEntity.ok(blogs);
    }
}
