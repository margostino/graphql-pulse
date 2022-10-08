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
import static org.gaussian.graphql.pulse.helpers.Matchers.hasPulseCounter;
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
        run().onSuccess(app -> {
            pulse = app;
            query("queries/two_types_query.json");
            query("queries/two_types_query.json");
            query("queries/two_types_query.json");
            query("queries/faulty_query.json");
            query("queries/some_faulty_query.json");
            query("queries/none_query.json");
            query("queries/some_none_query.json");
            context.completeNow();
        }).onFailure(context::failNow);

    }

    @AfterAll
    public static void tearDown() {
        pulse.stop();
    }

    private static void query(String resource) {
        given().contentType(ContentType.JSON)
                .body(resource(resource))
                .post(QUERY_PATH);
    }

    @Test
    public void shouldReturnPulseMetrics() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/full_pulse_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.pulse", aMapWithSize(3))
                .body("data.pulse.requests_count", hasSize(9))
                .body("data.pulse.errors_count", hasSize(2))
                .body("data.pulse.none_values_count", hasSize(2))
                .body("data.pulse.requests_count", allOf(
                        hasPulseCounter("demographic", "population", 3),
                        hasPulseCounter("demographic", "population_between_30_39", 3),
                        hasPulseCounter("economy", "government_debt", 3),
                        hasPulseCounter("faulty", "average_of_something", 1),
                        hasPulseCounter("some_faulty", "total_hospitals", 1),
                        hasPulseCounter("some_faulty", "average_of_employees", 1),
                        hasPulseCounter("none", "total_companies", 1),
                        hasPulseCounter("some_none", "total_cars", 1),
                        hasPulseCounter("some_none", "total_insurance_companies", 1)))
                .body("data.pulse.errors_count", allOf(
                        hasPulseCounter("faulty", "average_of_something", 1),
                        hasPulseCounter("some_faulty", "average_of_employees", 1)))
                .body("data.pulse.none_values_count", allOf(
                        hasPulseCounter("some_none", "total_cars", 1),
                        hasPulseCounter("none", "total_companies", 1)));

        scenario.when()
                .post(QUERY_PATH);

    }

}
