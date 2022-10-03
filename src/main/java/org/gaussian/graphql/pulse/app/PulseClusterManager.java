package org.gaussian.graphql.pulse.app;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.gaussian.graphql.pulse.metric.PulseRegistry;
import org.gaussian.graphql.pulse.verticle.GraphQLPulseVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static io.vertx.core.Promise.promise;
import static java.time.Instant.now;

public record PulseClusterManager(ClusterManager clusterManager) {

    private static final Logger LOG = LoggerFactory.getLogger(PulseClusterManager.class);

    public static PulseClusterManager create() {
        // TODO: config from file
        Config config = new Config();
        config.setClusterName("dev");
        config.getCPSubsystemConfig().setCPMemberCount(0);
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortCount(20);
        network.setPortAutoIncrement(true);
        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(true);
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        final ClusterManager clusterManager = new HazelcastClusterManager(hazelcastInstance);

        return new PulseClusterManager(clusterManager);
    }

    public Future<GraphQLPulse> start(VertxOptions vertxOptions) {
        final Instant start = now();
        final Promise<GraphQLPulse> app = promise();

        vertxOptions.setClusterManager(clusterManager);

        Vertx.clusteredVertx(vertxOptions, async -> {
            if (async.succeeded()) {
                final Vertx vertx = async.result();
                final PulseRegistry pulseRegistry = new PulseRegistry(clusterManager);
                vertx.deployVerticle(new GraphQLPulseVerticle(pulseRegistry));

                Duration duration = Duration.between(start, now());
                LOG.info("GraphQL-Pulse verticle deployed in {} seconds", duration.getSeconds());
                app.complete(new GraphQLPulse(vertx, pulseRegistry));
            } else {
                LOG.error("GraphQL-Pulse verticle deployment failed: " + async.cause().getMessage());
                app.fail(async.cause());
            }
        });
        return app.future();
    }

}
