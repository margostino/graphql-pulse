package org.gaussian.graphql.demo;

import com.google.common.io.Resources;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.net.URL;

public class Configuration {

    public static Future<JsonObject> getJsonConfig(Vertx vertx) {
        final ConfigRetrieverOptions options = getConfigurationOptions();
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        return retriever.getConfig();
    }

    private static ConfigRetrieverOptions getConfigurationOptions() {
        final String configFile = getFilePath("config.yml");
        final ConfigStoreOptions store = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", configFile)
                );
        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(store);
        return options;
    }

    private static String getFilePath(String resourceName) throws IllegalArgumentException {
        final URL configUrl = Resources.getResource(resourceName);
        return configUrl.getPath();
    }
}
