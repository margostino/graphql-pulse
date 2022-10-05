package org.gaussian.graphql.demo;

import com.google.common.io.Resources;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

import java.net.URL;

public class Configuration {

    protected static Future<JsonObject> getJsonConfig(Vertx vertx) {
        final ConfigRetrieverOptions options = getConfigurationOptions();
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        return retriever.getConfig();
    }

    protected static ConfigRetrieverOptions getConfigurationOptions() {
        final URL configUrl = Resources.getResource("config.yml");
        final String configFile = configUrl.getPath();
        final ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", configFile)
                );
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
        return options;
    }

    protected static VertxOptions vertxOptions() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions().setEnabled(true)
                .setStartEmbeddedServer(true)
                .setEmbeddedServerOptions(new HttpServerOptions().setPort(8081))
                .setEmbeddedServerEndpoint("/metrics");
        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions().setPrometheusOptions(prometheusOptions).setEnabled(true);
        return new VertxOptions().setMetricsOptions(metricsOptions);
    }

    public static Future<String> readSchema(Vertx vertx, JsonObject config) {
        return vertx.fileSystem()
                .readFile(config.getJsonObject("graphql").getString("file"))
                .map(Buffer::toString);
    }

}
