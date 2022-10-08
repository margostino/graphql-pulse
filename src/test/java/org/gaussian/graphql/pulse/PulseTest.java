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

import java.util.stream.IntStream;

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
    public void shouldReturnRequestsMetricForQueryResponse() {
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
                .body("data.pulse.requests_count[0].value", equalTo(1))
                .body("data.pulse.requests_count[1].type", equalTo("demographic"))
                .body("data.pulse.requests_count[1].field", equalTo("population"))
                .body("data.pulse.requests_count[1].value", equalTo(1))
                .body("data.pulse.requests_count[2].type", equalTo("demographic"))
                .body("data.pulse.requests_count[2].field", equalTo("population_between_30_39"))
                .body("data.pulse.requests_count[2].value", equalTo(1))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(1))
                .body("data.pulse.requests_count", hasSize(3));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnErrorsMetricsForQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/faulty_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/errors_count_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.errors_count[0].type", equalTo("faulty"))
                .body("data.pulse.errors_count[0].field", equalTo("average_of_something"))
                .body("data.pulse.errors_count[0].value", equalTo(1))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(1))
                .body("data.pulse.errors_count", hasSize(1));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnNoneValuesMetricsForQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/none_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/none_values_count_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.none_values_count[0].type", equalTo("none"))
                .body("data.pulse.none_values_count[0].field", equalTo("total_companies"))
                .body("data.pulse.none_values_count[0].value", equalTo(1))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(1))
                .body("data.pulse.none_values_count", hasSize(1));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnRequestsAndErrorsMetricsForQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/some_faulty_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/full_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.errors_count[0].type", equalTo("some_faulty"))
                .body("data.pulse.errors_count[0].field", equalTo("average_of_employees"))
                .body("data.pulse.errors_count[0].value", equalTo(1))
                .body("data.pulse.requests_count[0].type", equalTo("some_faulty"))
                .body("data.pulse.requests_count[0].field", equalTo("total_hospitals"))
                .body("data.pulse.requests_count[0].value", equalTo(1))
                .body("data.pulse.requests_count[1].type", equalTo("some_faulty"))
                .body("data.pulse.requests_count[1].field", equalTo("average_of_employees"))
                .body("data.pulse.requests_count[1].value", equalTo(1))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(3))
                .body("data.pulse.requests_count", hasSize(2))
                .body("data.pulse.none_values_count", nullValue())
                .body("data.pulse.errors_count", hasSize(1));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnMetricsForMultipleIsolatedQueries() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/some_faulty_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/some_none_query.json"))
                .post(QUERY_PATH);

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/full_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.errors_count[0].type", equalTo("some_faulty"))
                .body("data.pulse.errors_count[0].field", equalTo("average_of_employees"))
                .body("data.pulse.errors_count[0].value", equalTo(1))
                .body("data.pulse.none_values_count[0].type", equalTo("some_none"))
                .body("data.pulse.none_values_count[0].field", equalTo("total_cars"))
                .body("data.pulse.none_values_count[0].value", equalTo(1))
                .body("data.pulse.requests_count[0].type", equalTo("some_faulty"))
                .body("data.pulse.requests_count[0].field", equalTo("total_hospitals"))
                .body("data.pulse.requests_count[0].value", equalTo(1))
                .body("data.pulse.requests_count[1].type", equalTo("some_faulty"))
                .body("data.pulse.requests_count[1].field", equalTo("average_of_employees"))
                .body("data.pulse.requests_count[1].value", equalTo(1))
                .body("data.pulse.requests_count[2].type", equalTo("some_none"))
                .body("data.pulse.requests_count[2].field", equalTo("total_cars"))
                .body("data.pulse.requests_count[2].value", equalTo(1))
                .body("data.pulse.requests_count[3].type", equalTo("some_none"))
                .body("data.pulse.requests_count[3].field", equalTo("total_insurance_companies"))
                .body("data.pulse.requests_count[3].value", equalTo(1))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(3))
                .body("data.pulse.requests_count", hasSize(4))
                .body("data.pulse.none_values_count", hasSize(1))
                .body("data.pulse.errors_count", hasSize(1));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnRequestsMetricForMultipleQueries() {
        IntStream.range(0, 5).forEach(i ->
                scenario.given()
                        .contentType(ContentType.JSON)
                        .body(resource("queries/two_types_query.json"))
                        .post(QUERY_PATH)
        );

        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/requests_count_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("data.pulse.requests_count[0].type", equalTo("economy"))
                .body("data.pulse.requests_count[0].field", equalTo("government_debt"))
                .body("data.pulse.requests_count[0].value", equalTo(5))
                .body("data.pulse.requests_count[1].type", equalTo("demographic"))
                .body("data.pulse.requests_count[1].field", equalTo("population"))
                .body("data.pulse.requests_count[1].value", equalTo(5))
                .body("data.pulse.requests_count[2].type", equalTo("demographic"))
                .body("data.pulse.requests_count[2].field", equalTo("population_between_30_39"))
                .body("data.pulse.requests_count[2].value", equalTo(5))
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(1))
                .body("data.pulse.requests_count", hasSize(3));

        scenario.when()
                .post(QUERY_PATH);

    }

}
