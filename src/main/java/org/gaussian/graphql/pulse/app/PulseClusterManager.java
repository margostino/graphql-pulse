package org.gaussian.graphql.pulse.app;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.gaussian.graphql.pulse.configuration.PulseConfig;
import org.gaussian.graphql.pulse.metric.PulseRegistry;
import org.gaussian.graphql.pulse.verticle.GraphQLPulseVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static io.vertx.core.Promise.promise;
import static io.vertx.core.Vertx.clusteredVertx;
import static java.lang.Runtime.getRuntime;
import static java.time.Instant.now;

public record PulseClusterManager(ClusterManager clusterManager, PulseConfig pulseConfig) {

    private static final Logger LOG = LoggerFactory.getLogger(PulseClusterManager.class);

    public static PulseClusterManager create(PulseConfig pulseConfig) {
        Config config = pulseConfig.getClusterConfig();
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        final ClusterManager clusterManager = new HazelcastClusterManager(hazelcastInstance);
        return new PulseClusterManager(clusterManager, pulseConfig);
    }

    public Future<GraphQLPulse> start(VertxOptions vertxOptions) {
        final Instant start = now();
        final Promise<GraphQLPulse> app = promise();

        vertxOptions.setClusterManager(clusterManager);

        clusteredVertx(vertxOptions, async -> {
            if (async.succeeded()) {
                final Vertx vertx = async.result();
                final PulseRegistry pulseRegistry = new PulseRegistry(clusterManager, pulseConfig.isMicrometerEnabled());

                onDeploy(vertx, pulseConfig);
                vertx.deployVerticle(new GraphQLPulseVerticle(pulseRegistry));

                Duration duration = Duration.between(start, now());
                LOG.info("GraphQL Pulse verticle deployed in {} milliseconds", duration.toMillis());

                final GraphQLPulse pulse = new GraphQLPulse(vertx, pulseRegistry);
                app.complete(pulse);
            } else {
                LOG.error("GraphQL Pulse verticle deployment failed: " + async.cause().getMessage());
                app.fail(async.cause());
            }
        });
        return app.future();
    }

    private void onDeploy(Vertx vertx, PulseConfig pulseConfig) {
        HeaderPrinter.printHeader(pulseConfig);
        getRuntime().addShutdownHook(new Thread(() -> stop(vertx)));
    }

    public void stop(Vertx vertx) {
        vertx.close()
                .onSuccess(ignored -> LOG.info("GraphQL Pulse cluster was closed successfully"))
                .onFailure(error -> LOG.error("Shutdown failed", error));
    }
}
