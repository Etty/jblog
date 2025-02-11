package com.practice.jblog;

import com.practice.jblog.controller.PostController;
import com.practice.jblog.controller.SearchController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SmokeTest {
    @Autowired
    private PostController postController;

    @Autowired
    private SearchController searchController;

    @Test
    void contextLoads() throws Exception {
//        ensure that context is creating controller
        assertThat(postController).isNotNull();
        assertThat(searchController).isNotNull();
    }
}
