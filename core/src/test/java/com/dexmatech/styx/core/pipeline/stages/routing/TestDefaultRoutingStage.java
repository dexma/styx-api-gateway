package com.dexmatech.styx.core.pipeline.stages.routing;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.AbortedStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.utils.asynchttpclient.ClientResponse;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by aortiz on 8/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultRoutingStage {

	@Mock
	private DefaultAsyncHttpClient client;

	@Test
	public void shouldApplyingTransformationOfCopySomeParamFromRequestToResponse() throws Exception {
		// given

		HttpRequest request = HttpRequest.get(
				"/", Headers.from("request-header", "value").put(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE, "http://www.as.com")
		);
		RoutingStage stage = DefaultRoutingStage
				.usingDefaults()
				.applyAfterRoutingSuccess((req, resp) ->
						Optional.ofNullable(req.getHeaders().get("request-header")).map(h -> resp.addHeader("request-header", h)).get()
				)
				.withConfiguredClient(client)
				.build();
		// when
		when(client.executeRequest(any(Request.class))).thenReturn(ClientResponse.fake().buildAsFuture());
		StageResult<HttpResponse> result = stage.apply(request).get();

		// then
		assertThat(result.isSuccess(), is(true));
		assertThat(result.getSuccess().getHeaders().contains("request-header"), is(true));
	}

	@Test
	public void shouldRouteSimpleRequest() throws Exception {
		// given

		HttpRequest request = HttpRequest.get("/", Headers.from(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE, "http://www.as.com"));
		RoutingStage stage = DefaultRoutingStage.usingDefaults().withConfiguredClient(client).build();
		// when
		when(client.executeRequest(any(Request.class))).thenReturn(ClientResponse.fake().buildAsFuture());
		StageResult<HttpResponse> result = stage.apply(request).get();

		// then
		assertThat(result.isSuccess(), is(true));
		assertThat(result.getSuccess().getStatusLine().getStatusCode(), is(200));
	}

	@Test
	public void shouldRouteSimpleRequestWhenRouteHeaderIsInLowerCase() throws Exception {
		// given

		HttpRequest request = HttpRequest.get("/", Headers.from(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE.toLowerCase(), "http://www.as.com"));
		RoutingStage stage = DefaultRoutingStage.usingDefaults().withConfiguredClient(client).build();
		// when
		when(client.executeRequest(any(Request.class))).thenReturn(ClientResponse.fake().buildAsFuture());
		StageResult<HttpResponse> result = stage.apply(request).get();

		// then
		assertThat(result.isSuccess(), is(true));
		assertThat(result.getSuccess().getStatusLine().getStatusCode(), is(200));
	}

	@Test
	public void shouldRouteWhenDefaultRouteExtractorIsChanged() throws Exception {
		// given

		HttpRequest request = HttpRequest.get("/");
		RoutingStage stage = DefaultRoutingStage
				.usingDefaults()
				.usingStrategyToRoute(r -> Optional.of("http://localhost"))
				.withConfiguredClient(client)
				.build();
		// when
		when(client.executeRequest(any(Request.class))).thenReturn(ClientResponse.fake().buildAsFuture());
		StageResult<HttpResponse> result = stage.apply(request).get();

		// then
		assertThat(result.isSuccess(), is(true));
		assertThat(result.getSuccess().getStatusLine().getStatusCode(), is(200));
	}

	@Test
	public void shouldFailWhenNoHeaderToRouteIsFound() throws Exception {
		// given

		HttpRequest request = HttpRequest.get(
				"/", Headers.from("request-header", "value")
		);
		RoutingStage stage = DefaultRoutingStage
				.usingDefaults()
				.applyAfterRoutingSuccess((req, resp) ->
						Optional.ofNullable(req.getHeaders().get("request-header")).map(h -> resp.addHeader("request-header", h)).get()
				)
				.withConfiguredClient(client)
				.build();
		// when
		when(client.executeRequest(any(Request.class))).thenReturn(ClientResponse.fake().buildAsFuture());
		StageResult<HttpResponse> result = stage.apply(request).get();
		// then
		assertThat(result.isFail(), is(true));
		assertThat(result.getFail().getStatusLine().getStatusCode(), is(500));
		assertThat(result.getFailCause(), instanceOf(AbortedStage.class));
		assertThat(result.getFailCause().getMessage(),
				startsWith("Impossible extract route from header"));
	}

}