package com.practice.jblog.controller;

import com.practice.jblog.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@CrossOrigin("${frontend.origin}")
public class PostController {
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/{urlKey}")
    public ResponseEntity<?> getPostByUrlKey(@PathVariable String urlKey) {
        return new ResponseEntity<>(postRepository.findByUrlKey(urlKey), HttpStatus.OK);
    }
}
