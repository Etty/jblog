package com.practice.jblog.adapters;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.practice.jblog.Entity.SearchableAttribute;
import com.practice.jblog.Entity.SearchableEntity;

import com.practice.jblog.dto.SearchResult;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ElasticAdapter<T extends SearchableEntity, A extends SearchableAttribute>
        implements DisposableBean, SearchAdapter<T, A> {

    @Autowired
    private ElasticsearchClient elasticsearchClient;
    private TermsQueryField.Builder tqf;

    public void addIndex(String indexName) throws IOException {
        boolean indexExists = indexExists(indexName);
        if (indexExists) {
            elasticsearchClient.indices().delete(c -> c.index(indexName));
        }

        elasticsearchClient.
                indices()
                .create(c -> c.index(indexName));
    }

    public boolean indexExists(String indexName) throws IOException {
        BooleanResponse result = elasticsearchClient.
                indices()
                .exists(g -> g.index(indexName));
        return result.value();
    }

    public void addFieldsMapping(String indexName, List<A> attributes) throws IOException {
        elasticsearchClient.indices().putMapping(pm -> {
                    pm.index(indexName);

                    for (A attribute : attributes) {
                        pm.properties(attribute.getCode(), prop -> prop
                                .text(text -> text
                                        .analyzer("standard")
                                        .fields("keyword", keyword -> keyword.keyword(k -> k))
                                ));
                    }
                    return pm;
                }
        );
    }

    @Transactional
    @Override
    public void reindexFull(String indexName, List<T> collection, List<A> attributes) throws IOException {
        this.addIndex(indexName);
        this.addFieldsMapping(indexName, attributes);

        BulkRequest.Builder br = prepareBulkRequest(indexName, collection);

        BulkResponse result = elasticsearchClient.bulk(br.build());
    }

    @Transactional
    @Override
    public void reindexRecords(String indexName, List<T> collection) throws IOException {
        BulkRequest.Builder br = prepareBulkRequest(indexName, collection);
        BulkResponse result = elasticsearchClient.bulk(br.build());
    }

    @Transactional
    @Override
    public void reindexOne(String indexName, T entity) throws IOException {
        elasticsearchClient.index(i -> i
                .index(indexName)
                .id(entity.getIdField())
                .document(entity)
        );
    }

    @Override
    public SearchResult<T> search(String query,
                                  int pageNumber,
                                  Map<String, String> mustConditions,
                                  Map<String, Integer> fields,
                                  String indexName,
                                  Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        this.addMustConditions(musts, mustConditions);

        List<String> searchFields = new ArrayList<>();
        Map<String, HighlightField> highlightFields = new HashMap<>();
        fields.forEach((field, priority) -> {
            searchFields.add(field + "^" + priority);
            highlightFields.put(field, HighlightField.of(hf -> hf.numberOfFragments(3)));
        });

        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .bool(b -> {
                                            b.must(musts);
                                            b.should(ss -> ss.
                                                    multiMatch(m -> m.query(query).fields(searchFields))
                                            );
                                            return b;
                                        }
                                )
                        )
                        .highlight(Highlight.of(h -> h.type(HighlighterType.Unified)
                                .fields(highlightFields)))
                        .from(pageNumber),
                searchEntityType
        );

        return retrieveResults(result, query);
    }

    @Override
    public SearchResult<T> filter(Map<String, List<String>> filters,
                                  int pageNumber,
                                  Map<String, String> mustConditions,
                                  Map<String, String> sortOptions,
                                  String indexName,
                                  Class<T> searchEntityType) throws IOException {

        List<Query> criterias = new ArrayList<>();
        addFilterConditions(criterias, filters);
        addMustConditions(criterias, mustConditions);

        List<SortOptions> soList = buildSortOptions(sortOptions);

        SearchResponse<T> result = elasticsearchClient.search(s ->
                        s
                                .index(indexName)
                                .query(q -> q
                                        .bool(b -> {
                                                    b.must(criterias);
                                                    return b;
                                                }
                                        )
                                )
                                .sort(soList)
                                .from(pageNumber),
                searchEntityType
        );

        return retrieveResults(result, null);
    }

    @Override
    public SearchResult<T> searchFiltered(String query,
                                          int pageNumber,
                                          Map<String, List<String>> filters,
                                          Map<String, String> mustConditions,
                                          Map<String, Integer> fields,
                                          String indexName,
                                          Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        addMustConditions(musts, mustConditions);
        addFilterConditions(musts, filters);

        List<String> searchFields = new ArrayList<>();
        Map<String, HighlightField> highlightFields = new HashMap<>();
        fields.forEach((field, priority) -> {
            searchFields.add(field + "^" + priority);
            highlightFields.put(field, HighlightField.of(hf -> hf.numberOfFragments(3)));
        });


        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .bool(b -> {
                                            b.must(musts);
                                            b.should(ss -> ss.
                                                    multiMatch(m -> m.query(query).fields(searchFields))
                                            );
                                            return b;
                                        }
                                )
                        )
                        .highlight(Highlight.of(h -> h.type(HighlighterType.Unified)
                                .fields(highlightFields)))
                        .from(pageNumber),
                searchEntityType
        );

        return retrieveResults(result, query);
    }

    private void addMustConditions(List<Query> conds, Map<String, String> mustConditions) {
        mustConditions.forEach((field, value) -> conds.add(MatchQuery.of(m -> m
                .field(field)
                .query(value)
        )._toQuery()));
    }

    private void addFilterConditions(List<Query> conds, Map<String, List<String>> filters) {
        filters.forEach((field, value) -> conds.add(TermsQuery.of(m -> {
                    List<FieldValue> termValues = new ArrayList<>();
                    for (String termValue : value) {
                        termValues.add(FieldValue.of(termValue));
                    }
                    TermsQueryField termsQueryField = new TermsQueryField.Builder()
                            .value(termValues)
                            .build();
                    m
                            .field(field + ".keyword")
                            .terms(termsQueryField);
                    return m;
                }
        )._toQuery()));
    }

    private List<SortOptions> buildSortOptions(Map<String, String> sortOptions) {
        List<SortOptions> soList = new ArrayList<>();
        sortOptions.forEach((field, order) -> soList.add(SortOptions.of(s -> s
                                .field(FieldSort.of(f -> f
                                                .field(field)
                                                .order(order.equalsIgnoreCase("desc")
                                                        ? SortOrder.Desc : SortOrder.Asc)
                                        )
                                )
                        )
                )
        );
        return soList;
    }

    private SearchResult<T> retrieveResults(SearchResponse<T> result, String query) {
        List<T> resultItems = new ArrayList<>();
        Map<String, Map<String, List<String>>> highlights = new HashMap<>();

        result.hits().hits().forEach(hit -> {
                    resultItems.add(hit.source());
                    if (!hit.highlight().isEmpty()) {
                        highlights.put(hit.source().getIdField(), hit.highlight());
                        if (highlights.get(hit.source().getIdField()).get("title") == null) {
                            List<String> title = hit.highlight().get("title") != null
                                    ? hit.highlight().get("title")
                                    : List.of(hit.source().getTitle());
                            highlights.get(hit.source().getIdField()).put("title", title);
                        }
                    }
                }
        );
        return new SearchResult<>() {
            @Override
            public List<T> getResultItems() {
                return resultItems;
            }

            @Override
            public Map<String, Map<String, List<String>>> getHighlights() {
                return highlights;
            }

            @Override
            public String getSearchResultsLink() {
                if (query != null) {
                    return "/search/" + URLEncoder.encode(query, StandardCharsets.UTF_8);
                }
                return null;
            }

            @Override
            public long getResultsCount() {
                return result.hits().total() != null ? result.hits().total().value() : 0;
            }
        };
    }

    private BulkRequest.Builder prepareBulkRequest(String indexName, List<T> entities) {
        BulkRequest.Builder br = new BulkRequest.Builder();
        for (T entity : entities) {
            br.operations(op -> {
                        op
                                .index(idx -> idx
                                        .index(indexName)
                                        .id(entity.getIdField())
                                        .document(entity)
                                );
                        return op;
                    }
            );
        }

        return br;
    }

    @Override
    public void destroy() throws Exception {
        elasticsearchClient.close();
    }
}
