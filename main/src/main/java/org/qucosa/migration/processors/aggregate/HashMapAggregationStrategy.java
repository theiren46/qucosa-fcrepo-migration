/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.migration.processors.aggregate;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.HashMap;
import java.util.Map;

public class HashMapAggregationStrategy implements AggregationStrategy {

    private final ValueBuilder keyExpression;

    private HashMapAggregationStrategy(ValueBuilder keyExpression) {
        this.keyExpression = keyExpression;
    }

    public static HashMapAggregationStrategy aggregateHashBy(ValueBuilder keyExpression) {
        return new HashMapAggregationStrategy(keyExpression);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        final Message newIn = newExchange.getIn();
        final String header = keyExpression.evaluate(newExchange, String.class);
        if (oldExchange == null) {
            newIn.setBody(new HashMap() {{
                put(header, newIn.getBody());
            }});
            return newExchange;
        } else {
            final Message oldIn = oldExchange.getIn();
            ((Map) oldIn.getBody()).put(
                    header,
                    newIn.getBody());
            return oldExchange;
        }
    }
}
