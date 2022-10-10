package org.gaussian.graphql.pulse.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.backends.BackendRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class PulseRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(PulseRegistry.class);
    private static final String PULSE_METRIC_PREFIX = "graphql-pulse";
    private static final MeterRegistry METER_REGISTRY = BackendRegistries.getDefaultNow();

    private final ClusterManager clusterManager;
    private final boolean micrometerEnabled;

    public PulseRegistry(ClusterManager clusterManager, boolean micrometerEnabled) {
        this.clusterManager = clusterManager;
        this.micrometerEnabled = micrometerEnabled;
    }

    public void incrementCounter(MetricType metricType, String type, String field, Tags tags) {
        final String meterMetricName = format("%s.%s", PULSE_METRIC_PREFIX, metricType.toLowerCase());
        final String pulseMetricName = format("%s.%s.%s", metricType.toLowerCase(), type, field);

        if (micrometerEnabled) {
            METER_REGISTRY.counter(meterMetricName, tags).increment();
        }

        getAsyncMap().flatMap(asyncMap -> asyncMap.putIfAbsent(pulseMetricName, pulseMetricName))
                .flatMap(ignored -> getCounter(pulseMetricName))
                .onSuccess(Counter::incrementAndGet)
                .onFailure(error -> LOG.error(format("Cannot increment counter %s", pulseMetricName), error));
    }

    public Future<Counter> getCounter(String metricName) {
        final Promise<Counter> promise = Promise.promise();
        clusterManager.getCounter(metricName, promise);
        return promise.future();
    }

    public Future<AsyncMap<String, String>> getAsyncMap() {
        final Promise<AsyncMap<String, String>> promise = Promise.promise();
        clusterManager.getAsyncMap("pulse", promise);
        return promise.future();
    }

    public Future<Long> getCounterValue(String metricName) {
        return getCounter(metricName).flatMap(Counter::get);
    }

}
