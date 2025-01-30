package com.practice.jblog.adapters;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
public class ElasticService_back<T extends SearchableEntity, A extends SearchableAttribute> {

    @Autowired
    private ElasticsearchClient elasticsearchClient;
    private TermsQueryField.Builder tqf;

    public void addIndex(String indexName) throws IOException {
        boolean indexExists = indexExists(indexName);
        if (indexExists) {
            elasticsearchClient.indices().delete(c -> c.index(indexName));
        }

        elasticsearchClient.
                indices().
                create(c -> c.index(indexName));
    }

    public boolean indexExists(String indexName) throws IOException {
        BooleanResponse result = elasticsearchClient.
                indices().
                exists(g -> g.index(indexName));
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
    public void reindexFull(String indexName, List<T> collection, List<A> attributes) throws IOException {
        this.addIndex(indexName);
        this.addFieldsMapping(indexName, attributes);

        BulkRequest.Builder br = prepareBulkRequest(indexName, collection);

        BulkResponse result = elasticsearchClient.bulk(br.build());
    }

    @Transactional
    public void reindexRecords(String indexName, List<T> collection) throws IOException {
        BulkRequest.Builder br = prepareBulkRequest(indexName, collection);
        BulkResponse result = elasticsearchClient.bulk(br.build());
    }

    @Transactional
    public void reindexOne(String indexName, T entity) throws IOException {
        elasticsearchClient.index(i -> i
                .index(indexName)
                .id(entity.getIdField())
                .document(entity)
        );
    }

    public List<T> search(String query,
                          Map<String, String> mustConditions,
                          Map<String, Integer> fields,
                          String indexName,
                          Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        mustConditions.forEach((field, value) -> musts.add(MatchQuery.of(m -> m
                .field(field)
                .query(value)
        )._toQuery()));

        List<String> searchFields = new ArrayList<>();
        fields.forEach((field, priority) -> searchFields.add(field + "^" + priority));

        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .bool(b -> {
                                            for (Query qm : musts) {
                                                b.must(qm);
                                            }
                                            b.should(ss -> ss.
                                                    multiMatch(m -> m.query(query).fields(searchFields))
                                            );
                                            return b;
                                        }
                                )
                        ),
                searchEntityType
        );

        List<T> searchResults = new ArrayList<>();

        result.hits().hits().forEach(hit -> searchResults.add(
                hit.source()
        ));

        return searchResults;
    }

    public List<T> filter(Map<String, List<String>> filters, String indexName, Class<T> searchEntityType) throws IOException {
        List<Query> filterPart = new ArrayList<>();
        filters.forEach((field, value) -> filterPart.add(TermsQuery.of(m -> {
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

        SearchResponse<T> result = elasticsearchClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .bool(b -> {
                                            b.filter(filterPart);
                                            return b;
                                        }
                                )
                        ),
                searchEntityType
        );

        List<T> searchResults = new ArrayList<>();

        result.hits().hits().forEach(hit -> searchResults.add(
                hit.source()
        ));

        return searchResults;
    }

    public List<T> searchFiltered(String query,
                                  Map<String, List<String>> filters,
                                  Map<String, String> mustConditions,
                                  Map<String, Integer> fields,
                                  String indexName,
                                  Class<T> searchEntityType) throws IOException {

        List<Query> musts = new ArrayList<>();
        mustConditions.forEach((field, value) -> musts.add(MatchQuery.of(m -> m
                .field(field)
                .query(value)
        )._toQuery()));

        filters.forEach((field, value) -> musts.add(TermsQuery.of(m -> {
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


}
