package com.practice.jblog.Repository;

import com.practice.jblog.Entity.Post;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findAllById(Iterable<Long> ids);

    List<Post> findAll();

}
