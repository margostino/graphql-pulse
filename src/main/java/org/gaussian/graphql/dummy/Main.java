package org.gaussian.graphql.dummy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.gaussian.graphql.pulse.GraphQLPulseVerticle;

public class Main {

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        final JsonObject config = new JsonObject()
                .put("graphql", new JsonObject().put("file", "schema.graphql"));
        vertx.fileSystem()
             .readFile("schema.graphql")
             .map(Buffer::toString)
             .map(schema -> {
                 config.getJsonObject("graphql").put("schema", schema);
                 DeploymentOptions options = new DeploymentOptions().setConfig(config);
                 vertx.deployVerticle(new GraphQLServerVerticle(), options);
                 vertx.deployVerticle(new GraphQLPulseVerticle());
                 return config;
             });
    }
}
