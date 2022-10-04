package org.gaussian.graphql.pulse.app;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import org.gaussian.graphql.pulse.configuration.ClusterConfig;
import org.gaussian.graphql.pulse.configuration.NetworkConfig;
import org.gaussian.graphql.pulse.configuration.PulseConfig;
import org.gaussian.graphql.pulse.metric.PulseRegistry;

public record GraphQLPulse(Vertx vertx, PulseRegistry pulseRegistry) {

    public static Future<GraphQLPulse> start(VertxOptions vertxOptions) {
        final PulseConfig pulseConfig = defaultConfig();
        return start(vertxOptions, pulseConfig);
    }

    public static Future<GraphQLPulse> start(VertxOptions vertxOptions, PulseConfig pulseConfig) {
        return PulseClusterManager
                .create()
                .start(vertxOptions);
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }

    private static PulseConfig defaultConfig() {
        final NetworkConfig networkConfig = NetworkConfig.builder()
                .port(5701)
                .portCount(20)
                .multiCastEnabled(true)
                .portAutoIncrement(true)
                .build();

        final ClusterConfig clusterConfig = ClusterConfig.builder()
                .networkConfig(networkConfig)
                .backupCount(1)
                .name("dev")
                .build();

        return PulseConfig.builder()
                .clusterConfig(clusterConfig)
                .micrometerEnabled(true)
                .build();
    }
}
