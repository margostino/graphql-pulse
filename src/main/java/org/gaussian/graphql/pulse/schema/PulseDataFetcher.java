package org.gaussian.graphql.pulse.schema;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.AsyncMap;
import org.gaussian.graphql.pulse.metric.PulseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletionStage;

import static io.vertx.core.Promise.promise;
import static java.util.stream.Collectors.*;

public class PulseDataFetcher extends AbstractPulseDataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(PulseDataFetcher.class);
    private final Random random;
    private final PulseRegistry pulseRegistry;

    public PulseDataFetcher(EventBus eventBus, PulseRegistry pulseRegistry) {
        super(eventBus);
        this.pulseRegistry = pulseRegistry;
        this.random = new Random();
    }

    @Override
    public CompletionStage<DataFetcherResult> getAndPulse(DataFetchingEnvironment environment) {
        List<String> fields = getQueryFields(environment);
        return getPulse(fields).toCompletionStage();
    }

    private List<Future> getCountersFor(Set<String> metrics) {
        return metrics.stream()
                .map(metricName -> {
                    String[] metricParts = metricName.split("\\.");
                    return pulseRegistry.getCounterValue(metricName)
                            .map(value -> PulseCounter.builder()
                                    .type(metricParts[1])
                                    .field(metricParts[2])
                                    .metricType(metricParts[0])
                                    .value(Double.valueOf(value))
                                    .build());
                })
                .collect(toList());
    }

    private Future<DataFetcherResult> getPulse(List<String> fields) {
        Promise<DataFetcherResult> promise = promise();

        pulseRegistry.getAsyncMap()
                .flatMap(AsyncMap::keys)
                .map(keys -> filterMetricsBy(keys, fields))
                .map(this::getCountersFor)
                .onSuccess(counters -> mergeAsyncCounters(counters, promise))
                .onFailure(error -> LOG.error("Cannot fetch async map", error));

        return promise.future();
    }

    private Set<String> filterMetricsBy(Set<String> metrics, List<String> fields) {
        return metrics.stream()
                .filter(metric -> fields.contains(metric.split("\\.")[0]))
                .collect(toSet());
    }

    private void mergeAsyncCounters(List<Future> counters, Promise<DataFetcherResult> promise) {
        CompositeFuture.all(counters)
                .onSuccess(composite -> {
                    final Map<String, List<Object>> data = composite.result()
                            .list()
                            .stream()
                            .map(PulseCounter.class::cast)
                            .map(counter -> {
                                final Map<String, Object> partialData = new HashMap();
                                partialData.put(counter.getMetricType(), counter);
                                return partialData;
                            })
                            .flatMap(partialData -> partialData.entrySet().stream())
                            .collect(groupingBy(entry -> entry.getKey(), mapping(entry -> entry.getValue(), toList())));
                    promise.complete(DataFetcherResult.newResult().data(data).build());
                })
                .onFailure(error -> LOG.error("Cannot composite async counters", error));
    }

}
