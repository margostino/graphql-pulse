package org.gaussian.graphql.demo;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
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
        run().onFailure(error -> fail("Demo cannot start", error));
    }

    public static Future<GraphQLPulse> run() {
        final VertxOptions vertxOptions = vertxOptions();
        return GraphQLPulse.start(vertxOptions)
                .compose(Demo::start);
    }

    private static Future<GraphQLPulse> start(GraphQLPulse pulse) {
        final Vertx vertx = pulse.vertx();
        return getJsonConfig(vertx)
                .compose(config -> readSchema(vertx, config)
                        .compose(schema -> deploy(pulse, schema, config)))
                .map(ignored -> pulse);
    }

    private static Future<String> deploy(GraphQLPulse pulse, String schema, JsonObject config) {
        config.getJsonObject("graphql").put("schema", schema);
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        return pulse.vertx().deployVerticle(new GraphQLServerVerticle(pulse), options);
    }

    private static void fail(String reason, Throwable throwable) {
        LOG.error(reason, throwable);
        System.exit(1);
    }
}
