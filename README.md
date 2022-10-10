### ⚠️ [WORK-IN-PROGRESS] 

[![Build & Test](https://github.com/margostino/graphql-pulse/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/margostino/graphql-pulse/actions/workflows/main.yml)

# graphql-pulse (GPulse)

GraphQL Pulse is a **distributed** and **lightweight** metric collector for GraphQL queries. Users run queries and GPulse collects and serves metrics as part of the GraphQL server implementation.  

GPulse allows you to extract as many insights as you want from your GraphQL queries such as:

- How many invalid results
- How many requests by type/field
- Query Latency by type/field
- Least recently used
- Last usage within X time (e.g. 24h)


## Micrometer integration

Optionally, GPulse is able to use any registry from Micrometer and publish the metrics collected as well.
These metrics will be published under `graphql_pulse_*` prefix.

#### Example: 
```
# HELP graphql_pulse_requests_count_total  
# TYPE graphql_pulse_requests_count_total counter
graphql_pulse_requests_count_total{field="total_cars",type="some_none",} 2.0
graphql_pulse_requests_count_total{field="average_of_something",type="faulty",} 3.0
graphql_pulse_requests_count_total{field="government_debt",type="economy",} 1.0
graphql_pulse_requests_count_total{field="total_insurance_companies",type="some_none",} 2.0
graphql_pulse_requests_count_total{field="average_of_employees",type="some_faulty",} 2.0
graphql_pulse_requests_count_total{field="total_hospitals",type="some_faulty",} 2.0
graphql_pulse_requests_count_total{field="population_between_30_39",type="demographic",} 1.0
graphql_pulse_requests_count_total{field="population",type="demographic",} 1.0
graphql_pulse_requests_count_total{field="total_companies",type="none",} 2.0
```

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

