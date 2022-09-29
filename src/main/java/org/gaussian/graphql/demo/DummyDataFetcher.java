package org.gaussian.graphql.demo;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import org.gaussian.graphql.pulse.schema.AbstractPulseDataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import static java.lang.Math.random;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class DummyDataFetcher extends AbstractPulseDataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(DummyDataFetcher.class);
    private final Random random;

    public DummyDataFetcher() {
        this.random = new Random();
    }

    @Override
    public CompletionStage<DataFetcherResult> getAndPulse(DataFetchingEnvironment environment) {
        final String type = environment.getField().getName();
        final List<String> fields = getQueryFields(environment);
        return completedFuture(fetchMockData(type, fields));
    }

    private DataFetcherResult fetchMockData(String type, List<String> fields) {
        Map<String, Object> data = new HashMap<>();
        DataFetcherResult.Builder builder = DataFetcherResult.newResult();

        if (random() > 0.1) {
            if (random() > 0.1) {
                fields.stream().forEach(field -> data.put(field, random.nextInt(Integer.MAX_VALUE)));
            } else {
                fields.stream().forEach(field -> data.put(field, null));
            }
            builder.data(data);
        } else {
            fields.stream().forEach(field -> data.put(field, null));
            builder.error(new DummyGraphQLError("some dummy error", List.of(type, fields.stream().findAny().get())));
        }

        return builder.build();
    }

}
