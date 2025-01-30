package com.practice.jblog.service;

import com.practice.jblog.Entity.Post;
import com.practice.jblog.Entity.PostAttributeDefinition;
import com.practice.jblog.adapters.ElasticAdapter;
import com.practice.jblog.dto.SearchResult;
import com.practice.jblog.indexers.PostsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearcher {
    @Autowired
    private ElasticAdapter<Post, PostAttributeDefinition> elasticAdapter;

    @Autowired
    private PostsIndexer postsIndexer;

    @Autowired
    private PostAttributesService postAttributesService;

    public SearchResult<Post> search(String query) throws IOException {
        HashMap<String, String> musts = new HashMap<>();
        musts.put("isEnabled", "1");
        HashMap<String, Integer> fields = new HashMap<>();
        List<PostAttributeDefinition> searchableAttrs = postAttributesService.getSearchableAttributes();
        for (PostAttributeDefinition attr: searchableAttrs) {
            fields.put(attr.getCode(), attr.getSearchWeight());
        }
        return elasticAdapter.search(query, 0, musts, fields, postsIndexer.getIndexName(), Post.class);
    }

    public SearchResult<Post> filter(Map<String, List<String>> filters) throws IOException {
        HashMap<String, String> musts = new HashMap<>();
        musts.put("isEnabled", "1");
        HashMap<String, String> so = new HashMap<>();
        so.put("id", "desc");

        return elasticAdapter.filter(filters, 0, musts, so, postsIndexer.getIndexName(), Post.class);
    }

    public SearchResult<Post> searchFiltered(String query, Map<String, List<String>> filters) throws IOException {
        HashMap<String, String> musts = new HashMap<>();
        musts.put("isEnabled", "1");
        HashMap<String, Integer> fields = new HashMap<>();
        List<PostAttributeDefinition> searchableAttrs = postAttributesService.getSearchableAttributes();
        for (PostAttributeDefinition attr: searchableAttrs) {
            fields.put(attr.getCode(), attr.getSearchWeight());
        }

        return elasticAdapter.searchFiltered(query, 0, filters, musts, fields, postsIndexer.getIndexName(), Post.class);
    }
}
