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
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.practice.jblog.entity.SearchableAttribute;
import com.practice.jblog.entity.SearchableEntity;

import com.practice.jblog.dto.search.FilterRequest;
import com.practice.jblog.dto.search.SearchFilteredRequest;
import com.practice.jblog.dto.search.SearchRequest;
import com.practice.jblog.dto.search.SearchResult;
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
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticAdapter(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void addIndex(String indexName) throws IOException {
        boolean indexExists = indexExists(indexName);
        if (indexExists) {
            elasticsearchClient.indices().delete(c -> c.index(indexName));
        }

        elasticsearchClient.indices().create(createIndexRequest(indexName));
    }

    private CreateIndexRequest createIndexRequest(String indexName) {
        return new CreateIndexRequest.Builder()
                .index(indexName)
                .settings(s -> s
                        .analysis(a -> a
                                .analyzer("stemmer_analyzer", analyzer -> analyzer
                                        .custom(ca -> ca
                                                .tokenizer("standard")
                                                .filter("lowercase", "english_stemmer")
                                        )
                                )
                                .filter(
                                        "english_stemmer", filter -> filter
                                                .definition(n -> n.
                                                        stemmer(stem -> stem.language("english"))
                                                )
                                )
                        )
                )
                .build();
    }

    public boolean indexExists(String indexName) throws IOException {
        BooleanResponse result = elasticsearchClient.
                indices()
                .exists(g -> g.index(indexName));
        return result.value();
    }

    public void addFieldsMapping(String indexName, List<A> attributes) throws IOException {
        elasticsearchClient.indices()
                .putMapping(pm -> {
                            pm.index(indexName);

                            for (A attribute : attributes) {
                                pm.properties(attribute.getCode(), prop -> prop
                                        .text(text -> text
                                                .analyzer("stemmer_analyzer")
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

        elasticsearchClient.bulk(br.build());
    }

    @Transactional
    @Override
    public void reindexRecords(String indexName, List<T> collection) throws IOException {
        BulkRequest.Builder br = prepareBulkRequest(indexName, collection);
        elasticsearchClient.bulk(br.build());
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
    public SearchResult<T> search(SearchRequest request) throws IOException {

        List<Query> musts = new ArrayList<>();
        this.addMustConditions(musts, request.getMustConditions());

        List<String> searchFields = new ArrayList<>();
        Map<String, HighlightField> highlightFields = new HashMap<>();
        request.getSearchFields().forEach((field, priority) -> {
            searchFields.add(field + "^" + priority);
            highlightFields.put(field, HighlightField.of(hf -> hf.numberOfFragments(3)));
        });

        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(request.getIndexName())
                        .query(q -> q
                                .bool(b -> {
                                            b.must(musts);
                                            b.should(ss -> ss.
                                                    multiMatch(m -> m.query(
                                                            request.getQuery())
                                                            .fields(searchFields)
                                                            .fuzziness("AUTO")
                                                    )
                                            );
                                            b.minimumShouldMatch("100%");
                                            return b;
                                        }
                                )
                        )
                        .highlight(Highlight.of(h -> h.type(HighlighterType.Unified)
                                .fields(highlightFields)))
                        .from(request.getPageNumber()),
                request.getSearchEntityType()
        );

        return retrieveResults(result, request.getQuery(), request.getPageNumber());
    }

    @Override
    public SearchResult<T> filter(FilterRequest request) throws IOException {

        List<Query> criterias = new ArrayList<>();
        addFilterConditions(criterias, request.getFilters());
        addMustConditions(criterias, request.getMustConditions());

        List<SortOptions> soList = buildSortOptions(request.getSortOptions());

        SearchResponse<T> result = elasticsearchClient.search(s ->
                        s
                                .index(request.getIndexName())
                                .query(q -> q
                                        .bool(b -> {
                                                    b.must(criterias);
                                                    return b;
                                                }
                                        )
                                )
                                .sort(soList)
                                .from(request.getPageNumber()),
                request.getSearchEntityType()
        );

        return retrieveResults(result, null, request.getPageNumber());
    }

    @Override
    public SearchResult<T> searchFiltered(SearchFilteredRequest request) throws IOException {

        List<Query> musts = new ArrayList<>();
        addMustConditions(musts, request.getMustConditions());
        addFilterConditions(musts, request.getFilters());

        List<String> searchFields = new ArrayList<>();
        Map<String, HighlightField> highlightFields = new HashMap<>();
        request.getSearchFields().forEach((field, priority) -> {
            searchFields.add(field + "^" + priority);
            highlightFields.put(field, HighlightField.of(hf -> hf.numberOfFragments(3)));
        });


        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(request.getIndexName())
                        .query(q -> q
                                .bool(b -> {
                                            b.must(musts);
                                            b.should(ss -> ss.
                                                    multiMatch(m
                                                            -> m.query(request.getQuery())
                                                            .fields(searchFields)
                                                            .fuzziness("AUTO")
                                                    )
                                            );
                                            b.minimumShouldMatch("100%");
                                            return b;
                                        }
                                )
                        )
                        .highlight(Highlight.of(h -> h.type(HighlighterType.Unified)
                                .fields(highlightFields)))
                        .from(request.getPageNumber()),
                request.getSearchEntityType()
        );

        return retrieveResults(result, request.getQuery(), request.getPageNumber());
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

    private SearchResult<T> retrieveResults(SearchResponse<T> result, String query, Integer pageNum) {
        List<T> resultItems = new ArrayList<>();
        Map<String, Map<String, List<String>>> highlights = new HashMap<>();
        long resCount = result.hits().total() != null ? result.hits().total().value() : 0;
        long remainingCount = resCount - pageNum * 10;
        if (remainingCount >= 10) {
            result.hits().hits().forEach(hit -> {
                        resultItems.add(hit.source());
                        if (!hit.highlight().isEmpty()) {
                            highlights.put(hit.source().getIdField(), hit.highlight());
                        }
                    }
            );
        } else {
            result.hits().hits().subList(0, (int)remainingCount).forEach(hit -> {
                        resultItems.add(hit.source());
                        if (!hit.highlight().isEmpty()) {
                            highlights.put(hit.source().getIdField(), hit.highlight());
                        }
                    }
            );
        }

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
                    return "/search/" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                            + ((pageNum != null && pageNum != 0) ? "/" + (pageNum + 1) : "");
                }
                return null;
            }

            @Override
            public long getResultsCount() {
                return resCount;
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
