package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.ApiPipeline;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import com.dexmatech.styx.testing.SocketUtils;
import com.dexmatech.styx.testing.jetty.LocalTestServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.dexmatech.styx.testing.jetty.LocalTestServer.setUpLocalServer;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 9/09/16.
 */
public class TestApiGatewayIT {

	private AsyncHttpClient CLIENT = new DefaultAsyncHttpClient();

	@Test
	public void shouldHandleAndProxyToAnSimpleRequest() throws Exception {
		// given
		LocalTestServer internalEndpoint = setUpLocalServer().build();

		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline.pipeline().applyingDefaultRoutingStage().build()
		).build();

		int randomPort = SocketUtils.findRandomPort();
		ApiGateway apiGateway = ApiGateway
				.runningOverGrizzly()
				.withDefaultServerRunningOnPort(randomPort)
				.withExecutorService(Executors.newFixedThreadPool(4))
				.withPipeline(pipeline)
				.build();

		// when
		internalEndpoint.runAndKill(() -> {
			apiGateway.start();
			Response response = CLIENT.prepareGet("http://localhost:" + randomPort + "/asd?e=4&r=2")
					.addHeader(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + internalEndpoint.getRunningPort())
					.execute()
					.get();
			// then
			assertThat("Response was empty", response, notNullValue());
			assertThat("Response status code was wrong", response.getStatusCode(), is(200));
		});

	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@EqualsAndHashCode(of = { "stringProperty", "intProperty" })
	public static class TestObject {
		private String stringProperty;
		private int intProperty;
	}

	@Test
	public void shouldHandleAndProxyToAnSimpleRequestReturningJsonBody() throws Exception {
		// given
		TestObject object = new TestObject("some_property", 101);
		ObjectMapper objectMapper = new ObjectMapper();
		LocalTestServer internalEndpoint = setUpLocalServer()
				.respondingWith("application/json", objectMapper.writeValueAsBytes(object))
				.build();

		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline.pipeline().applyingDefaultRoutingStage().build()
		).build();

		int randomPort = SocketUtils.findRandomPort();
		ApiGateway apiGateway = ApiGateway
				.runningOverGrizzly()
				.withDefaultServerRunningOnPort(randomPort)
				.withExecutorService(Executors.newFixedThreadPool(4))
				.withPipeline(pipeline)
				.build();

		// when
		internalEndpoint.runAndKill(() -> {
			apiGateway.start();
			Response response = CLIENT.prepareGet("http://localhost:" + randomPort + "/asd?e=4&r=2")
					.addHeader(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + internalEndpoint.getRunningPort())
					.execute()
					.get();
			// then
			assertThat("Response was empty", response, notNullValue());
			assertThat("Response status code was wrong", response.getStatusCode(), is(200));
			assertThat("Response content type was wrong", response.getContentType(), is("application/json"));
			assertThat("Response content type was wrong", objectMapper.readValue(response.getResponseBodyAsBytes(), TestObject.class),
					is(object));
		});

	}

	@Test
	public void shouldHandleAndReturnAnInternalServerError() throws Exception {
		// given
		LocalTestServer internalEndpoint = setUpLocalServer().build();

		ApiPipeline pipeline = ApiPipeline.singlePipeline().using(
				HttpRequestReplyPipeline.pipeline().applyingDefaultRoutingStage().build()
		).build();
		int randomPort = SocketUtils.findRandomPort();
		ApiGateway apiGateway = ApiGateway
				.runningOverGrizzly()
				.withDefaultServerRunningOnPort(randomPort)
				.withExecutorService(Executors.newFixedThreadPool(4))
				.withPipeline(pipeline)
				.build();

		// when
		internalEndpoint.runAndKill(() -> {
			apiGateway.start();
			Response response = CLIENT.prepareGet("http://localhost:" + randomPort + "/path")
					.execute()
					.get();
			// then
			assertThat("Response was empty", response, notNullValue());
			assertThat("Response status code was wrong", response.getStatusCode(), is(500));
		});

	}

}