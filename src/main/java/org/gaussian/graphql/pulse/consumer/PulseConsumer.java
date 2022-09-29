package org.gaussian.graphql.pulse.consumer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Counter;
import io.vertx.micrometer.backends.BackendRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class PulseConsumer implements Handler<Message<Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(PulseConsumer.class);
    public static final String QUERY_COUNTER_NAME = "graphql-pulse.requests";
    public static final String QUERY_NULL_VALUES_NAME = "graphql-pulse.null_values";
    public static final String QUERY_ERRORS_NAME = "graphql-pulse.errors";

    private final Promise<Counter> sharedCounter;

    public PulseConsumer(Promise<Counter> sharedCounter) {
        this.sharedCounter = sharedCounter;
    }

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

                        sharedCounter.future()
                                .flatMap(counter -> counter.incrementAndGet())
                                .onSuccess(value -> LOG.info(format("Counter Value %d", value)))
                                .onFailure(error -> LOG.error("Shared counter failed", error));

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
