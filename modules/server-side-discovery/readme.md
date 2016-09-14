# Server side discovery stage

This is request stage that adds a routing header to the request, in order to redirect on routing stage to the correct internal server

## What is a server-side service discovery pattern 

[pattern](http://microservices.io/patterns/server-side-discovery.html)


## Installation

Maven:
```xml
<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>server-side-discovery</artifactId>
  <version>VERSION</version>
</dependency>

```

## Usage

The usage is simple, you add routes to your internal load balancers, the they will resolve the real services ips.

Imagine that you have three internal load balancers:

1. api1.service.dmz -> resolve ips for api v1
2. api2.service.dmz -> resolve ips for api v1
3. default.service.dmz -> resolve other ips

```java
RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withHostRoutingRule("/api/v1/.*","api1.service.dmz")
				.withHostRoutingRule("/api/v2/.*","api2.service.dmz")
				.withDefaultHostRule("default.service.dmz")
				.build();

```

Then if a request enter to a pipeline with:

    request : http://www.mydomain.com:8080/api/v1/users
    
This stage will add a custom http header to the request:

    X-route-to: http://api1.service.dmz:8080/api/v1/users
    
## Hiding internal api paths

Imagine you want have some paths that you want to hide, for example you have an internal api with

- /api/items (for public purposes)
- /api/users (for internal use)

This two calls are in the same microservice or internal api but you don't want to expose /api/users to the world.

Then add to a blacklist:

```java
RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withBlackList(Arrays.asList("/api/users.*"))
				.withDefaultHostRule("default.service.dmz")
				.build();

```

All request to /api/users path will be aborted returning an 404 error



