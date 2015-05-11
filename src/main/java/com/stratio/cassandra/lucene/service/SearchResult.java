/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.cassandra.lucene.service;

import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.composites.CellName;
import org.apache.lucene.search.ScoreDoc;

/**
 * Class representing the a result of a search in Lucene. Its roughly formed by the row identifier and the search hit.
 *
 * @author Andres de la Pena <adelapena@stratio.com>
 */
public class SearchResult {

    private final DecoratedKey partitionKey; // The partition key
    private final CellName clusteringKey; // The clustering key
    private final ScoreDoc scoreDoc; // The search hit info

    /**
     * Builds a new {@link SearchResult} for the specified CQL3 row key and search hit info.
     *
     * @param partitionKey  The partition key.
     * @param clusteringKey The clustering key.
     * @param scoreDoc      The search hit info.
     */
    public SearchResult(DecoratedKey partitionKey, CellName clusteringKey, ScoreDoc scoreDoc) {
        this.partitionKey = partitionKey;
        this.clusteringKey = clusteringKey;
        this.scoreDoc = scoreDoc;
    }

    /**
     * Returns the partition key.
     *
     * @return The partition key.
     */
    public DecoratedKey getPartitionKey() {
        return partitionKey;
    }

    /**
     * Returns the clustering key.
     *
     * @return The clustering key.
     */
    public CellName getClusteringKey() {
        return clusteringKey;
    }

    /**
     * Returns the search {@link ScoreDoc}.
     *
     * @return The search {@link ScoreDoc}.
     */
    public ScoreDoc getScoreDoc() {
        return scoreDoc;
    }

    /**
     * Returns the search numeric score.
     *
     * @return The search numeric score.
     */
    public Float getScore() {
        return scoreDoc.score;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchResult that = (SearchResult) o;

        return scoreDoc.doc == that.scoreDoc.doc;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return scoreDoc.doc;
    }
}
