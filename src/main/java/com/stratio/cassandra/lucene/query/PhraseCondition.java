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
package com.stratio.cassandra.lucene.query;

import com.google.common.base.Objects;
import com.stratio.cassandra.lucene.schema.Schema;
import com.stratio.cassandra.lucene.schema.mapping.ColumnMapperSingle;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A {@link Condition} implementation that matches documents containing a particular sequence of terms.
 *
 * @author Andres de la Pena <adelapena@stratio.com>
 */
public class PhraseCondition extends SingleFieldCondition {

    /** The default umber of other words permitted between words in phrase. */
    public static final int DEFAULT_SLOP = 0;

    /** The name of the field to be matched. */
    @JsonProperty("field")
    private final String field;

    /** The phrase terms to be matched. */
    @JsonProperty("value")
    private final String value;

    /** The number of other words permitted between words in phrase. */
    @JsonProperty("slop")
    private int slop;

    /**
     * Constructor using the field name and the value to be matched.
     *
     * @param boost The boost for this query clause. Documents matching this clause will (in addition to the normal
     *              weightings) have their score multiplied by {@code boost}. If {@code null}, then {@link
     *              #DEFAULT_BOOST} is used as default.
     * @param field The name of the field to be matched.
     * @param value The phrase terms to be matched.
     * @param slop  The number of other words permitted between words in phrase.
     */
    @JsonCreator
    public PhraseCondition(@JsonProperty("boost") Float boost,
                           @JsonProperty("field") String field,
                           @JsonProperty("value") String value,
                           @JsonProperty("slop") Integer slop) {
        super(boost, field);

        if (value == null) {
            throw new IllegalArgumentException("Field value required");
        }
        if (slop != null && slop < 0) {
            throw new IllegalArgumentException("Slop must be positive");
        }

        this.field = field;
        this.value = value;
        this.slop = slop == null ? DEFAULT_SLOP : slop;
    }

    /**
     * Returns the number of other words permitted between words in phrase.
     *
     * @return The number of other words permitted between words in phrase.
     */
    public int getSlop() {
        return slop;
    }

    /**
     * Returns The phrase terms to be matched.
     *
     * @return the phrase terms to be matched.
     */
    public String getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public Query query(Schema schema) {
        ColumnMapperSingle<?> columnMapper = getMapper(schema, field);
        Class<?> clazz = columnMapper.baseClass();
        if (clazz == String.class) {
            Analyzer analyzer = schema.getAnalyzer();
            QueryBuilder queryBuilder = new QueryBuilder(analyzer);
            Query query = queryBuilder.createPhraseQuery(field, value, slop);
            if (query == null) query = new BooleanQuery();
            query.setBoost(boost);
            return query;
        } else {
            String message = String.format("Unsupported query %s for mapper %s", this, columnMapper);
            throw new UnsupportedOperationException(message);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("boost", boost)
                      .add("field", field)
                      .add("value", value)
                      .add("slop", slop)
                      .toString();
    }
}