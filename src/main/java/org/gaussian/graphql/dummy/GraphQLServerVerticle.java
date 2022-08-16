package org.gaussian.graphql.dummy;

import graphql.GraphQL;
import graphql.language.ScalarTypeDefinition;
import graphql.language.ScalarTypeExtensionDefinition;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandlerOptions;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static java.lang.String.format;

public class GraphQLServerVerticle extends AbstractVerticle {

    private final static Logger LOG = LoggerFactory.getLogger(GraphQLServerVerticle.class);
    private final static int DEFAULT_PORT = 8080;

    private GraphQLHandler graphQLHandler;

    @Override
    public void start(Promise<Void> promise) {
        this.graphQLHandler = newGraphQLHandler();

        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(this::contentTypeHandler);
        router.post("/graphql").handler(this::query);

        final JsonObject config = config() != null ? config().getJsonObject("graphql") : new JsonObject();

        final int port = config.getInteger("port") != null ? config.getInteger("port") : DEFAULT_PORT;

        httpServer(port).requestHandler(router)
                        .listen(port)
                        .onSuccess(server -> complete(server, promise))
                        .onFailure(server -> fail(server, promise));
    }

    private void complete(HttpServer server, Promise<Void> promise) {
        promise.complete();
        LOG.info(format("GraphQL server started on port %s", server.actualPort()));
    }

    private void fail(Throwable throwable, Promise<Void> promise) {
        LOG.error("GraphQL server failed: {}", throwable);
        promise.fail(throwable.getCause());
    }

    private void query(RoutingContext context) {
        graphQLHandler.handle(context);
    }

    private HttpServer httpServer(int port) {
        final HttpServerOptions options =
                new HttpServerOptions()
                        .setCompressionSupported(true)
                        .setPort(port);
        return vertx.createHttpServer(options);
    }

    private void contentTypeHandler(RoutingContext routingContext) {
        routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_JSON);
        routingContext.next();
    }

    private GraphQLHandler newGraphQLHandler() {
        final String schema = config().getJsonObject("graphql").getString("schema");
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry schemaRegistry = schemaParser.parse(schema);
        final ScalarTypeDefinition scalarTypeDefinition = ScalarTypeExtensionDefinition.newScalarTypeDefinition().name("Long").build();
        schemaRegistry.add(scalarTypeDefinition);

        final DummyDataFetcher fetcher = new DummyDataFetcher();
        final RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                                                         .scalar(ExtendedScalars.GraphQLLong)
                                                         .type("Query", typeWiring -> typeWiring.dataFetcher("demographic", fetcher))
                                                         .type("Query", typeWiring -> typeWiring.dataFetcher("economy", fetcher))
                                                         .type("Query", typeWiring -> typeWiring.dataFetcher("environment", fetcher))
                                                         .build();

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(schemaRegistry, runtimeWiring);
        final GraphQL graphQLApi = GraphQL.newGraphQL(graphQLSchema).build();
        final GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions();
        return GraphQLHandler.create(graphQLApi, graphQLHandlerOptions);
    }

}
