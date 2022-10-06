package org.gaussian.graphql.pulse.app;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.gaussian.graphql.pulse.configuration.ClusterConfig;
import org.gaussian.graphql.pulse.configuration.NetworkConfig;
import org.gaussian.graphql.pulse.configuration.PulseConfig;
import org.gaussian.graphql.pulse.metric.PulseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record GraphQLPulse(Vertx vertx, PulseRegistry pulseRegistry) {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulse.class);

    public static Future<GraphQLPulse> start() {
        final VertxOptions vertxOptions = defaultVertxOptions();
        final PulseConfig pulseConfig = defaultConfig();
        return start(vertxOptions, pulseConfig);
    }

    public static Future<GraphQLPulse> start(VertxOptions vertxOptions) {
        final PulseConfig pulseConfig = defaultConfig();
        return start(vertxOptions, pulseConfig);
    }

    public static Future<GraphQLPulse> start(VertxOptions vertxOptions, PulseConfig pulseConfig) {
        return PulseClusterManager
                .create(pulseConfig)
                .start(vertxOptions);
    }

    public void stop() {
        vertx.close()
                .onSuccess(ignored -> LOG.info("GraphQL Pulse was closed successfully"))
                .onFailure(error -> LOG.error("Shutdown failed", error));
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
                .bannerEnabled(true)
                .build();
    }

    private static VertxOptions defaultVertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions).setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }
}
