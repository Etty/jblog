package com.practice.jblog.dto.search;

import com.practice.jblog.Entity.SearchableEntity;

import java.util.List;
import java.util.Map;

public interface SearchResult <T extends SearchableEntity> {
    List<T> getResultItems();
    Map<String, Map<String, List<String>>> getHighlights();
    String getSearchResultsLink();
    long getResultsCount();
}
