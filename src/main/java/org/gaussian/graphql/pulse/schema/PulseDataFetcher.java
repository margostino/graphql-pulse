package org.gaussian.graphql.pulse.schema;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.vertx.micrometer.backends.BackendRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;
import static org.gaussian.graphql.pulse.domain.MetricType.metricOf;

public class PulseDataFetcher extends AbstractPulseDataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(PulseDataFetcher.class);
    private final Random random;

    public PulseDataFetcher() {
        this.random = new Random();
    }

    @Override
    public CompletionStage<DataFetcherResult> getAndPulse(DataFetchingEnvironment environment) {
        List<String> fields = getQueryFields(environment);
        return completedFuture(getPulse(fields));
    }

    private DataFetcherResult getPulse(List<String> fields) {
        Map<String, Object> data = new HashMap<>();
        fields.stream().forEach(field -> data.put(field, getPulseFor(field)));
        return DataFetcherResult.newResult()
                .data(data)
                .build();
    }

    private List<PulseCounter> getPulseFor(String field) {
        MeterRegistry registry = BackendRegistries.getDefaultNow();
        final String metricName = metricOf(field);
        List<Meter> meters = registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().equalsIgnoreCase(metricName))
                .collect(toList());

        if (meters.size() == 0) {
            return List.of(PulseCounter.builder().build());
        }

        return meters.stream()
                .map(meter -> {
                    PulseCounter.PulseCounterBuilder pulseCounter = PulseCounter.builder();
                    meter.getId().getTags().stream()
                            .forEach(tag -> {
                                if (tag.getKey().equals("type")) {
                                    pulseCounter.type(tag.getValue());
                                } else {
                                    pulseCounter.field(tag.getValue());
                                }
                            });
                    List<Measurement> measurements = (List<Measurement>) meter.measure();

                    if (measurements.size() == 1) {
                        pulseCounter.count(measurements.get(0).getValue());
                    }
                    return pulseCounter.build();
                })
                .collect(toList());
    }
}
