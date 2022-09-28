package org.gaussian.graphql.demo;

import com.google.common.collect.ImmutableMap;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

public class DummyGraphQLError implements GraphQLError {

    private final String message;
    private final List<Object> path;

    public DummyGraphQLError(String message, List<Object> path) {
        this.path = path;
        this.message = message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return ImmutableMap.of("detailedErrorType", this.getClass().getSimpleName());
    }

}

