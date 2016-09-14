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

The usage is simple, you add routes to your internal load balancers, the they will resolve the real services ips

```java
RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withHostRoutingRule("/some_path.*","api2.service.dmz")
				.withDefaultHostRule("api.service.dmz")
				.build();

```

