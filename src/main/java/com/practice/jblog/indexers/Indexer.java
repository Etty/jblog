package com.practice.jblog.indexers;

import java.io.IOException;

public interface Indexer {
    /**
     * Execute a full reindex
     * Rebuild index & mappings
     */
    void reindexFull() throws IOException;

    /**
     * Update entities data only (documents) w/o index recreation
     */
    void reindexRecords(Long... entityIds) throws IOException;

    /**
     * Update single entity in the index
     */
    void reindexOne(Long entityId) throws IOException;

    String getIndexName();
}
