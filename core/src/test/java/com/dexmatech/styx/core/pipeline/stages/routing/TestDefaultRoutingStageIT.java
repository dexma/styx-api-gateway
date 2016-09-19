package com.dexmatech.styx.core.pipeline.stages.routing;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.testing.jetty.LocalTestServer;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.junit.Test;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import static com.dexmatech.styx.testing.jetty.LocalTestServer.setUpLocalServer;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestDefaultRoutingStageIT {

	private ObjectMapper MAPPER = new ObjectMapper();

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode(of = "id")
	private static class SimpleObject {
		private String id;
	}

	@Test
	public void shouldRouteSimpleRequest() throws Exception {
		// given
		LocalTestServer server = setUpLocalServer().respondingWith(200).build();
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + server.getRunningPort()));
		RoutingStage stage = DefaultRoutingStage.usingDefaults().build();
		// when
		server.runAndKill(() -> {
			StageResult<HttpResponse> result = stage.apply(request).get();
			// then
			assertThat(result.isSuccess(), is(true));
			assertThat(result.getSuccess().getStatusLine().getStatusCode(), is(200));
		});

	}

	@Test
	public void shouldRouteAndParseSimpleJson() throws Exception {
		// given

		SimpleObject simpleObject = new SimpleObject("some-id");
		LocalTestServer server = setUpLocalServer().respondingWith("application/json", MAPPER.writeValueAsBytes(simpleObject)).build();
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + server.getRunningPort()));
		RoutingStage stage = DefaultRoutingStage.usingDefaults().build();
		// when
		server.runAndKill(() -> {
			StageResult<HttpResponse> result = stage.apply(request).get();
			// then
			assertThat(result.isSuccess(), is(true));
			assertThat(result.getSuccess().getStatusLine().getStatusCode(), is(200));
			assertThat(result.getSuccess().getMessageBody().isPresent(), is(true));
			assertThat(result.getSuccess().getMessageBody().isPresent(), is(true));
			SimpleObject objectReturned = MAPPER.readValue(result.getSuccess().getMessageBody().get(), SimpleObject.class);
			assertThat(objectReturned, equalTo(simpleObject));
		});

	}

	@Test
	public void shouldRouteSimpleRequestAndThrowingInternalServerErrorWhenRequestTimeout() throws Exception {
		// given
		LocalTestServer server = setUpLocalServer().applyingDelayOnResponse(2000).respondingWith(200).build();
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:" + server.getRunningPort()));
		RoutingStage stage = DefaultRoutingStage
				.usingDefaults()
				.usingDefaultClientwithConfig(new DefaultAsyncHttpClientConfig.Builder().setRequestTimeout(1000).build())
				.build();
		// when
		server.runAndKill(() -> {
			StageResult<HttpResponse> result = stage.apply(request).get();
			// then
			assertThat(result.isFail(), is(true));
			assertThat(result.getFail().getStatusLine().getStatusCode(), is(500));
			assertThat(result.getFailCause(), instanceOf(TimeoutException.class));
		});

	}

	@Test
	public void shouldRouteSimpleRequestAndThrowingInternalServerErrorWhenExpectedEndpointDoesntRespond() throws Exception {
		// given
		HttpRequest request = HttpRequest.get("/", Headers.empty().put(DefaultRoutingStage
				.DEFAULT_HEADER_USED_TO_ROUTE, "http://localhost:5555"));
		RoutingStage stage = DefaultRoutingStage
				.usingDefaults()
				.usingDefaultClientwithConfig(new DefaultAsyncHttpClientConfig.Builder().build())
				.build();
		// when

		StageResult<HttpResponse> result = stage.apply(request).get();
		// then
		assertThat(result.isFail(), is(true));
		assertThat(result.getFail().getStatusLine().getStatusCode(), is(500));
		assertThat(result.getFailCause(), instanceOf(ConnectException.class));
		assertThat(result.getFailCause().getMessage(), is("Connection refused: localhost/127.0.0.1:5555"));

	}


}