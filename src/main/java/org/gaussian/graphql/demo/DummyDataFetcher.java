package org.gaussian.graphql.demo;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.vertx.core.eventbus.EventBus;
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

@SuppressWarnings("unchecked")
public class DummyDataFetcher extends AbstractPulseDataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(DummyDataFetcher.class);

    private final Random random;
    private final boolean probabilisticDemo;

    public DummyDataFetcher(EventBus eventBus, boolean probabilisticDemo) {
        super(eventBus);
        this.random = new Random();
        this.probabilisticDemo = probabilisticDemo;
    }

    @Override
    public CompletionStage<DataFetcherResult> getAndPulse(DataFetchingEnvironment environment) {
        final String type = environment.getField().getName();
        final List<String> fields = getQueryFields(environment);
        return completedFuture(fetchFakeData(type, fields));
    }

    private DataFetcherResult fetchFakeData(String type, List<String> fields) {
        DataFetcherResult.Builder builder = DataFetcherResult.newResult();

        if (probabilisticDemo) {
            generateProbabilisticResponse(type, fields, builder);
        } else {
            generateMockedResponse(type, fields, builder);
        }

        return builder.build();
    }

    private void generateMockedResponse(String type, List<String> fields, DataFetcherResult.Builder builder) {
        switch (type) {
            case "faulty":
                generateErrorFakeResult(type, fields, builder);
                break;
            case "some_faulty":
                generateProbabilityErrorFakeResult(type, fields, builder);
                break;
            case "none":
                generateNoneFakeResult(fields, builder);
                break;
            case "some_none":
                generateProbabilityNoneFakeResult(fields, builder);
                break;
            default:
                generateFakeResult(fields, builder);
        }
    }

    private void generateProbabilisticResponse(String type, List<String> fields, DataFetcherResult.Builder builder) {
        if (random() > 0.1) {
            if (random() > 0.1) {
                generateFakeResult(fields, builder);
            } else {
                generateNoneFakeResult(fields, builder);
            }
        } else {
            generateProbabilityErrorFakeResult(type, fields, builder);
        }
    }

    private void generateFakeResult(List<String> fields, DataFetcherResult.Builder builder) {
        final Map<String, Object> data = new HashMap<>();
        fields.stream().forEach(field -> data.put(field, random.nextInt(Integer.MAX_VALUE)));
        builder.data(data);
    }

    private void generateNoneFakeResult(List<String> fields, DataFetcherResult.Builder builder) {
        final Map<String, Object> data = new HashMap<>();
        fields.stream().forEach(field -> data.put(field, null));
        builder.data(data);
    }

    private void generateProbabilityNoneFakeResult(List<String> fields, DataFetcherResult.Builder builder) {
        final Map<String, Object> data = new HashMap<>();
        fields.stream()
                .forEach(field -> {
                    if (random() > 0.5) {
                        data.put(field, null);
                    } else {
                        data.put(field, random.nextInt(Integer.MAX_VALUE));
                    }
                });
        builder.data(data);
    }

    private void generateProbabilityErrorFakeResult(String type, List<String> fields, DataFetcherResult.Builder builder) {
        final Map<String, Object> data = new HashMap<>();
        fields.stream()
                .forEach(field -> {
                    if (random() > 0.5) {
                        data.put(field, null);
                        builder.error(new DummyGraphQLError("some dummy error", List.of(type, field)));
                    } else {
                        data.put(field, random.nextInt(Integer.MAX_VALUE));
                    }
                });
        builder.data(data);
    }

    private void generateErrorFakeResult(String type, List<String> fields, DataFetcherResult.Builder builder) {
        final Map<String, Object> data = new HashMap<>();
        fields.stream()
                .forEach(field -> {
                    data.put(field, null);
                    builder.error(new DummyGraphQLError("some dummy error", List.of(type, field)));
                });
        builder.data(data);
    }

}
