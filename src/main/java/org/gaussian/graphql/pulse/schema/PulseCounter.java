package org.gaussian.graphql.pulse.schema;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PulseCounter {

    private String metricType;
    private String type;
    private String field;
    private Double value;

    private PulseCounter(String metricType, String type, String field, Double value) {
        this.metricType = metricType;
        this.type = type;
        this.field = field;
        this.value = value;
    }
}
