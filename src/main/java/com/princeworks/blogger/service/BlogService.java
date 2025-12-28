package com.princeworks.blogger.service;

import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.payload.CreateBlogDTO;
import com.princeworks.blogger.payload.UpdateBlogDTO;

import java.util.List;

public interface BlogService {
    BlogResponseDTO createBlog(CreateBlogDTO createBlogDTO);
    BlogResponseDTO getBlogById(Long blogId);
    List<BlogResponseDTO> getAllBlogs();
    List<BlogResponseDTO> getBlogsByAuthor(Long authorId);
    List<BlogResponseDTO> getMyBlogs();
    BlogResponseDTO updateBlog(Long blogId, UpdateBlogDTO updateBlogDTO);
    void deleteBlog(Long blogId);
}
