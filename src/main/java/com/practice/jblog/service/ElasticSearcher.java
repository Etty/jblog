package com.practice.jblog.service;

import com.practice.jblog.entity.Post;
import com.practice.jblog.entity.PostAttributeDefinition;
import com.practice.jblog.adapters.ElasticAdapter;
import com.practice.jblog.dto.search.FilterRequest;
import com.practice.jblog.dto.search.SearchFilteredRequest;
import com.practice.jblog.dto.search.SearchRequest;
import com.practice.jblog.dto.search.SearchResult;
import com.practice.jblog.indexers.PostsIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.practice.jblog.entity.Post.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticSearcher {
    private ElasticAdapter<Post, PostAttributeDefinition> elasticAdapter;

    private PostsIndexer postsIndexer;

    private PostAttributesService postAttributesService;

    @Autowired
    public ElasticSearcher (
            ElasticAdapter<Post, PostAttributeDefinition> elasticAdapter,
            PostsIndexer postsIndexer,
            PostAttributesService postAttributesService) {
        this.elasticAdapter = elasticAdapter;
        this.postsIndexer = postsIndexer;
        this.postAttributesService = postAttributesService;
    }

    public SearchResult<Post> search(String query, int pageNum) throws IOException {
        HashMap<String, String> musts = new HashMap<>();
        musts.put(IS_ENABLED_FIELD, "1");
        HashMap<String, Integer> fields = new HashMap<>();
        List<PostAttributeDefinition> searchableAttrs = postAttributesService.getSearchableAttributes();
        for (PostAttributeDefinition attr : searchableAttrs) {
            fields.put(attr.getCode(), attr.getSearchWeight());
        }

        return elasticAdapter.search(new SearchRequest() {
            @Override
            public String getQuery() {
                return query;
            }

            @Override
            public int getPageNumber() {
                return pageNum;
            }

            @Override
            public Map<String, String> getMustConditions() {
                return musts;
            }

            @Override
            public Map<String, Integer> getSearchFields() {
                return fields;
            }

            @Override
            public String getIndexName() {
                return postsIndexer.getIndexName();
            }

            @Override
            public Class<Post> getSearchEntityType() {
                return Post.class;
            }
        });
    }

    public SearchResult<Post> search(String query) throws IOException {
        return search(query, 0);
    }

    public SearchResult<Post> filter(Map<String, List<String>> filters, int pageNum) throws IOException {
        HashMap<String, String> musts = new HashMap<>();
        musts.put(IS_ENABLED_FIELD, "1");
        HashMap<String, String> so = new HashMap<>();
        so.put("id", "desc");

        return elasticAdapter.filter(new FilterRequest() {
            @Override
            public Map<String, List<String>> getFilters() {
                return filters;
            }

            @Override
            public int getPageNumber() {
                return pageNum;
            }

            @Override
            public Map<String, String> getMustConditions() {
                return musts;
            }

            @Override
            public Map<String, String> getSortOptions() {
                return so;
            }

            @Override
            public String getIndexName() {
                return postsIndexer.getIndexName();
            }

            @Override
            public Class<Post> getSearchEntityType() {
                return Post.class;
            }
        });
    }

    public SearchResult<Post> filter(Map<String, List<String>> filters) throws IOException {
        return filter(filters, 0);
    }

    public SearchResult<Post> searchFiltered(String query, Map<String,
                                                     List<String>> filters,
                                             int pageNum) throws IOException {

        HashMap<String, String> musts = new HashMap<>();
        musts.put(IS_ENABLED_FIELD, "1");
        HashMap<String, Integer> fields = new HashMap<>();
        List<PostAttributeDefinition> searchableAttrs = postAttributesService.getSearchableAttributes();
        for (PostAttributeDefinition attr : searchableAttrs) {
            fields.put(attr.getCode(), attr.getSearchWeight());
        }

        return elasticAdapter.searchFiltered(new SearchFilteredRequest() {
            @Override
            public Map<String, List<String>> getFilters() {
                return filters;
            }

            @Override
            public String getQuery() {
                return query;
            }

            @Override
            public int getPageNumber() {
                return pageNum;
            }

            @Override
            public Map<String, String> getMustConditions() {
                return musts;
            }

            @Override
            public Map<String, Integer> getSearchFields() {
                return fields;
            }

            @Override
            public String getIndexName() {
                return postsIndexer.getIndexName();
            }

            @Override
            public Class<Post> getSearchEntityType() {
                return Post.class;
            }
        });
    }

    public SearchResult<Post> searchFiltered(String query, Map<String, List<String>> filters) throws IOException {
        return searchFiltered(query, filters, 0);
    }
}
