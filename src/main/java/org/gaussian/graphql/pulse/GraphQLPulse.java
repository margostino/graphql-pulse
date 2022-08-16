package org.gaussian.graphql.pulse;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.gaussian.graphql.dummy.GraphQLServerVerticle;

public class GraphQLPulse {

    // TODO
    public void start() {
        Vertx vertx = Vertx.vertx(vertxOptions());
        vertx.deployVerticle(new GraphQLServerVerticle());
    }

    private VertxOptions vertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                                                                               .setStartEmbeddedServer(true)
                                                                               .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                                                                               .setEmbeddedServerEndpoint("/graphql-pulse");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions)
                                                                                .setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }

}
