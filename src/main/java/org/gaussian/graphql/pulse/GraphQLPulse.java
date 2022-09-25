package org.gaussian.graphql.pulse;

import graphql.schema.idl.RuntimeWiring;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulse {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseConsumer.class);

    private static GraphQLPulse graphQLPulseApp = null;
    private final Vertx vertx;

    private GraphQLPulse() {
        this.vertx = Vertx.currentContext() != null ? Vertx.currentContext().owner() : Vertx.vertx(vertxOptions());
        vertx.deployVerticle(new GraphQLPulseVerticle());
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }

    public static GraphQLPulse getGraphQLPulse() {
        if (graphQLPulseApp == null) {
            graphQLPulseApp = new GraphQLPulse();
        }
        return graphQLPulseApp;
    }

    private static VertxOptions vertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(new HttpServerOptions().setPort(3000))
                .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions)
                .setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }

    public static RuntimeWiring.Builder newRuntimeWiringBuilder() {
        final PulseDataFetcher pulseDataFetcher = new PulseDataFetcher();
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring.dataFetcher("pulse", pulseDataFetcher));
    }

}
