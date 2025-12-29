package com.princeworks.blogger.service;

import com.princeworks.blogger.exceptions.BlogNotFoundException;
import com.princeworks.blogger.exceptions.UnauthorizedAccessException;
import com.princeworks.blogger.model.Blog;
import com.princeworks.blogger.model.User;
import com.princeworks.blogger.payload.AuthorDTO;
import com.princeworks.blogger.payload.BlogResponseDTO;
import com.princeworks.blogger.payload.CreateBlogDTO;
import com.princeworks.blogger.payload.UpdateBlogDTO;
import com.princeworks.blogger.repositories.BlogRepository;
import com.princeworks.blogger.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BlogResponseDTO createBlog(CreateBlogDTO createBlogDTO) {
        User currentUser = authUtil.loggedInUser();

        Blog blog = new Blog();
        blog.setTitle(createBlogDTO.getTitle());
        blog.setDescription(createBlogDTO.getDescription());
        blog.setUser(currentUser);

        Blog savedBlog = blogRepository.save(blog);
        return mapToBlogResponseDTO(savedBlog);
    }

    @Override
    public BlogResponseDTO getBlogById(Long blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));
        return mapToBlogResponseDTO(blog);
    }

    @Override
    public List<BlogResponseDTO> getAllBlogs() {
        List<Blog> blogs = blogRepository.findAll();
        return blogs.stream()
                .map(this::mapToBlogResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogResponseDTO> getBlogsByAuthor(Long authorId) {
        List<Blog> blogs = blogRepository.findByUserUserId(authorId);
        return blogs.stream()
                .map(this::mapToBlogResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogResponseDTO> getMyBlogs() {
        User currentUser = authUtil.loggedInUser();
        List<Blog> blogs = blogRepository.findByUser(currentUser);
        return blogs.stream()
                .map(this::mapToBlogResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BlogResponseDTO updateBlog(Long blogId, UpdateBlogDTO updateBlogDTO) {
        Blog existingBlog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();

        // Check if the current user is the author of the blog
        if (!existingBlog.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedAccessException("You can only update your own blogs");
        }

        existingBlog.setTitle(updateBlogDTO.getTitle());
        existingBlog.setDescription(updateBlogDTO.getDescription());

        Blog updatedBlog = blogRepository.save(existingBlog);
        return mapToBlogResponseDTO(updatedBlog);
    }

    @Override
    public void deleteBlog(Long blogId) {
        Blog existingBlog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();

        // Check if the current user is the author of the blog or is an admin
        if (!existingBlog.getUser().getUserId().equals(currentUser.getUserId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("You can only delete your own blogs or must be an admin");
        }

        // Delete associated images
        imageService.deleteAllImagesForBlog(existingBlog.getBlogId());

        blogRepository.delete(existingBlog);
    }

    private BlogResponseDTO mapToBlogResponseDTO(Blog blog) {
        BlogResponseDTO dto = modelMapper.map(blog, BlogResponseDTO.class);

        // Manually set the author information
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setUserId(blog.getUser().getUserId());
        authorDTO.setUsername(blog.getUser().getUserName());
        authorDTO.setFullName(blog.getUser().getFullName());

        dto.setAuthor(authorDTO);

        // Set images
        dto.setImages(imageService.getImagesByBlogId(blog.getBlogId()));

        return dto;
    }
}
