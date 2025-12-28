package com.princeworks.blogger.repositories;

import com.princeworks.blogger.model.Blog;
import com.princeworks.blogger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByUser(User user);
    List<Blog> findByUserUserId(Long userId);
}
