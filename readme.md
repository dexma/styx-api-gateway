# Styx Api gateway
[![Build Status](https://travis-ci.org/dexma/styx-api-gateway.svg?branch=master)](https://travis-ci.org/dexma/styx-api-gateway)

Styx is an async and non-blocking API Gateway developed by dexmatech that handle all incoming request and redirect to internal servers.

## Table of contents


- [What is an API Gateway?](#what-is-an-api-gateway)
- [Quick start](#quick-start)
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
    - [Implementing stage](#implementing-stage)
    - [Implementing complex stage](#implementing-complex-stage)
    - [Handling with errors](#handling-with-errors)
    - [Resiliency](#resiliency)
    - [Monitoring](#monitoring)
    - [Changing http server](#changing-http-server)
- [Creators](#creators)
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

```Java

// create http request-reply pipeline
HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage(myOwnImplementationOfRouteSelector())
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
   
```Java

	// We create our pipelines
		RoutablePipeline pipelineWithAuthentication =
				matchingRequestsByHost("X").using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage(myAuthentication())
								.applyingPreRoutingStage(myOwnImplementationOfRouteSelector())
								.applyingDefaultRoutingStage()
								.build()
				).build();

		RoutablePipeline pipelineWithRateLimit =
				matchingRequestsByPathRegexPattern("/some_path.*").using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage(myRateLimit())
								.applyingPreRoutingStage(myOwnImplementationOfRouteSelector())
								.applyingDefaultRoutingStage()
								.build()
				).build();

		RoutablePipeline defaultPipeline =
				defaultPipeline().using(
						HttpRequestReplyPipeline
								.pipeline()
								.applyingPreRoutingStage(myAuthentication())
								.applyingPreRoutingStage(myRateLimit())
								.applyingPreRoutingStage(myOwnImplementationOfRouteSelector())
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

### Run pipeline over http server

In a simplest way :

```Java
 ApiGateway.runningOverGrizzly().withPipeline(apiPipeline).build().startAndKeepRunning();
```

Or if you want to config some behaviour :

```Java
 ApiGateway
        .runningOverGrizzly()
        .withDefaultServerRunningOnPort(8081)
        .withExecutorService(Executors.newFixedThreadPool(4))
        .withPipeline(apiPipeline)
        .build()
        .startAndKeepRunning();
```

### Implementing stage

### Implementing complex stage

### Handling with errors

### Resiliency

### Monitoring

### Changing http server

## Creators
 
## Contributing

Of course, we encourage the community to contribute with us and add specific modules and integrations with 3rd party systems!

Bug reporting and Pull Requests are welcome.





