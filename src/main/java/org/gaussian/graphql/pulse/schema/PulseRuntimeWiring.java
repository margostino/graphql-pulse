package org.gaussian.graphql.pulse.schema;

import graphql.schema.idl.RuntimeWiring;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import org.gaussian.graphql.pulse.metric.PulseRegistry;

public class PulseRuntimeWiring {

    public static RuntimeWiring.Builder newRuntimeWiringBuilder(EventBus eventBus, PulseRegistry pulseRegistry) {
        final PulseDataFetcher pulseDataFetcher = new PulseDataFetcher(eventBus, pulseRegistry);
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring.dataFetcher("pulse", pulseDataFetcher));
    }

}
