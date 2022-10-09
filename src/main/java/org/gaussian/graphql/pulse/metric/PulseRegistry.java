package org.gaussian.graphql.pulse.metric;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.spi.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class PulseRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(PulseRegistry.class);

    private final ClusterManager clusterManager;

    public PulseRegistry(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void incrementCounter(String metricName) {
        getAsyncMap().flatMap(asyncMap -> asyncMap.putIfAbsent(metricName, metricName))
                .flatMap(ignored -> getCounter(metricName))
                .onSuccess(Counter::incrementAndGet)
                .onFailure(error -> LOG.error(format("Cannot increment counter %s", metricName), error));
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
