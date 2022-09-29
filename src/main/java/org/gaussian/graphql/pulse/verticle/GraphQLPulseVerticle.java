package org.gaussian.graphql.pulse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.Counter;
import org.gaussian.graphql.pulse.consumer.PulseConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulseVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseVerticle.class);
    public final static String GRAPHQL_PULSE_ADDRESS = "graphql.queries";
    private final Promise<Counter> sharedCounter;

    public GraphQLPulseVerticle(Promise<Counter> sharedCounter) {
        this.sharedCounter = sharedCounter;
    }

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("sarlanga 3");
        vertx.eventBus().consumer(GRAPHQL_PULSE_ADDRESS, new PulseConsumer(sharedCounter));
        LOG.info("sarlanga 4");
        LOG.info("GraphQLPulse application started");
        LOG.info("GraphQLPulseConsumer consumer registered");
    }
}
