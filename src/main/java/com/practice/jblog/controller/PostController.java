package com.practice.jblog.controller;

import com.practice.jblog.entity.Post;
import com.practice.jblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@CrossOrigin("${frontend.origin}")
public class PostController {
    private PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/{urlKey}")
    public ResponseEntity<Post> getPostByUrlKey(@PathVariable String urlKey) {
        return new ResponseEntity<>(postRepository.findByUrlKey(urlKey), HttpStatus.OK);
    }
}
