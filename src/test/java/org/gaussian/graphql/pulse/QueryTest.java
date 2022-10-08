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

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.gaussian.graphql.demo.Demo.run;
import static org.gaussian.graphql.pulse.helpers.QueryHelper.resource;
import static org.hamcrest.Matchers.*;

@ExtendWith(VertxExtension.class)
public class QueryTest {

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
            context.completeNow();
        }).onFailure(context::failNow);
    }

    @AfterAll
    public static void tearDown() {
        pulse.stop();
    }

    @Test
    public void shouldPing() {
        get(PING_PATH).then().assertThat().statusCode(200);
    }

    @Test
    public void shouldReturnSuccessStatusAndQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/two_types_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(2))
                .body("data.demographic", aMapWithSize(2))
                .body("data.economy", aMapWithSize(1))
                .body("data.demographic.population", greaterThan(0))
                .body("data.demographic.population_between_30_39", greaterThan(0))
                .body("data.economy.government_debt", greaterThan(0));

        scenario.when()
                .post(QUERY_PATH);

    }

    @Test
    public void shouldReturnFaultyQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/faulty_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(2))
                .body("data", aMapWithSize(1))
                .body("errors", hasSize(1))
                .body("data.faulty", aMapWithSize(1))
                .body("errors[0].message", equalTo("some dummy error"))
                .body("errors[0].path", hasItems("faulty", "average_of_something"))
                .body("data.faulty.average_of_something", nullValue());

        scenario.when()
                .post(QUERY_PATH);
    }

    @Test
    public void shouldReturnSomeFaultyQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/some_faulty_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(2))
                .body("data", aMapWithSize(1))
                .body("errors", hasSize(1))
                .body("data.some_faulty", aMapWithSize(2))
                .body("errors[0].message", equalTo("some dummy error"))
                .body("errors[0].path", hasItems("some_faulty", "average_of_employees"))
                .body("data.some_faulty.average_of_employees", nullValue())
                .body("data.some_faulty.total_hospitals", greaterThan(0));

        scenario.when()
                .post(QUERY_PATH);
    }

    @Test
    public void shouldReturnNoneQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/none_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.none", aMapWithSize(1))
                .body("data.none.total_companies", nullValue());

        scenario.when()
                .post(QUERY_PATH);
    }

    @Test
    public void shouldReturnSomeNoneQueryResponse() {
        scenario.given()
                .contentType(ContentType.JSON)
                .body(resource("queries/some_none_query.json"));

        scenario.expect()
                .statusCode(200)
                .body("", aMapWithSize(1))
                .body("data", aMapWithSize(1))
                .body("data.some_none", aMapWithSize(2))
                .body("data.some_none.total_insurance_companies", greaterThan(0))
                .body("data.some_none.total_cars", nullValue());

        scenario.when()
                .post(QUERY_PATH);
    }
}
