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
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.practice.jblog.Entity.SearchableAttribute;
import com.practice.jblog.Entity.SearchableEntity;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
    public List<T> search(String query,
                          Map<String, String> mustConditions,
                          Map<String, Integer> fields,
                          String indexName,
                          Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        this.addMustConditions(musts, mustConditions);

        List<String> searchFields = new ArrayList<>();
        fields.forEach((field, priority) -> searchFields.add(field + "^" + priority));

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
                        ),
                searchEntityType
        );

        return retrieveResults(result);
    }

    @Override
    public List<T> filter(Map<String, List<String>> filters,
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
                                ).sort(soList)
                ,
                searchEntityType
        );

        return retrieveResults(result);
    }

    @Override
    public List<T> searchFiltered(String query,
                                  Map<String, List<String>> filters,
                                  Map<String, String> mustConditions,
                                  Map<String, Integer> fields,
                                  Map<String, String> sortOptions,
                                  String indexName,
                                  Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        addMustConditions(musts, mustConditions);
        addFilterConditions(musts, filters);

        List<String> searchFields = new ArrayList<>();
        fields.forEach((field, priority) -> searchFields.add(field + "^" + priority));

        List<SortOptions> soList = buildSortOptions(sortOptions);

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
                        ).sort(soList),
                searchEntityType
        );

        return retrieveResults(result);
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

    private List<T> retrieveResults(SearchResponse<T> result) {
        List<T> searchResults = new ArrayList<>();

        result.hits().hits().forEach(hit -> searchResults.add(
                hit.source()
        ));

        return searchResults;
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
