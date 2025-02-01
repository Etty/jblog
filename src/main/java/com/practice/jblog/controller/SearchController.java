package com.practice.jblog.controller;

import com.practice.jblog.service.ElasticSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private ElasticSearcher searcher;

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
