package com.practice.jblog;

import com.practice.jblog.Entity.Post;
import com.practice.jblog.Repository.PostRepository;
import com.practice.jblog.controller.PostController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(PostController.class)
public class FetchPostTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostController postController;

    @MockitoBean
    private PostRepository postRepository;

    @Test
    void fetchPost()  throws Exception{
        when(postRepository.findByUrlKey("cool-post")).thenReturn(new Post()
                .setId(1L)
                .setIsEnabled((byte)1)
                .setPostIdentifier("post-1")
                .setTitle("Cool Post")
                .setImage("https://site.com/media/post/image.png")
                .setDescription("A cool story from some post here!")
                .setUrlKey("cool-post")
                .setCreatedAt(Instant.parse("2025-02-10T21:12:25.205302Z"))
                .setUpdatedAt(Instant.parse("2025-02-10T21:12:25.205302Z"))
        );

        this.mockMvc.perform(get("/post/cool-post")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("cool-post")));
    }
}
