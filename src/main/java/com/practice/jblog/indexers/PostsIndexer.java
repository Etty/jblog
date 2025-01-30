package com.practice.jblog.indexers;

import com.practice.jblog.Entity.Post;
import com.practice.jblog.Entity.PostAttributeDefinition;
import com.practice.jblog.Repository.PostRepository;
import com.practice.jblog.config.IndexPrefix;
import com.practice.jblog.adapters.ElasticAdapter;
import com.practice.jblog.service.PostAttributesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class PostsIndexer implements Indexer {
    @Autowired
    private IndexPrefix indexPrefix;

    @Autowired
    private ElasticAdapter<Post, PostAttributeDefinition> elasticAdapter;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostAttributesService postAttributesService;

    @Transactional
    @Override
    public void reindexFull() throws IOException {
        elasticAdapter.reindexFull(getIndexName(), postRepository.findAll(), prepareAttributes());
    }

    @Override
    @Transactional
    public void reindexRecords(Long... postIds) throws IOException {
        List<Post> posts;
        if (postIds == null || postIds.length == 0) {
            posts = postRepository.findAll();
        } else {
            posts = postRepository.findAllById(List.of(postIds));
        }

        elasticAdapter.reindexRecords(getIndexName(), posts);
    }

    @Override
    @Transactional
    public void reindexOne(Long postId) throws IOException {
        Optional<Post> post = postRepository.findById(postId);
        if (!post.isPresent()) {
            throw new IllegalArgumentException("Post with id " + postId + " not found");
        }
        elasticAdapter.reindexOne(getIndexName(), post.get());
    }

    public String getIndexName() {
        return indexPrefix.getPrefix() + "posts";
    }

    private List<PostAttributeDefinition> prepareAttributes() {
        List<PostAttributeDefinition> attrs = postAttributesService.getSearchableAttributes();
        attrs.add(new PostAttributeDefinition().setCode("is_enabled"));
        attrs.add(new PostAttributeDefinition().setCode("categoryIds"));
        return attrs;
    }

}
