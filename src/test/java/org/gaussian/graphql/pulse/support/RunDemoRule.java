package org.gaussian.graphql.pulse.support;

import io.restassured.RestAssured;
import org.gaussian.graphql.demo.Demo;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static java.util.stream.IntStream.rangeClosed;
import static org.hamcrest.CoreMatchers.equalTo;

class RunDemoRule extends ExternalResource {

    private static final Logger logger = LoggerFactory.getLogger(RunDemoRule.class);
    private static final int DEFAULT_DEMO_PORT = 8080;

    private final Class<?> main;

    private RunDemoRule(Class main) {
        this.main = main;
    }

    static <T> RunDemoRule of(Class<T> main) {
        return new RunDemoRule(main);
    }

    public int httpServerPort() {
        return DEFAULT_DEMO_PORT;
    }

    @Override
    protected void before() {
        final long start = System.currentTimeMillis();
        Demo.main(new String[]{});
        rangeClosed(1, 5)
                .filter(i -> ping())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Vert.x application not available after 5 pings"));
        logger.debug("Vert.x application startup completed in {} ms.", System.currentTimeMillis() - start);
    }

    private boolean ping() {
        try {
            RestAssured
                    .given().port(httpServerPort()).baseUri("http://localhost")
                    .expect().statusCode(OK.code()).and().body(equalTo("pong"))
                    .when().get("/ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
