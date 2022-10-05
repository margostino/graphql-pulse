package org.gaussian.graphql.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.gaussian.graphql.pulse.app.GraphQLPulse;

import static org.gaussian.graphql.demo.Configuration.*;

public class Demo {

    private final static Logger LOG = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        final VertxOptions vertxOptions = vertxOptions();
        GraphQLPulse.start(vertxOptions)
                .onSuccess(Demo::startDemo)
                .onFailure(error -> LOG.error("Pulse cannot start", error));
    }

    private static void fail(String reason, Throwable throwable) {
        LOG.error(reason, throwable);
        System.exit(1);
    }

    private static void startDemo(GraphQLPulse pulse) {
        final Vertx vertx = pulse.vertx();
        getJsonConfig(vertx)
                .onSuccess(config -> readSchema(vertx, config)
                        .onSuccess(schema -> deploy(pulse, schema, config))
                        .onFailure(error -> fail("Cannot read schema", error)))
                .onFailure(error -> fail("Demo cannot start", error));
    }

    private static void deploy(GraphQLPulse pulse, String schema, JsonObject config) {
        config.getJsonObject("graphql").put("schema", schema);
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        pulse.vertx().deployVerticle(new GraphQLServerVerticle(pulse), options);
    }
}
