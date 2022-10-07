package org.gaussian.graphql.pulse;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.gaussian.graphql.pulse.app.GraphQLPulse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.restassured.RestAssured.given;
import static org.gaussian.graphql.demo.Demo.run;
import static org.gaussian.graphql.pulse.helpers.QueryHelper.resource;
import static org.hamcrest.Matchers.*;

@ExtendWith(VertxExtension.class)
public class PulseTest {

    private static final String HOST = "localhost";
    private static final String QUERY_PATH = "/graphql";
    private static final String PING_PATH = "/ping";
    private static final int PORT = 8080;

    private final RequestSpecification scenario = given();
    private static GraphQLPulse pulse;

    @BeforeAll
    public static void setup(VertxTestContext context) {
        run().onSuccess(ignored -> {
            pulse = ignored;
            context.completeNow();
        }).onFailure(context::failNow);
    }

    @AfterAll
    public static void tearDown() {
        pulse.stop();
    }

    @Test
    public void shouldReturnMetricsForQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/two_types_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/requests_count_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.requests_count[0].type", equalTo("economy"))
                .body("data.pulse.requests_count[0].field", equalTo("government_debt"))
                .body("data.pulse.requests_count[0].value", greaterThan(0))
                .body("data.pulse.requests_count[1].type", equalTo("demographic"))
                .body("data.pulse.requests_count[1].field", equalTo("population"))
                .body("data.pulse.requests_count[1].value", greaterThan(0))
                .body("data.pulse.requests_count[2].type", equalTo("demographic"))
                .body("data.pulse.requests_count[2].field", equalTo("population_between_30_39"))
                .body("data.pulse.requests_count[2].value", greaterThan(0))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(1))
                .body("data.pulse.requests_count", hasSize(3));

        scenario.when()
                .post(QUERY_PATH);

    }

}
