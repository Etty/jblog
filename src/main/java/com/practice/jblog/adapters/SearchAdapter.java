package com.practice.jblog.adapters;

import com.practice.jblog.Entity.SearchableAttribute;
import com.practice.jblog.Entity.SearchableEntity;
import com.practice.jblog.dto.search.FilterRequest;
import com.practice.jblog.dto.search.SearchFilteredRequest;
import com.practice.jblog.dto.search.SearchRequest;
import com.practice.jblog.dto.search.SearchResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SearchAdapter<T extends SearchableEntity, A extends SearchableAttribute> {
    @Transactional
    void reindexFull(String indexName, List<T> collection, List<A> attributes) throws IOException;

    @Transactional
    void reindexRecords(String indexName, List<T> collection) throws IOException;

    @Transactional
    void reindexOne(String indexName, T entity) throws IOException;

    SearchResult<T> search(SearchRequest request) throws IOException;

    SearchResult<T> filter(FilterRequest request) throws IOException;

    SearchResult<T> searchFiltered(SearchFilteredRequest request) throws IOException;
}
