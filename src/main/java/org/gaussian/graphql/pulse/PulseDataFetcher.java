package org.gaussian.graphql.pulse;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;

import static io.vertx.core.Future.fromCompletionStage;

public abstract class PulseDataFetcher implements DataFetcher<CompletionStage<DataFetcherResult>> {

    private static final Logger LOG = LoggerFactory.getLogger(PulseDataFetcher.class);

    private final EventBus eventBus;

    public PulseDataFetcher() {
        this.eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
        QueryListener listener = new QueryListener();
        eventBus.register(listener);
    }

    public abstract CompletionStage<DataFetcherResult> getAndTrack(DataFetchingEnvironment environment);

    @Override
    public CompletionStage<DataFetcherResult> get(DataFetchingEnvironment environment) {
        LOG.info("fetching fields");
        Future<DataFetcherResult> asyncResult = fromCompletionStage(getAndTrack(environment));
        asyncResult.onSuccess(this::trackData)
                   .onFailure(this::trackError);
        return asyncResult.toCompletionStage();
    }

    private void trackData(Object result) {
        if (result instanceof DataFetcherResult) {
            DataFetcherResult dataFetcherResult = (DataFetcherResult) result;
            if (dataFetcherResult.getData() instanceof Map) {
                Map<String, Object> data = (Map) dataFetcherResult.getData();
                final String message = JsonObject.mapFrom(data).encode();
                eventBus.post(message);
            }
        }
    }

    private void trackError(Object result) {
        LOG.info("tracking error...");
    }

}
