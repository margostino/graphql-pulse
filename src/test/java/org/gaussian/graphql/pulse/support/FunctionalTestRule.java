package org.gaussian.graphql.pulse.support;

import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.builder.verify.VerifyHttp;
import com.xebialabs.restito.server.StubServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static io.restassured.RestAssured.given;

public class FunctionalTestRule implements TestRule, FunctionalTest.Delegate {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private final RestitoRule restito;
    private final RunDemoRule application;

    private FunctionalTestRule(RunDemoRule application, RestitoRule restito) {
        this.restito = restito;
        this.application = application;
    }

    public static <T> FunctionalTestRule of(Class<T> main) {
        return of(main, StubServer.DEFAULT_PORT);
    }

    public static <T> FunctionalTestRule of(Class<T> main, int restitoPort) {
        final RestitoRule restito = new RestitoRule(restitoPort);
        final RunDemoRule application = RunDemoRule.of(main);
        return new FunctionalTestRule(application, restito);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return RuleChain.outerRule(restito).around(application).apply(base, description);
    }

    @Override
    public RequestSpecification http() {
        return given()
                .port(application.httpServerPort())
                .baseUri("http://localhost")
                .accept(ContentType.JSON);
    }

    @Override
    public StubHttp mock() {
        return StubHttp.whenHttp(restito.server());
    }

    @Override
    public VerifyHttp verify() {
        return VerifyHttp.verifyHttp(restito.server());
    }

    @Override
    public void clear() {
        restito.server().clear();
    }
}
