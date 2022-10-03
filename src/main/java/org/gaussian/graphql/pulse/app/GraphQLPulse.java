package org.gaussian.graphql.pulse.app;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import org.gaussian.graphql.pulse.metric.PulseRegistry;

public record GraphQLPulse(Vertx vertx, PulseRegistry pulseRegistry) {

    public static Future<GraphQLPulse> start(VertxOptions vertxOptions) {
        final PulseClusterManager clusterManager = PulseClusterManager.create();
        return clusterManager.start(vertxOptions);
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }
}
