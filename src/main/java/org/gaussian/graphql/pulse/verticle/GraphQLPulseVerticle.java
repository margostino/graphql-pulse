package org.gaussian.graphql.pulse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.gaussian.graphql.pulse.consumer.PulseConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulseVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseVerticle.class);
    public final static String GRAPHQL_PULSE_ADDRESS = "graphql.queries";

    @Override
    public void start(Promise<Void> promise) {
        vertx.eventBus().consumer(GRAPHQL_PULSE_ADDRESS, new PulseConsumer());
        LOG.info("GraphQLPulse application started");
        LOG.info("GraphQLPulseConsumer consumer registered");
    }
}
