package org.gaussian.graphql.pulse;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLPulseConsumer implements Handler<Message<Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulseConsumer.class);
    public static final String QUERY_COUNTER_NAME = "graphql-pulse.requests";
    public static final String QUERY_NULL_VALUES_NAME = "graphql-pulse.null_values";
    public static final String QUERY_ERRORS_NAME = "graphql-pulse.errors";

    // TODO: validate everything
    public void handle(Message message) {
        LOG.info("handling message");

        final JsonObject body = new JsonObject(message.body().toString());
        final MeterRegistry registry = BackendRegistries.getDefaultNow();
        final String type = body.getString("type");
        final JsonObject values = body.getJsonObject("values");
        final JsonArray errors = body.getJsonArray("errors");

        if (values != null) {
            values.stream()
                    .forEach(field -> {
                        final Tags tags = Tags.of("type", type)
                                .and("field", field.getKey());
                        registry.counter(QUERY_COUNTER_NAME, tags).increment();
                        if (field.getValue() == null) {
                            registry.counter(QUERY_NULL_VALUES_NAME, tags).increment();
                        }
                    });
        }

        if (errors != null) {
            errors.stream()
                    .map(String.class::cast)
                    .forEach(field -> {
                        final Tags tags = Tags.of("type", type)
                                .and("field", field);
                        registry.counter(QUERY_ERRORS_NAME, tags).increment();
                    });
        }

    }

}
