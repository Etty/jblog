package com.practice.jblog.repository;

import com.practice.jblog.entity.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findAllById(Iterable<Long> ids);

    List<Post> findAll();

    Post findByUrlKey(String urlKey);

}
