package org.gaussian.graphql.pulse.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.gaussian.graphql.pulse.consumer.PulseConsumer;
import org.gaussian.graphql.pulse.metric.PulseRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulseVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseVerticle.class);
    public static final String GRAPHQL_PULSE_ADDRESS = "graphql.pulse";
    private final PulseRegistry pulseRegistry;

    public GraphQLPulseVerticle(PulseRegistry pulseRegistry) {
        this.pulseRegistry = pulseRegistry;
    }

    @Override
    public void start(Promise<Void> promise) {
        vertx.eventBus().consumer(GRAPHQL_PULSE_ADDRESS, new PulseConsumer(pulseRegistry));
        LOG.info("GraphQL Pulse consumer consumer registered");
        LOG.info("GraphQL Pulse application started");
    }
}
