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

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.RowPosition;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.dht.Token;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;

/**
 * Class for several row partitioning {@link Token} mappings between Cassandra and Lucene.
 *
 * @author Andres de la Pena <adelapena@stratio.com>
 */
public abstract class TokenMapper {

    /**
     * Returns a new {@link TokenMapper} instance for the current partitioner using the specified column family
     * metadata.
     *
     * @return A new {@link TokenMapper} instance for the current partitioner.
     */
    public static TokenMapper instance() {
        IPartitioner partitioner = DatabaseDescriptor.getPartitioner();
        if (partitioner instanceof Murmur3Partitioner) {
            return new TokenMapperMurmur();
        } else {
            return new TokenMapperGeneric();
        }
    }

    /**
     * Adds to the specified {@link Document} the {@link org.apache.lucene.document.Field}s associated to the token of
     * the specified row key.
     *
     * @param document     A {@link Document}.
     * @param partitionKey The raw partition key to be added.
     */
    public abstract void addFields(Document document, DecoratedKey partitionKey);

    /**
     * Returns a Lucene {@link Query} for retrieving the documents inside the specified {@link Token} range.
     *
     * @param lower        The lower accepted {@link Token}. Maybe null meaning no lower limit.
     * @param upper        The upper accepted {@link Token}. Maybe null meaning no lower limit.
     * @param includeLower If the {@code lowerValue} is included in the range.
     * @param includeUpper If the {@code upperValue} is included in the range.
     * @return A Lucene {@link Query} for retrieving the documents inside the specified {@link Token} range.
     */
    public Query query(Token lower, Token upper, boolean includeLower, boolean includeUpper) {
        if (lower != null && upper != null && isMinimum(lower) && isMinimum(upper) && (includeLower || includeUpper)) {
            return null;
        } else {
            return makeQuery(lower, upper, includeLower, includeUpper);
        }
    }

    /**
     * Returns {@code true} if the specified {@link Token} is the minimum accepted by the {@link IPartitioner}, {@code
     * false} otherwise.
     *
     * @param token A {@link Token}.
     * @return {@code true} if the specified {@link Token} is the minimum accepted by the {@link IPartitioner}, {@code
     * false} otherwise.
     */
    public boolean isMinimum(Token token) {
        Token minimum = DatabaseDescriptor.getPartitioner().getMinimumToken();
        return token.compareTo(minimum) == 0;
    }

    /**
     * Returns a Lucene {@link Query} for retrieving the documents with the specified {@link Token}.
     *
     * @param token A {@link Token}.
     * @return A Lucene {@link Query} for retrieving the documents with the specified {@link Token}.
     */
    public abstract Query query(Token token);

    /**
     * Returns a Lucene {@link Query} for retrieving the documents inside the specified {@link Token} range.
     *
     * @param lower        The lower accepted {@link Token}. Maybe null meaning no lower limit.
     * @param upper        The upper accepted {@link Token}. Maybe null meaning no lower limit.
     * @param includeLower If the {@code lowerValue} is included in the range.
     * @param includeUpper If the {@code upperValue} is included in the range.
     * @return A Lucene {@link Query} for retrieving the documents inside the specified {@link Token} range.
     */
    protected abstract Query makeQuery(Token lower, Token upper, boolean includeLower, boolean includeUpper);

    /**
     * Returns a Lucene {@link SortField} array for sorting documents/rows according to the current partitioner.
     *
     * @return A Lucene {@link SortField} array for sorting documents/rows according to the current partitioner.
     */
    public abstract SortField[] sortFields();

    /**
     * Returns {@code true} if the specified lower row position kind must be included in the filtered range, {@code
     * false} otherwise.
     *
     * @param rowPosition A {@link RowPosition}.
     * @return {@code true} if the specified lower row position kind must be included in the filtered range, {@code
     * false} otherwise.
     */
    public boolean includeStart(RowPosition rowPosition) {
        switch (rowPosition.kind()) {
            case MAX_BOUND:
                return false;
            case MIN_BOUND:
                return true;
            case ROW_KEY:
                return true;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Returns {@code true} if the specified upper row position kind must be included in the filtered range, {@code
     * false} otherwise.
     *
     * @param rowPosition A {@link RowPosition}.
     * @return {@code true} if the specified upper row position kind must be included in the filtered range, {@code
     * false} otherwise.
     */
    public boolean includeStop(RowPosition rowPosition) {
        switch (rowPosition.kind()) {
            case MAX_BOUND:
                return true;
            case MIN_BOUND:
                return false;
            case ROW_KEY:
                return true;
            default:
                throw new IllegalArgumentException();
        }
    }

}
