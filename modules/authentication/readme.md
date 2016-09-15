# Authentication

This is request stage applies authentication process to your incoming requests:

- If authentication success it will return a principal object
    - Adds security permission custom header to the request
    - Adds security metainfo custom headers to the request
- If authentication fails abort an stage and returns an 401 error (customizable)
       

## Installation

Maven:
```xml
<dependency>
  <groupId>com.dexmatech.styx</groupId>
  <artifactId>authentication</artifactId>
  <version>VERSION</version>
</dependency>

```
## Simplest usage

For the simplest usage you only has to provide:

- Header token key: We must to know where must extract the token in order to perform the authentication
- Authenticator dependency: This is your work, provide us an implementation from who really authenticates

```java
Authenticator authenticator 

RequestPipelineStage stage = AuthenticationStage
                    .authenticationByToken("X-token")
                    .withAuthenticator(authenticator)
                    .build();

```

If a request enter to a pipeline with:

    request -> GET http://www.mydomain.com:8080/api/v1/users
    headers -> X-token : Xxcew23ASDzxwsadsadsad
    
And authenticator returns a Principal object with:
 
        permissions -> [(user,R),(user,W),(items,R)]
        meta info -> [(account, 101)]
   
Then the stage will return an success request to the pipeline with custom headers:

        X-security-permissions: user:R,user:W,items:R
        X-security-account: 101


## Authenticator

In order to authenticate you must to provide an implementation of Authenticator:
```java
@FunctionalInterface
public interface Authenticator {
	CompletableFuture<Optional<Principal>> authenticate(String token);
}
```

Here you have some glue code to adapt your non-async code:

```java
    SecurityService securityService = // your real non async security service 

	Authenticator myAuthenticator = token -> {

		Optional<Principal> result = Optional.empty();
		// supose your service returns a context with all your authentication info
		YourSecurityContext yourContext = securityService.authenticate(token);
		if(yourContext.isAuthenticated()) {
			// here transform you permissions to our permissions (if not empty)
			List<Permission> permissions = transformToPermissions(yourContext.getMyPermissions());
			// here add any custom info you want in the request as headers
			MetaInfo metaInfo = MetaInfo.initWith("account", yourContext.getAccountId());
			return CompletableFuture.completedFuture(Optional.of(new Principal(permissions,metaInfo)));		

		}
		return CompletableFuture.completedFuture(result);
	};
```



