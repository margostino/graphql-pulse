### ⚠️ [WORK-IN-PROGRESS] 
# graphql-pulse (GPulse)

GraphQL Pulse is a **distributed** and **lightweight** metric collector for GraphQL queries. Users run queries and GPulse collects and serves metrics as part of the GraphQL server implementation.  

GPulse allows you to extract as many insights as you want from your GraphQL queries such as:

- How many invalid results
- How many requests by type/field
- Query Latency by type/field
- Least recently used
- Last usage within X time (e.g. 24h)

(in part it was inspired by [Shopify/graphql-metrics](https://github.com/Shopify/graphql-metrics))

## Micrometer integration

Optionally, GPulse is able to use any registry from Micrometer and publish the metrics collected as well.

// TODO

## Demo

##### Build:
```
./run build
```

##### Start Demo (spin up 2 servers)
```
./run demo
```

This will include dependencies containers for monitoring your cluster (Hazelcast Management Center) and a Nginx load balancer.

##### Stop the world (all Docker containers)
```
./run stop
```

