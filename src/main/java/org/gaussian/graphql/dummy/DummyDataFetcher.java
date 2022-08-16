package org.gaussian.graphql.dummy;

import graphql.execution.DataFetcherResult;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.gaussian.graphql.pulse.PulseDataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.toList;

public class DummyDataFetcher extends PulseDataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(DummyDataFetcher.class);
    private final Random random;

    public DummyDataFetcher() {
        this.random = new Random();
    }

    @Override
    public CompletionStage<DataFetcherResult> getAndTrack(DataFetchingEnvironment environment) {
        LOG.info("fetching and tracking query");
        List<String> fields = getQueryFields(environment);
        return completedFuture(fetchMockData(fields));
    }

    private DataFetcherResult fetchMockData(List<String> fields) {
        Map<String, Object> data = new HashMap<>();
        fields.stream().forEach(field -> data.put(field, random.nextInt(Integer.MAX_VALUE)));
        return DataFetcherResult.newResult()
                                .data(data)
                                .build();
    }

    private List<String> getQueryFields(DataFetchingEnvironment environment) {
        return environment.getFields().stream()
                          .flatMap(field -> field.getSelectionSet().getSelections().stream())
                          .map(selection -> ((Field) selection).getName())
                          .distinct()
                          .collect(toList());
    }

}
