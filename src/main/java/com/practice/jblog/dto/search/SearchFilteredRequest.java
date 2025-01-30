package com.practice.jblog.dto.search;

import java.util.List;
import java.util.Map;

public interface SearchFilteredRequest extends SearchRequest{
    Map<String, List<String>> getFilters();
}
