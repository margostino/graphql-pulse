package org.gaussian.graphql.pulse.schema;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static io.vertx.core.Future.fromCompletionStage;
import static io.vertx.core.json.JsonObject.mapFrom;
import static java.util.stream.Collectors.toList;
import static org.gaussian.graphql.pulse.app.GraphQLPulse.getGraphQLPulse;
import static org.gaussian.graphql.pulse.verticle.GraphQLPulseVerticle.GRAPHQL_PULSE_ADDRESS;

public abstract class AbstractPulseDataFetcher implements DataFetcher<CompletionStage<DataFetcherResult>> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPulseDataFetcher.class);

    private final Future<EventBus> asyncEventBus;

    public AbstractPulseDataFetcher() {
        this.asyncEventBus = getGraphQLPulse().eventBus();
    }

    public abstract CompletionStage<DataFetcherResult> getAndPulse(DataFetchingEnvironment environment);

    @Override
    public CompletionStage<DataFetcherResult> get(DataFetchingEnvironment environment) {
        Future<DataFetcherResult> asyncResult;
        final String type = environment.getField().getName();
        asyncResult = fromCompletionStage(getAndPulse(environment));
        asyncResult.onSuccess(result -> trackData(type, result))
                .onFailure(error -> trackError(type, error));
        return asyncResult.toCompletionStage();

    }

    protected List<String> getQueryFields(DataFetchingEnvironment environment) {
        return environment.getFields().stream()
                .flatMap(field -> field.getSelectionSet().getSelections().stream())
                .map(selection -> ((Field) selection).getName())
                .distinct()
                .collect(toList());
    }

    private void trackData(String type, Object result) {
        if (result instanceof DataFetcherResult) {
            DataFetcherResult dataFetcherResult = (DataFetcherResult) result;
            final JsonObject query = new JsonObject().put("type", type);
            if (dataFetcherResult.getData() instanceof Map) {
                Map<String, Object> data = (Map) dataFetcherResult.getData();
                final JsonObject fields = mapFrom(data);
                query.put("values", fields);
            }

            if (dataFetcherResult.hasErrors()) {
                List<GraphQLError> graphQLErrors = dataFetcherResult.getErrors();
                List<String> errors = graphQLErrors.stream()
                        // TODO: define bounded path
                        .map(error -> (error.getPath().size() > 1) ? (String) error.getPath().get(1) : null)
                        .collect(toList());
                query.put("errors", errors);
            }

            final String message = query.encode();

            asyncEventBus.onSuccess(eventBus -> eventBus.send(GRAPHQL_PULSE_ADDRESS, message))
                    .onSuccess(error -> LOG.error("Event bus cannot be used", error));
        }
    }

    private void trackError(String type, Throwable throwable) {
        LOG.error("Failure when fetching query for type {}", type, throwable);
    }

}
