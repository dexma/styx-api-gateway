# Styx Api gateway
[![Build Status](https://travis-ci.org/dexma/styx-api-gateway.svg?branch=master)](https://travis-ci.org/dexma/styx-api-gateway)

Styx is an async and non-blocking API Gateway developed by dexmatech that handle all incoming request and redirect to internal servers.

## Table of contents


- [What is an API Gateway?](#what-is-an-api-gateway)
- [Quick start](#quick-start)
    - [Installation](#installation)
- [Main concepts](#main-concepts)
    - [Http proxy](#http-proxy)
    - [Request-reply pipeline](#request-reply-pipeline)
    - [Request stages](#request-stages)
    - [Routing stage](#routing-stage)
    - [Response stages](#response-stages)
    - [Http server](#http-server)
- [Usage](#usage)
    - [Create single pipeline](#create-single-pipeline)
    - [Create multi pipeline](#create-multi-pipeline)
    - [Run pipeline over http server](#run-pipeline-over-http-server)
    - [Default routing stage](#default-routing-stage)
    - [Implementing stage](#implementing-stage)
    - [Implementing complex stage](#implementing-complex-stage)
    - [Handling with errors](#handling-with-errors)
    - [Resiliency](#resiliency)
    - [Monitoring](#monitoring)
    - [Changing http server](#changing-http-server)
- [Modules](#modules)
- [Authors](#authors)
- [Contributing](#contributing)


## What is an API Gateway

Api gateway encapsulates the internal system architecture and provides a single entry point to access to our internal API's, normally this 
term is strongly associated to microservice-based architectures.

In a simple concept, the main responsibilities of an API gateways are filter and redirect between external client requests and internal 
APIs.

![alt tag](/misc/api-gateway.png)

Usually it might have other responsibilities:
 
 - Authentication and security: Authenticate each client request and reject or permit pass to the systems, also manage ssl
 - Rate limiting: Applies rate limit policies to our request clients  
 - Service discovery(client or server) : Responsible to know the internal address of each microservice with which it communicates
 - Request routing: Routing requests to the different backend services
 - Response composition: An API Gateway will often handle a request by invoking multiple microservices and aggregating the results.
 - Resiliency: Failures in an specific resources or clients should not break the rest (circuit-breaker)
 - Log metrics: Track statistics in order to give you a view of what is happening
 - Transport transformations: Sometimes your internal apis are managed in different transport protocols 
 - Caching
 - Logging
 
Please do not confuse API gateway with API management solutions, the second one could be a full solution that integrates design, 
management, documenting and other features around APIs, also it could integrate API gateways.
      
## Quick start
 
### Installation

Maven:
```xml
<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>styx-core</artifactId>
  <version>VERSION</version>
</dependency>

<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>grizzly-adapter</artifactId>
  <version>VERSION</version>
</dependency>

```

### Run api gateway

## Main concepts

Styx Api gateway tries to make easy integrate your internal APIs encouraging:

- Async: All actions are made using java completable futures
- NIO (non blocking IO): All http calls are implemented using an http nio client 
- Functional programing: We are not functional jihadists, but we use functions, immutability ...

Then, although for a basic use you don't need to do, if you extend the framework, please, don't try to use imperative programming, try to 
use nio drivers (if it is possible) and compose futures.

### Http proxy

![alt tag](/misc/reverse-proxy.png)

From a global vision an api gateway is an http proxy as 'nginx', a facade that acts as a front-end to control and protect our private 
network.

### Request-reply pipeline

![alt tag](/misc/request-reply-pipeline.png)

The main idea of the project is simple, given a bunch of incoming requests from the outside world, they will be handled 
by a pipeline.

A pipeline has one entry for an http request and one outcome with the resulting http response 

One single pipeline will manage all incoming request in three different phases:
- First, apply request stages
- Second, apply routing stage
- Third, apply response stages

If there is any error or stage fails, pipeline will abort the rest of the pipeline and immediately will reject the request.

There is no shared context or pipeline context, if you are thinking in some way about servlet filters, it is all about requests and 
responses. 

### Request stages

![alt tag](/misc/request-stage.png)

In a simple concept, and thinking in a functional way, a 'request stage' is a simple function: 

    request -> request
    
Pipeline will apply all stages asynchronously, and also a request stage could success or fail, then reality is that a 'request stage' is a 
function that:
 
    Request -> [Future[StageResult[Request]]]
    
For example, an authentication stage could be:

- Given a request with an authentication token client as header
- The function check if it is a valid token
    - If is not valid then abort stage, request will be rejected 
    - If is valid
        - Retrieve privileges associated to this token
        - Add privilege headers to the current request
- Final return the result:
    - Success with a request
    - Fail( aborting immediately the pipeline flow) with a response

### Routing stage

After request has passed all request stages then is the turn of routing stage.

The the simple firm is:

    Request -> [Future[StageResult[Response]]]

This is the proxy stage, it will take the request and it will create a new one, redirecting to internal servers in order to 
make the real request for the client.

Styx provide a default and basic implementation of routing stage, is totally async and nio, and uses a header to route:
    
    X-routing-url
    
But if you need to compose different internal calls or something more complex you will need to implement a new one.

### Response stages

In that point your request have been handled, and you have your response ready to be sent to the client.

If you need to add cookies, headers or anything you want to do in the response, this is the way:
 
    Response -> [Future[StageResult[Response]]]


### Http server

In order to handle real http request and responses, we will need a real http server in front over our pipelines. Styx uses 
[Grizzly](https://grizzly.java.net/) as default http server.

![alt tag](/misc/grizzly-adapter.png)

It is so simple, running pipelines in an http servers is a mapping between:

    http server request implementation -> styx request
And:

    styx response -> http server response implementation
    
Then, if you want to run styx over another server, add this mappers and make us a pull request.

## Usage

Styx uses internal dsl's and don't use magics as other frameworks, it means that if you are a java developer you need to code.

### Create single pipeline

All incoming request will be handled by this:

```java

// create http request-reply pipeline
HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingStaticRouteGenerationTo("internal.service.dmz")
				.applyingDefaultRoutingStage()
				.build();

// create single api pipeline
ApiPipeline apiPipeline = ApiPipeline.singlePipeline().using(pipeline).build();

```

### Create multi pipeline

Sometimes you will need more than one pipeline to handle you request, for example you could have:

   - Requests from host 'X' will have authentication 1 by token
   - Request with path '/some_path' will have rate limit but no authentication
   - The rest of your requests will be authenticated and rate-limited
   
```java

	// We create our pipelines
		RoutablePipeline pipelineWithAuthentication =
				matchingRequestsByHost("X").using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage("authentication", myAuthentication())
								.applyingStaticRouteGenerationTo("internal.service.dmz")
								.applyingDefaultRoutingStage()
								.build()
				).build();

		RoutablePipeline pipelineWithRateLimit =
				matchingRequestsByPathRegexPattern("/some_path.*").using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage("rate-limit", myRateLimit())
								.applyingStaticRouteGenerationTo("internal.service.dmz")
								.applyingDefaultRoutingStage()
								.build()
				).build();

		RoutablePipeline defaultPipeline =
				defaultPipeline().using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage("authentication", myAuthentication())
								.applyingPreRoutingStage("rate-limit", myRateLimit())
								.applyingStaticRouteGenerationTo("internal.service.dmz")
								.applyingDefaultRoutingStage()
								.build()
				).build();

		// create multi pipeline
		ApiPipeline apiPipeline = ApiPipeline
				.multiPipeline()
				.addPipeline(pipelineWithAuthentication)
				.addPipeline(pipelineWithRateLimit)
				.addPipeline(defaultPipeline).build();

```

### Default routing stage

Default routing stage is an NIO stage that acts as a proxy an makes the real requests to internal servers.
 
As default it only expects that the incoming request has a custom header to make the real route like:

        X-routing-url: http://internal.server.dmz:8080/api  

Then, you can do:
 
1. Use a default static implementation, it will redirect all request to same host:

```java
 HttpRequestReplyPipeline
   				.pipeline()
   				.applyingStaticRouteGenerationTo("internal.service.dmz") 
   		...
```
Then if a request enter with a requested uri as 

    http://www.mydomain.com:8080/api
    
This stage will add a custom header as 

    http://internal.service.dmz:8080/api
    
2. Create your own creation of routing with your own logic, remember to add **X-routing-url** custom header 

3. A module was added with [Server side discovery](/modules/server-side-discovery/readme.md) 

Also, you can override some parameters of default routing implementation:


```java
 RoutingStage routingstage = DefaultRoutingStage
 				.usingDefaults()
 				// if you want to change header used to route
 				.usingHeaderToRoute("X-my-custom-route")
 				// if you want to change how route is extracted, maybe you don't want to use a header
 				.usingStrategyToRoute(ROUTE_EXTRACTOR)
 				// if you want to make transformations on response after proxy success
 				// for example copy some headers from request to response
 				.applyAfterRoutingSuccess((httpRequest, httpResponse) -> httpResponse.addHeader("X",httpRequest.getHeaders().get("X")))
 				// if you want to provide an custom config
 				.usingDefaultClientAndConfig(new DefaultAsyncHttpClientConfig.Builder().build())
 				.build();
 				
// create http request-reply pipeline
HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingStaticRouteGenerationTo("internal.service.dmz")
				.applyingRoutingStage(routingstage)
				.build();

// create single api pipeline
ApiPipeline apiPipeline = ApiPipeline.singlePipeline().using(pipeline).build(); 				
 				
```

### Run pipeline over http server

In a simplest way :

```java
 ApiGateway.runningOverGrizzly().withPipeline(apiPipeline).build().startAndKeepRunning();
```

Or if you want to config some behaviour :

```java
 ApiGateway
        .runningOverGrizzly()
        .withDefaultServerRunningOnPort(8081)
        .withExecutorService(Executors.newFixedThreadPool(4))
        .withPipeline(apiPipeline)
        .build()
        .startAndKeepRunning();
```

### Implementing stage

How we said before, a request stage is a function:

        Request -> [Future[StageResult[Request]]]
        
Then, let's code a simple request stage that add some custom headers

```java

RequestPipelineStage stage = r -> CompletableFuture
                .completedFuture(
                        StageResult.stageSuccessWith(r.addHeader("X-some-header", "some-value"))
                        );

```

### Implementing complex stage

Previous sample is so easy, but it is not realistic, normally you will want to access to a remote repository, add dependencies or do more 
complicated things, then let' s do an more complex sample.
  
We will code an authentication stage, to reach that, we will codify a simple DSL that constructs our final function.
 
```java

import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.AbortedStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by aortiz on 9/08/16.
 */

public class AuthenticationStage {

	public static final String DEFAULT_TOKEN_HEADER = "X-token";

	public interface AuthenticationService {
		boolean authenticate(String token);
	}

	public static Builder usingDefaults() {
		return new Builder();
	}

	public static class Builder {

		private AuthenticationService authenticationService;
		private Optional<String> tokenHeader = Optional.empty();

		public Builder authenticatingWith(AuthenticationService authenticationService) {
			this.authenticationService = authenticationService;
			return this;
		}

		public Builder usingHeaderToken(String tokenHeader) {
			this.tokenHeader = Optional.ofNullable(tokenHeader);
			return this;
		}

		public RoutingStage build() {
			Objects.requireNonNull(authenticationService, "Please provide an authentication service");
			String headerName = tokenHeader.orElse(DEFAULT_TOKEN_HEADER);
			return httpRequest -> {

				Optional<String> token = Optional.ofNullable(httpRequest.getHeaders().get(headerName));
				return token.map(t -> {

					if (authenticationService.authenticate(t)) {
						return StageResult.completeStageSuccessfullyWith(httpRequest);
					} else {
						return StageResult.completeStageFailingWith(
								HttpResponse.forbidden(), AbortedStage.because("Invalid token")
						);
					}
				}).orElseGet(() ->
						StageResult.completeStageFailingWith(
								HttpResponse.internalServerError(), AbortedStage.because("Token not found")
						)
				);

			};

		}

	}

}

```

With a resulting usage:

```java

AuthenticationService authenticationService = new MyAuthenticationImpl(config, ...); 

RequestPipelineStage stage = AuthenticationStage
				.usingDefaults()
				.authenticatingWith()
				.usingHeaderToken("X-my-security-header")
				.build();

```

*Note: this is only a sample in order to view how to code a simple DSL to encapsulate an stage creation.*

### Handling with errors

    TODO

### Resiliency

    TODO

### Monitoring

    TODO

### Changing http server

    TODO

## Modules

Here are listed and linked all modules or extensions, they could be request stages, routing or anything usefull

Request stages:

- [Server side discovery](/modules/server-side-discovery/readme.md)
    

## Authors

* **Albert Ortiz Llousas** - *Initial work*  (albert.ortizl@gmail.com)
* **Dexma development team**


 
## Contributing

Of course, we encourage the community to contribute with us and add specific modules and integrations with 3rd party systems!

Bug reporting and Pull Requests are welcome.

See https://github.com/dexma/styx-api-gateway/contributors





