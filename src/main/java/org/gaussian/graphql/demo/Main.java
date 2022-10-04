package org.gaussian.graphql.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.gaussian.graphql.pulse.app.GraphQLPulse;
import org.gaussian.graphql.pulse.configuration.PulseConfig;

public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        final VertxOptions vertxOptions = vertxOptions();
        final PulseConfig pulseConfig =
        GraphQLPulse.start(vertxOptions)
                .onSuccess(pulse -> {
                    final Vertx vertx = pulse.vertx();
                    final JsonObject schemaConfig = new JsonObject().put("file", "schema.graphql");
                    final JsonObject config = new JsonObject().put("graphql", schemaConfig);
                    pulse.vertx().fileSystem()
                            .readFile("schema.graphql")
                            .map(Buffer::toString)
                            .map(schema -> {
                                config.getJsonObject("graphql").put("schema", schema);
                                DeploymentOptions options = new DeploymentOptions().setConfig(config);
                                vertx.deployVerticle(new GraphQLServerVerticle(pulse), options);
                                return config;
                            })
                            .onFailure(error -> {
                                LOG.error("Cannot read schema", error);
                                System.exit(1);
                            });
                })
                .onFailure(error -> LOG.error("Pulse cannot start", error));
    }

    private static VertxOptions vertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions).setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }

}
