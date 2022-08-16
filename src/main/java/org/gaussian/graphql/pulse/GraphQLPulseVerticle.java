package org.gaussian.graphql.pulse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class GraphQLPulseVerticle extends AbstractVerticle {

    public final static String GRAPHQL_PULSE_ADDRESS = "graphql.queries";

    @Override
    public void start(Promise<Void> promise) {
        vertx.eventBus().consumer(GRAPHQL_PULSE_ADDRESS, new GraphQLPulseConsumer());
    }
}
