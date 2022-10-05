package org.gaussian.graphql.pulse;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.gaussian.graphql.demo.GraphQLServerVerticle;
import org.gaussian.graphql.pulse.app.GraphQLPulse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

import static org.gaussian.graphql.pulse.helpers.QueryHelper.resource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.valid4j.Assertive.neverGetHere;

@ExtendWith(VertxExtension.class)
public class GraphQLPulseTest {

    private HttpClient httpClient;

    @BeforeEach
    public void setup(VertxTestContext testContext) {
        GraphQLPulse.start()
                .onSuccess(pulse -> {
                    final Vertx vertx = pulse.vertx();
                    httpClient = vertx.createHttpClient();
                    final JsonObject schemaConfig = new JsonObject().put("file", "schema.graphql");
                    final JsonObject config = new JsonObject().put("graphql", schemaConfig);
                    pulse.vertx().fileSystem()
                            .readFile("schema.graphql")
                            .map(Buffer::toString)
                            .map(schema -> {
                                config.getJsonObject("graphql").put("schema", schema);
                                DeploymentOptions options = new DeploymentOptions().setConfig(config);
                                vertx.deployVerticle(new GraphQLServerVerticle(pulse), options, testContext.succeeding(id -> testContext.completeNow()));
                                return config;
                            })
                            .onFailure(error -> {
                                neverGetHere(error, "graphql schema failed");
                            });
                })
                .onFailure(error -> {
                    throw new RuntimeException("graphql-pulse initialization failed", error);
                });
    }

    private Future<JsonObject> query(VertxTestContext context, String requestPath) {
        final String query = resource(requestPath, StandardCharsets.UTF_8);
        return httpClient.request(HttpMethod.POST, 8080, "localhost", "/graphql")
                .compose(req -> req.send(query))
                .map(response -> {
                    assertThat(response.statusCode(), equalTo(200));
                    return response;
                })
                .compose(HttpClientResponse::body)
                .map(buffer -> new JsonObject(buffer.toString()));
    }

    @Test
    public void shouldReturnSuccessStatusAndQueryResponse(VertxTestContext context) {
        query(context, "queries/two_types_query.json")
                .onSuccess(response -> assertTrue(response.containsKey("data")));
    }

    private Handler<AsyncResult<JsonObject>> complete(VertxTestContext context) {
        return context.succeeding(response -> context.verify(() -> context.completeNow()));
    }

    @Test
    public void shouldReturnMetrics(VertxTestContext context) throws Throwable {
        query(context, "queries/two_types_query.json")
                .flatMap(ignored -> query(context, "queries/pulse_query.json"))
                .onSuccess(metrics -> {
                    assertThat(metrics.size(), greaterThan(0));
                })
                .onFailure(context::failNow)
                .onComplete(complete(context));

    }
}
