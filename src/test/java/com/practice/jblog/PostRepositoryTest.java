package com.practice.jblog;

import com.practice.jblog.entity.Post;
import com.practice.jblog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = { "classpath:application.properties" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private PostRepository postRepository;

    @BeforeEach
    public void setUp() {
        Post post = new Post().setUrlKey("test-post");
        Mockito.when(postRepository.findByUrlKey(post.getUrlKey()))
                .thenReturn(post);
    }

    @Test
    void testGetEntityByUrlKey() throws Exception {
        String urlKey = "test-post";
        Post found = postRepository.findByUrlKey(urlKey);

        assertThat(found.getUrlKey())
                .isEqualTo(urlKey);
    }
}
