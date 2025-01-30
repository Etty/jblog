package com.practice.jblog.dto.search;

import com.practice.jblog.Entity.SearchableEntity;

import java.util.List;
import java.util.Map;

public interface FilterRequest {
    Map<String, List<String>> getFilters();
    int getPageNumber();
    Map<String, String> getMustConditions();
    Map<String, String> getSortOptions();
    String getIndexName();
    <T extends SearchableEntity> Class<T> getSearchEntityType();
}
