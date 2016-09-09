# Styx Api gateway

Styx is an async and non-blocking API Gateway developed by dexmatech that handle all incoming request and redirect to internal servers.

## Table of contents


- [What is an API Gateway?](#what-is-an-api-gateway)
- [Quick start](#quick-start)
- [Main concepts](#main-concepts)
- [Usage](#usage)
- [Creators](#creators)
- [Contributing](#contributing)


## What is an API Gateway

Api gateway encapsulates the internal system architecture and provides a single entry point to access to our internal API's, normally this 
term is strongly associated to microservice-based architectures.

In a simple concept, the main responsibilities of an API gateways are filter and redirect between external client requests and internal 
APIs.

![alt tag](/misc/api_gateway.png)

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
 
## Quick start 

## Main concepts 

### Request-reply pipeline

![alt tag](/misc/request-reply-pipeline.png)

The main idea of the project is simple, given a bunch of incoming requests from the outside world, they will be handled 
by a pipeline.

A pipeline has one entry for an http request and one outcome with the resulting http response 

One single pipeline will manage all incoming request in three different phases:
- First, apply request stages
- Second, apply routing stage
- Third, apply response stages

If there is any error or stage fails, pipeline will abort the rest of the pipeline and immediately will reply with an http response 

### Request stages

![alt tag](/misc/request-stage.png)

In a simple concept, and thinking in a functional way, a 'request stage' is a simple function: 

    request -> request
    
For example, an authentication stage could be:

- Given a request with an authentication token as header
- It will 


Pipeline will apply all stages asynchronously, and also a request stage could success or fail, then reality is that a 'request stage' is a 
function that:
 
    Request -> [Future[StageResult[Request]]]
    


### Routing stages

### Response stages

### Http server

## Usage

### Create single pipeline

### Create multi pipeline

### Run pipeline over http server

### Implementing new stage

### Handling with errors

### Resiliency

### Monitoring
 
## Contributing

Of course, we encourage the community to contribute with us and add specific modules and integrations with 3rd party systems!

Bug reporting and Pull Requests are welcome.




