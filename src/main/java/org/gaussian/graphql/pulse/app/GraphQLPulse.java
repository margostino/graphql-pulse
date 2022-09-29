package org.gaussian.graphql.pulse.app;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import graphql.schema.idl.RuntimeWiring;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.gaussian.graphql.pulse.schema.PulseDataFetcher;
import org.gaussian.graphql.pulse.verticle.GraphQLPulseVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.getenv;
import static java.time.Instant.now;

public class GraphQLPulse {

    private static final Logger LOG = LoggerFactory.getLogger(GraphQLPulse.class);
    //private static final String HZ_MANCENTER_PORT = "8080";
    //private static final String HZ_MANCENTER_PATH = "hazelcast-mancenter";

    private static GraphQLPulse graphQLPulseApp = null;
    private final Vertx vertx;

    private GraphQLPulse() {
        Instant start = now();
        Promise startup = Promise.promise();
        this.vertx = Vertx.currentContext() != null ? Vertx.currentContext().owner() : Vertx.vertx(vertxOptions());
        ClusterManager clusterManager = createHazelcastClusterManager();
        VertxOptions vertxOptions = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(vertxOptions, async -> {
            if (async.succeeded()) {
                Duration duration = Duration.between(start, now());
                LOG.info("GraphQL-Pulse verticle deployed in {} seconds", duration.getSeconds());
                deployVerticle(async.result());
                startup.complete();
            } else {
                LOG.error("GraphQL-Pulse verticle deployment failed: " + async.cause().getMessage());
                startup.fail(async.cause());
            }
        });
        //vertx.deployVerticle(new GraphQLPulseVerticle());
    }

    private void deployVerticle(Vertx vertx) {
        vertx.deployVerticle(new GraphQLPulseVerticle());
    }

    public EventBus eventBus() {
        return vertx.eventBus();
    }

    public static GraphQLPulse getGraphQLPulse() {
        if (graphQLPulseApp == null) {
            graphQLPulseApp = new GraphQLPulse();
        }
        return graphQLPulseApp;
    }

    private static VertxOptions vertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(new HttpServerOptions().setPort(3000))
                .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions)
                .setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }

    public static RuntimeWiring.Builder newRuntimeWiringBuilder() {
        final PulseDataFetcher pulseDataFetcher = new PulseDataFetcher();
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring.dataFetcher("pulse", pulseDataFetcher));
    }

    private ClusterManager createHazelcastClusterManager() {
        Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName("graphql-pulse");
        NetworkConfig network = hazelcastConfig.getNetworkConfig();
        network.setPort(5701).setPortCount(20);
        network.setPortAutoIncrement(true);
        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(true);
        //join.getKubernetesConfig().setEnabled(true);
        //final String hzMancenterUrl = format("http://{0}:{1}/{2}", getHZHost(), HZ_MANCENTER_PORT, HZ_MANCENTER_PATH);
        //ManagementCenterConfig manCenterCfg = new ManagementCenterConfig().setEnabled(true).setUrl(hzMancenterUrl);
        //hazelcastConfig.setManagementCenterConfig(manCenterCfg);
        return new HazelcastClusterManager(hazelcastConfig);
    }

    private String getHZHost() {
        String host = getenv("HZ_HOST");
        return isNullOrEmpty(host) ? "localhost" : host;
    }
}
