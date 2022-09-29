### ⚠️ [WORK-IN-PROGRESS] 
# graphql-pulse

GraphQL Pulse (GPulse) is a **distributed** and **lightweight** metric collector for GraphQL queries. Users run queries and GPulse collects and serves such metrics as part of the GraphQL server implementation.  

GPulse allows you to extract as much insights as you want from your GraphQL queries such as:

- How many invalid results
- How many requests by type/field
- Query Latency by type/field
- Least recently used
- Last usage within X time (e.g. 24h)

(in part it was inspired by [Shopify/graphql-metrics](https://github.com/Shopify/graphql-metrics))

## Micrometer integration

GPulse is able to use any registry from Micrometer and publish the metrics collected.

// TODO

## Demo

##### Build JAR:
```
./run build-jar
```

##### Build Docker image:
```
./run build-image
```

##### Build all (Jar + Docker Image):
```
./run build
```

##### Prepare configuration for NGINX
```
./run config
```

##### Start Demo (spin up 2 servers)
```
./run demo
```

##### Start dependencies (nginx + prometheus + grafana)
```
./run dependencies
```

##### Stop the world (all Docker containers)
```
./run stop
```

