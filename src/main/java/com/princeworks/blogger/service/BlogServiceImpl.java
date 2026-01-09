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
import com.princeworks.blogger.util.HtmlSanitizer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private HtmlSanitizer htmlSanitizer;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public BlogResponseDTO createBlog(CreateBlogDTO createBlogDTO) {
        User currentUser = authUtil.loggedInUser();

        Blog blog = new Blog();
        blog.setTitle(createBlogDTO.getTitle());
        blog.setAuthorName(createBlogDTO.getAuthorName());
        blog.setAuthorTitle(createBlogDTO.getAuthorTitle());
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
                .map(blog -> {
                    try {
                        return mapToBlogResponseDTO(blog);
                    } catch (jakarta.persistence.EntityNotFoundException e) {
                        // Skip blogs with orphaned user references
                        return null;
                    }
                })
                .filter(dto -> dto != null)
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
        existingBlog.setAuthorName(updateBlogDTO.getAuthorName());
        existingBlog.setAuthorTitle(updateBlogDTO.getAuthorTitle());

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

        blogRepository.delete(existingBlog);
    }

    @Override
    public void deleteAllBlogs() {
        User currentUser = authUtil.loggedInUser();

        // Only allow admins to delete all blogs
        if (!currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("Only administrators can delete all blogs");
        }

        blogRepository.deleteAll();
    }

    private BlogResponseDTO mapToBlogResponseDTO(Blog blog) {
        BlogResponseDTO dto = modelMapper.map(blog, BlogResponseDTO.class);

        // Explicitly ensure pdfPath is set
        dto.setPdfPath(blog.getPdfPath());
        
        // Explicitly set authorName, authorImage, and authorTitle
        dto.setAuthorName(blog.getAuthorName());
        dto.setAuthorImage(blog.getAuthorImage());
        dto.setAuthorTitle(blog.getAuthorTitle());

        // Manually set the author information
        // Handle case where user might not exist (orphaned blog records)
        AuthorDTO authorDTO = new AuthorDTO();
        try {
            User user = blog.getUser();
            if (user != null) {
                authorDTO.setUserId(user.getUserId());
                authorDTO.setUsername(user.getUserName());
                authorDTO.setFullName(user.getFullName());
            } else {
                // Fallback if user is null
                authorDTO.setUserId(0L);
                authorDTO.setUsername("Unknown");
                authorDTO.setFullName("Unknown Author");
            }
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Handle case where user was deleted but blog still references it
            authorDTO.setUserId(0L);
            authorDTO.setUsername("Unknown");
            authorDTO.setFullName("Unknown Author");
        }

        dto.setAuthor(authorDTO);

        return dto;
    }

    @Value("${app.upload.dir}")
    private String uploadDir;
    
    private java.nio.file.Path getUploadDirectory() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
            
            // If the path doesn't exist or is not writable, try to create it
            if (!java.nio.file.Files.exists(path)) {
                try {
                    java.nio.file.Files.createDirectories(path);
                } catch (java.io.IOException e) {
                    System.err.println("Warning: Could not create upload directory: " + path.toString());
                    // Fallback to system temp directory
                    path = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "ylca-uploads");
                    java.nio.file.Files.createDirectories(path);
                    System.err.println("Using fallback upload directory: " + path.toString());
                }
            }
            
            // Verify it's writable
            if (!java.nio.file.Files.isWritable(path)) {
                System.err.println("Warning: Upload directory is not writable: " + path.toString());
                // Fallback to system temp directory
                path = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "ylca-uploads");
                java.nio.file.Files.createDirectories(path);
                System.err.println("Using fallback upload directory: " + path.toString());
            }
            
            return path;
        } catch (Exception e) {
            System.err.println("Error resolving upload directory, using temp directory: " + e.getMessage());
            java.nio.file.Path fallbackPath = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"), "ylca-uploads");
            try {
                java.nio.file.Files.createDirectories(fallbackPath);
            } catch (java.io.IOException ioException) {
                throw new RuntimeException("Failed to create fallback upload directory", ioException);
            }
            return fallbackPath;
        }
    }

    @Override
    public String uploadPdf(Long blogId, MultipartFile file) {
        Blog existingBlog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();

        // Check if the current user is the author of the blog or is an admin
        if (!existingBlog.getUser().getUserId().equals(currentUser.getUserId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("You can only upload PDFs to your own blogs or must be an admin");
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty");
        }

        // Validate file type - check content type or file extension
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        boolean isValidPdf = false;
        
        if (contentType != null) {
            isValidPdf = contentType.equals("application/pdf") || 
                        contentType.equals("application/x-pdf") ||
                        contentType.contains("pdf");
        }
        
        if (!isValidPdf && originalFilename != null && originalFilename.contains(".")) {
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex >= 0 && lastDotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
                isValidPdf = extension.equals("pdf");
            }
        }
        
        if (!isValidPdf) {
            throw new IllegalArgumentException("Only PDF files are allowed. Received: " + (contentType != null ? contentType : "unknown"));
        }

        // Validate file size (max 50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 50MB. Current size: " + (file.getSize() / 1024 / 1024) + "MB");
        }

        try {
            // Resolve absolute path for upload directory
            java.nio.file.Path uploadPath = getUploadDirectory();
            java.nio.file.Path pdfsDir = uploadPath.resolve("pdfs");
            
            // Create PDFs directory if it doesn't exist
            if (!java.nio.file.Files.exists(pdfsDir)) {
                try {
                    java.nio.file.Files.createDirectories(pdfsDir);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Failed to create PDFs directory: " + pdfsDir.toString() + " - " + e.getMessage(), e);
                }
            }
            
            // Verify directory is writable
            if (!java.nio.file.Files.isWritable(pdfsDir)) {
                throw new RuntimeException("PDFs directory is not writable: " + pdfsDir.toString());
            }

            // Delete old PDF if it exists
            if (existingBlog.getPdfPath() != null && !existingBlog.getPdfPath().isEmpty()) {
                try {
                    String pdfPath = existingBlog.getPdfPath();
                    int lastSlashIndex = pdfPath.lastIndexOf("/");
                    if (lastSlashIndex >= 0 && lastSlashIndex < pdfPath.length() - 1) {
                        String oldFileName = pdfPath.substring(lastSlashIndex + 1);
                        java.nio.file.Path oldFilePath = pdfsDir.resolve(oldFileName);
                        if (java.nio.file.Files.exists(oldFilePath)) {
                            java.nio.file.Files.delete(oldFilePath);
                        }
                    }
                } catch (Exception e) {
                    // Log but don't fail if old file deletion fails
                    System.err.println("Warning: Could not delete old PDF file: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Generate unique filename
            String fileName = "blog_" + blogId + "_pdf_" + System.currentTimeMillis() + ".pdf";
            java.nio.file.Path filePath = pdfsDir.resolve(fileName);

            // Save file
            try {
                System.out.println("Attempting to save PDF to: " + filePath.toString());
                System.out.println("File size: " + file.getSize() + " bytes");
                System.out.println("Directory exists: " + java.nio.file.Files.exists(pdfsDir));
                System.out.println("Directory is writable: " + java.nio.file.Files.isWritable(pdfsDir));
                
                // Ensure parent directory exists
                java.nio.file.Files.createDirectories(filePath.getParent());
                
                file.transferTo(filePath.toFile());
                System.out.println("PDF file saved successfully to: " + filePath.toString());
            } catch (java.io.IOException e) {
                System.err.println("IOException during file transfer:");
                System.err.println("Target path: " + filePath.toString());
                System.err.println("Parent directory exists: " + java.nio.file.Files.exists(filePath.getParent()));
                e.printStackTrace();
                throw new RuntimeException("Failed to transfer PDF file to: " + filePath.toString() + " - " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                System.err.println("IllegalStateException during file transfer:");
                System.err.println("Target path: " + filePath.toString());
                e.printStackTrace();
                throw new RuntimeException("Failed to transfer PDF file - file may have been moved or deleted: " + e.getMessage(), e);
            }

            // Update blog with PDF path
            existingBlog.setPdfPath("/pdfs/" + fileName);
            blogRepository.save(existingBlog);

            return "/pdfs/" + fileName;
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            throw new RuntimeException("Failed to upload PDF: " + e.getMessage() + " | Upload dir: " + uploadDir, e);
        }
    }

    @Override
    public String uploadAuthorImage(Long blogId, MultipartFile file) {
        Blog existingBlog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        User currentUser = authUtil.loggedInUser();

        // Check if the current user is the author of the blog or is an admin
        if (!existingBlog.getUser().getUserId().equals(currentUser.getUserId()) &&
            !currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new UnauthorizedAccessException("You can only upload author images to your own blogs or must be an admin");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Author image file is empty");
        }

        try {
            // Resolve absolute path for upload directory
            java.nio.file.Path uploadPath = getUploadDirectory();
            java.nio.file.Path authorImagesDir = uploadPath.resolve("author-images");
            
            // Create author-images directory if it doesn't exist
            if (!java.nio.file.Files.exists(authorImagesDir)) {
                try {
                    java.nio.file.Files.createDirectories(authorImagesDir);
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Failed to create author-images directory: " + authorImagesDir.toString() + " - " + e.getMessage(), e);
                }
            }
            
            // Verify directory is writable
            if (!java.nio.file.Files.isWritable(authorImagesDir)) {
                throw new RuntimeException("Author-images directory is not writable: " + authorImagesDir.toString());
            }

            // Delete old author image if it exists
            if (existingBlog.getAuthorImage() != null && !existingBlog.getAuthorImage().isEmpty()) {
                try {
                    String authorImagePath = existingBlog.getAuthorImage();
                    int lastSlashIndex = authorImagePath.lastIndexOf("/");
                    if (lastSlashIndex >= 0 && lastSlashIndex < authorImagePath.length() - 1) {
                        String oldFileName = authorImagePath.substring(lastSlashIndex + 1);
                        java.nio.file.Path oldFilePath = authorImagesDir.resolve(oldFileName);
                        if (java.nio.file.Files.exists(oldFilePath)) {
                            java.nio.file.Files.delete(oldFilePath);
                        }
                    }
                } catch (Exception e) {
                    // Log but don't fail if old file deletion fails
                    System.err.println("Warning: Could not delete old author image file: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                int lastDotIndex = originalFilename.lastIndexOf(".");
                if (lastDotIndex >= 0 && lastDotIndex < originalFilename.length() - 1) {
                    extension = originalFilename.substring(lastDotIndex);
                }
            }

            // Generate unique filename
            String fileName = "blog_" + blogId + "_author_" + System.currentTimeMillis() + extension;
            java.nio.file.Path filePath = authorImagesDir.resolve(fileName);

            // Save file
            try {
                System.out.println("Attempting to save author image to: " + filePath.toString());
                System.out.println("File size: " + file.getSize() + " bytes");
                System.out.println("Directory exists: " + java.nio.file.Files.exists(authorImagesDir));
                System.out.println("Directory is writable: " + java.nio.file.Files.isWritable(authorImagesDir));
                
                // Ensure parent directory exists
                java.nio.file.Files.createDirectories(filePath.getParent());
                
                file.transferTo(filePath.toFile());
                System.out.println("Author image file saved successfully to: " + filePath.toString());
            } catch (java.io.IOException e) {
                System.err.println("IOException during author image transfer:");
                System.err.println("Target path: " + filePath.toString());
                System.err.println("Parent directory exists: " + java.nio.file.Files.exists(filePath.getParent()));
                e.printStackTrace();
                throw new RuntimeException("Failed to transfer author image file to: " + filePath.toString() + " - " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                System.err.println("IllegalStateException during author image transfer:");
                System.err.println("Target path: " + filePath.toString());
                e.printStackTrace();
                throw new RuntimeException("Failed to transfer author image file - file may have been moved or deleted: " + e.getMessage(), e);
            }

            // Update blog with author image path
            existingBlog.setAuthorImage("/author-images/" + fileName);
            blogRepository.save(existingBlog);

            return "/author-images/" + fileName;
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            throw new RuntimeException("Failed to upload author image: " + e.getMessage() + " | Upload dir: " + uploadDir, e);
        }
    }
}
