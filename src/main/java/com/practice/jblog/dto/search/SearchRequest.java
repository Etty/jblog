package com.practice.jblog.dto.search;

import com.practice.jblog.Entity.SearchableEntity;

import java.util.Map;

public interface SearchRequest {
    String getQuery();
    int getPageNumber();
    Map<String, String> getMustConditions();
    Map<String, Integer> getSearchFields();
    String getIndexName();
    <T extends SearchableEntity> Class<T> getSearchEntityType();
}
