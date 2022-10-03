package org.gaussian.graphql.pulse.metric;

public enum MetricType {

    REQUESTS_COUNT,
    NONE_VALUES_COUNT,
    ERRORS_COUNT;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
