package org.gaussian.graphql.pulse.support;

import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.builder.verify.VerifyHttp;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

public abstract class FunctionalTest {

    private Delegate delegate;

    protected FunctionalTest(Delegate delegate) {
        this.delegate = delegate;
    }

    @Before
    public void setUp() throws Exception {
        delegate.clear();
    }

    protected RequestSpecification http() {
        return delegate.http();
    }

    protected StubHttp mock() {
        return delegate.mock();
    }

    protected VerifyHttp verify() {
        return delegate.verify();
    }

    public interface Delegate {

        RequestSpecification http();

        StubHttp mock();

        VerifyHttp verify();

        void clear();

    }

}
