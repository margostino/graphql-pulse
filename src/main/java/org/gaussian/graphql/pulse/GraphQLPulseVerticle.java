package org.gaussian.graphql.pulse;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulseVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseVerticle.class);
    public final static String GRAPHQL_PULSE_ADDRESS = "graphql.queries";

    @Override
    public void start(Promise<Void> promise) {
        vertx.eventBus().consumer(GRAPHQL_PULSE_ADDRESS, new GraphQLPulseConsumer());
        LOG.info("GraphQLPulse application started");
        LOG.info("GraphQLPulseConsumer consumer registered");
    }
}
