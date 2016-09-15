package com.dexmatech.styx.core.pipeline;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.http.StatusLine;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.pipeline.stages.response.ResponsePipelineStage;
import com.dexmatech.styx.core.pipeline.stages.routing.RoutingStage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.dexmatech.styx.core.http.HttpResponse.internalServerError;
import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.stageSuccessWith;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Created by aortiz on 11/08/16.
 */
public class TestHttpRequestReplyPipeline {

	public static final RoutingStage SIMPLE_ROUTING_STAGE
			= request -> completedFuture(stageSuccessWith(ok()));
	public static final RequestPipelineStage SIMPLE_REQUEST_STAGE
			= request -> completedFuture(stageSuccessWith(request));
	public static final ResponsePipelineStage SIMPLE_RESPONSE_STAGE
			= response -> completedFuture(stageSuccessWith(response));

	public static final RoutingStage NON_EXPECTED_ERROR_STAGE
			= request -> {
		int causingArithmeticException = 1 / 0;
		return completedFuture(stageSuccessWith(ok()));
	};

	@Test
	public void shouldApplySimpleRoutingStage() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingRoutingStage(SIMPLE_ROUTING_STAGE)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse, is(ok()));

	}

	@Test
	public void shouldApplyStaticRouteGenerationStage() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingStaticHostOnRouteGeneration("internal.host")
				.applyingRoutingStage(SIMPLE_ROUTING_STAGE)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse, is(ok()));

	}

	@Test
	public void shouldApplySimpleAllStages() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline;
		pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage("simple-pre-routing", SIMPLE_REQUEST_STAGE)
				.applyingRoutingStage(SIMPLE_ROUTING_STAGE)
				.applyingPostRoutingStage("simple-post-routing", SIMPLE_RESPONSE_STAGE)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse, is(ok()));
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenRoutingStageIsNotApplied() throws Exception {

		// when
		HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage("simple-pre-routing", SIMPLE_REQUEST_STAGE)
				.applyingPostRoutingStage("simple-post-routing", SIMPLE_RESPONSE_STAGE)
				.build();

	}

	@Test
	public void shouldApplyChangesOnRequest() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline;
		pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage("adding-pre-header-1", r -> completedFuture(stageSuccessWith(r.addHeader("pre-routing-1", "OK"))))
				.applyingPreRoutingStage("adding-pre-header-2", r -> completedFuture(stageSuccessWith(r.addHeader("pre-routing-2", "OK"))))
				.applyingRoutingStage(
						r -> completedFuture(
								stageSuccessWith(HttpResponse.from(StatusLine.ok(), r.getHeaders().put("routing", "OK")))
						)
				)
				.applyingPostRoutingStage("adding-post-header-1",
						r -> completedFuture(
								stageSuccessWith(HttpResponse.from(StatusLine.ok(), r.getHeaders().put("post-routing-1", "OK")))
						)
				)
				.applyingPostRoutingStage("adding-post-header-1",
						r -> completedFuture(
								stageSuccessWith(HttpResponse.from(StatusLine.ok(), r.getHeaders().put("post-routing-1", "OK")))
						)
				)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse.getHeaders().contains("pre-routing-1"), is(true));
		assertThat(httpResponse.getHeaders().contains("pre-routing-2"), is(true));
		assertThat(httpResponse.getHeaders().contains("routing"), is(true));
		assertThat(httpResponse.getHeaders().contains("pre-routing-1"), is(true));
		assertThat(httpResponse.getHeaders().contains("pre-routing-2"), is(true));
	}

	@Test
	public void shouldControlInternallyNonExpectedErrorsReturningAnInternalServerError() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline;
		pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage("simple-pre-routing", SIMPLE_REQUEST_STAGE)
				.applyingRoutingStage(NON_EXPECTED_ERROR_STAGE)
				.applyingPostRoutingStage("simple-post-routing", SIMPLE_RESPONSE_STAGE)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse, is(internalServerError()));

	}

	@Test
	public void shouldConsumeNonExpectedErrorWhenWeProvideAnErrorConsumer() throws Exception {

		// given
		List<String> errorsReported = new ArrayList<>();
		HttpRequest httpRequest = HttpRequest.get("/");
		HttpRequestReplyPipeline pipeline;
		pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingPreRoutingStage("simple-pre-routing", SIMPLE_REQUEST_STAGE)
				.applyingRoutingStage(NON_EXPECTED_ERROR_STAGE)
				.applyingPostRoutingStage("simple-post-routing", SIMPLE_RESPONSE_STAGE)
				.handlingNonExpectedErrorsWith(
						(request, throwable) ->
								errorsReported.add(String.format("Error in '%s' caused by '%s'", request, throwable.getCause()))
				)
				.build();
		// when
		HttpResponse httpResponse = pipeline.applyHttpReplyProtocol(httpRequest).get();

		// then
		assertThat(httpResponse, is(internalServerError()));
		assertThat(errorsReported, hasSize(1));

	}
}