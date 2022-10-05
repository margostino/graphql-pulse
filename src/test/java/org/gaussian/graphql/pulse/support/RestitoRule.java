package org.gaussian.graphql.pulse.support;

import com.xebialabs.restito.server.StubServer;
import org.junit.rules.ExternalResource;

public class RestitoRule extends ExternalResource {

	private final int port;

	private StubServer server;

	public RestitoRule(final int port) {
		this.port = port;
	}

	public StubServer server() {
		return this.server;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		this.server = new StubServer(port);
		this.server.start();
	}

	@Override
	protected void after() {
		super.after();
		this.server.stop();
		this.server = null;
	}

}
