package org.gaussian.graphql.pulse.consumer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import org.gaussian.graphql.pulse.metric.MetricType;
import org.gaussian.graphql.pulse.metric.PulseRegistry;

import static java.lang.String.format;
import static org.gaussian.graphql.pulse.metric.MetricType.*;

public record PulseConsumer(PulseRegistry pulseRegistry) implements Handler<Message<Object>> {

    // TODO: validate everything
    public void handle(Message message) {
        final JsonObject body = new JsonObject(message.body().toString());
        final String type = body.getString("type");
        final JsonObject values = body.getJsonObject("values");
        final JsonArray errors = body.getJsonArray("errors");

        if (values != null) {
            values.stream()
                    .forEach(field -> {
                        final Tags tags = Tags.of("type", type).and("field", field.getKey());

                        mark(REQUESTS_COUNT, type, field.getKey(), tags);

                        if (field.getValue() == null && (errors == null || (errors != null && !errors.contains(field.getKey())))) {
                            mark(NONE_VALUES_COUNT, type, field.getKey(), tags);
                        }

                    });
        }

        if (errors != null) {
            errors.stream()
                    .map(String.class::cast)
                    .forEach(field -> {
                        final Tags tags = Tags.of("type", type).and("field", field);
                        mark(ERRORS_COUNT, type, field, tags);
                    });
        }
    }

    private void mark(MetricType metricType, String type, String field, Tags tags) {
        pulseRegistry.incrementCounter(metricType, type, field, tags);
    }
}
