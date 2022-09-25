package org.gaussian.graphql.dummy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

public class Main {

    public static void main(String[] args) {
        final Vertx vertx = vertx();
        final JsonObject config = new JsonObject()
                .put("graphql", new JsonObject().put("file", "schema.graphql"));
        vertx.fileSystem()
             .readFile("schema.graphql")
             .map(Buffer::toString)
             .map(schema -> {
                 config.getJsonObject("graphql").put("schema", schema);
                 DeploymentOptions options = new DeploymentOptions().setConfig(config);
                 vertx.deployVerticle(new GraphQLServerVerticle(), options);
                 return config;
             });
    }

    private static Vertx vertx() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                                                                               .setStartEmbeddedServer(true)
                                                                               .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                                                                               .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions)
                                                                                .setEnabled(true);
        final VertxOptions options = new VertxOptions().setMetricsOptions(metricsOptions);
        return Vertx.vertx(options);
    }

}
