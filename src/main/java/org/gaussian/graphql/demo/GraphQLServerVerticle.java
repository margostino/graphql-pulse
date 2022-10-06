package org.gaussian.graphql.demo;

import graphql.GraphQL;
import graphql.language.ScalarTypeDefinition;
import graphql.language.ScalarTypeExtensionDefinition;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
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
import org.gaussian.graphql.pulse.app.GraphQLPulse;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static java.lang.String.format;
import static org.gaussian.graphql.pulse.schema.PulseRuntimeWiring.newRuntimeWiringBuilder;

public class GraphQLServerVerticle extends AbstractVerticle {

    private final static Logger LOG = LoggerFactory.getLogger(GraphQLServerVerticle.class);
    private final static int DEFAULT_PORT = 8080;

    private GraphQLHandler graphQLHandler;
    private final GraphQLPulse pulse;

    public GraphQLServerVerticle(GraphQLPulse pulse) {
        this.pulse = pulse;
    }

    @Override
    public void start(Promise<Void> promise) {
        this.graphQLHandler = newGraphQLHandler();

        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(this::contentTypeHandler);
        router.post("/graphql").handler(this::query);
        router.get("/ping").handler(this::pong);

        final JsonObject config = config() != null ? config().getJsonObject("graphql") : new JsonObject();

        final int port = config.getInteger("port") != null ? config.getInteger("port") : DEFAULT_PORT;

        httpServer(port).requestHandler(router)
                .listen(port)
                .onSuccess(server -> complete(server, promise))
                .onFailure(server -> fail(server, promise));
    }

    private void pong(RoutingContext context) {
        LOG.info("ping received");
        context.response()
                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .end("pong");
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

        final boolean probabilisticDemo = config().getBoolean("probabilistic_demo_enabled");
        final DummyDataFetcher fetcher = new DummyDataFetcher(pulse.eventBus(), probabilisticDemo);
        final RuntimeWiring runtimeWiring = newRuntimeWiringBuilder(pulse.eventBus(), pulse.pulseRegistry())
                .scalar(ExtendedScalars.GraphQLLong)
                .type("Query", typeWiring -> typeWiring.dataFetcher("demographic", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("economy", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("environment", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("faulty", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("some_faulty", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("none", fetcher))
                .type("Query", typeWiring -> typeWiring.dataFetcher("some_none", fetcher))
                .build();

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(schemaRegistry, runtimeWiring);
        final GraphQL graphQLApi = GraphQL.newGraphQL(graphQLSchema).build();
        final GraphQLHandlerOptions graphQLHandlerOptions = new GraphQLHandlerOptions();
        return GraphQLHandler.create(graphQLApi, graphQLHandlerOptions);
    }

}
