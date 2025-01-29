package com.practice.jblog.adapters;

import com.practice.jblog.Entity.SearchableAttribute;
import com.practice.jblog.Entity.SearchableEntity;
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

    List<T> search(String query,
                   Map<String, String> mustConditions,
                   Map<String, Integer> fields,
                   String indexName,
                   Class<T> searchEntityType) throws IOException;

    List<T> filter(Map<String, List<String>> filters,
                   Map<String, String> mustConditions,
                   Map<String, String> sortOptions,
                   String indexName,
                   Class<T> searchEntityType) throws IOException;

    List<T> searchFiltered(String query,
                           Map<String, List<String>> filters,
                           Map<String, String> mustConditions,
                           Map<String, Integer> fields,
                           Map<String, String> sortOptions,
                           String indexName,
                           Class<T> searchEntityType) throws IOException;
}
