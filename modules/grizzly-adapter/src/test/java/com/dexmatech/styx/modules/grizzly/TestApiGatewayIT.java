package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.ApiPipeline;
import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import com.dexmatech.styx.utils.SocketUtils;
import com.dexmatech.styx.utils.jetty.LocalTestServer;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.dexmatech.styx.utils.jetty.LocalTestServer.setUpLocalServer;
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
			Response response = CLIENT.prepareGet("http://localhost:"+randomPort+"/")
					.addHeader(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE,"http://localhost:"+internalEndpoint.getRunningPort())
					.execute()
					.get();
			// then
			assertThat(response, notNullValue());
			assertThat(response.getStatusCode(), is(200));
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
			Response response = CLIENT.prepareGet("http://localhost:"+randomPort+"/path")
					.execute()
					.get();
			// then
			assertThat(response, notNullValue());
			assertThat(response.getStatusCode(), is(500));
		});

	}

}