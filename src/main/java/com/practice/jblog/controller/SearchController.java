package com.practice.jblog.controller;

import com.practice.jblog.service.ElasticSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin("${frontend.origin}")
public class SearchController {
    private ElasticSearcher searcher;

    @Autowired
    public SearchController(ElasticSearcher searcher) {
        this.searcher = searcher;
    }

    @GetMapping("/{q}")
    public ResponseEntity<?> getSearchResult(@PathVariable String q) {
        try {
            return new ResponseEntity<>(searcher.search(q), HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<Map<String, String>>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{q}/{p}")
    public ResponseEntity<?> getSearchResult(@PathVariable String q, @PathVariable int p) {
        try {
            return new ResponseEntity<>(searcher.search(q, p), HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<Map<String, String>>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
