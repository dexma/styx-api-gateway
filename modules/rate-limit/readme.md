# Rate Limit

This is request stage that applies rate-limit process to your incoming requests:

- If rate limit is not reached
    - Allow to add the request to continue with the pipeline
    - Adds rate-limit custom headers to the response
- If If rate limit is reached
    - Abort an stage and returns an 429 error
    - Adds rate-limit custom headers to the response
       

## Installation

Maven:
```xml
<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>rate-limit</artifactId>
  <version>VERSION</version>
</dependency>

```

## Usage

For the simplest usage you only has to provide:

- Header key: We must to know where must extract the key in order to perform the rate limit stage
- Rate limit dependency: This is your work, provide us an implementation from who really rates the request

```java
RateLimitProvider myRateLimitProvider = // provide an implementation 

RequestPipelineStage stage = 
                    RateLimitStage.rateByHeader("X-token")
                    .withRateLimitProvider(rateLimitProvider)
                    .build();

```

If a request enter to a pipeline with:

    request -> GET http://www.mydomain.com:8080/api/v1/users
    headers -> X-token : Xxcew23ASDzxwsadsadsad
    
And stage returns a RateLimitStatus object with:
 
        allowed -> true
        current rate limits -> [(
                                   type prefix -> Hour
                                   limit -> 1000
                                   reset -> 100
                                   remaining -> 999
                                )]
   
Then the stage will always add to request/response with custom headers:

        X-Ratelimit-Hour-Limit: 1000
        X-Ratelimit-Hour-Reset: 100
        X-Ratelimit-Hour-Remaining: 999

#### Note

This is a request stage (request->request), then if stage is completed successfully it will add ratelimit headers to the request, 
but when the routing stage will be executed this headers will be lost.
 
This is in that way because ratelimit normally is get and update atomically, think in a concurrent application.

Also, request pipelines don't have any thread context or shared context to pass variables because is totally functional, you have only 
requests and responses.
 
To solve that we have provided an exchange state in default routing stage, a function:
 
    (request, response) -> response
    
It will be executed if routing stage success an then you can copy headers there.

Also we have provided in this module to an implementation of this function

```java
RateLimitProvider myRateLimitProvider = // provide an implementation 

// create your rate limit stage
RequestPipelineStage rateLimitStage = 
                    RateLimitStage.rateByHeader("X-token")
                    .withRateLimitProvider(rateLimitProvider)
                    .build();

// override default behaviour of default routing stage
RoutingStage routingstage = DefaultRoutingStage
 				.usingDefaults()
 				.applyAfterRoutingSuccess(RateLimitsHeaders.COPY_RATELIMIT_HEADERS_TO_RESPONSE)
 				.build();

// create http request-reply pipeline
HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingStaticHostOnRouteGeneration("internal.service.dmz")
				.applyingPreRoutingStage("rate-limit", rateLimitStage)
				.applyingRoutingStage(routingstage)
				.build();

```


## Rate limit provider

In order to apply rate limit you must to provide an implementation of RateLimitProvider:
```java
@FunctionalInterface
public interface RateLimitProvider {
	CompletableFuture<RateLimitStatus> apply(String key);
}
```

Normally, a rate limit is applied doing two process atomically:

- Update ratelimit hits
- Get current rate limit

Here you have some glue code to adapt your non-async code:

```java
    MyRateLimitService myRateLimitService = // your real non async rate limit service 

	RateLimitProvider rateLimitProvider = key -> {
    
    		return CompletableFuture.supplyAsync(() -> {
                MyRateLimits rateLimit = myRateLimitService.rate(key);
    			RateLimitStatus result = parseToStyxDomain(rateLimit);
    			return result;
    		};
    	};
```

This is only an example, for a custom usages please we recommend you to create a DSL as explained in 
[Implementing complex stage](../../readme-md)

