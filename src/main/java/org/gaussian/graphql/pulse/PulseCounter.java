package org.gaussian.graphql.pulse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PulseCounter {

    private String type;
    private String field;
    private Double count;

    private PulseCounter(String type, String field, Double count) {
        this.type = type;
        this.field = field;
        this.count = count;
    }
}
