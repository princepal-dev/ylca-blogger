package com.princeworks.blogger.repositories;

import com.princeworks.blogger.model.Blog;
import com.princeworks.blogger.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByBlog(Blog blog);
    List<Image> findByBlogBlogId(Long blogId);
    List<Image> findByBlogBlogIdOrderByDisplayOrderAsc(Long blogId);
    void deleteByBlogBlogId(Long blogId);
}
