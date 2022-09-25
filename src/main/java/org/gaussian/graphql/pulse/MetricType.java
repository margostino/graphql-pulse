package org.gaussian.graphql.pulse;

import lombok.Getter;

import static org.gaussian.graphql.pulse.GraphQLPulseConsumer.*;

@Getter
public enum MetricType {

    REQUESTS("requests", QUERY_COUNTER_NAME),
    NULL_VALUES("null_values", QUERY_NULL_VALUES_NAME),
    ERRORS("errors", QUERY_ERRORS_NAME);

    private final String schemaType;
    private final String metricName;

    MetricType(String schemaType, String metricName) {
        this.schemaType = schemaType;
        this.metricName = metricName;
    }


    public static String metricOf(String schemaType) {
        try {
            MetricType metricType = MetricType.valueOf(schemaType.toUpperCase());
            return metricType.getMetricName();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
