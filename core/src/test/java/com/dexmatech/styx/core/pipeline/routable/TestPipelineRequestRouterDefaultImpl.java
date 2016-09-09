package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static com.dexmatech.styx.core.pipeline.routable.RoutablePipeline.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 5/09/16.
 */
public class TestPipelineRequestRouterDefaultImpl {

	private static HttpRequestReplyPipeline PIPELINE = HttpRequestReplyPipeline.pipeline().applyingRoutingStage(
			r -> StageResult.completeStageSuccessfullyWith(ok())).build();

	private RoutablePipeline DEFAULT_ROUTABLE_PIPELINE = defaultPipeline().using(PIPELINE).build();
	private RoutablePipeline BY_HOST_PIPELINE = matchingRequestsByHost("www.as.com").using(PIPELINE).build();
	private RoutablePipeline BY_PATH_PIPELINE = matchingRequestsByPathRegexPattern("/some_path.*").using(PIPELINE).build();

	@Test
	public void shouldCreateRouter() throws Exception {
		// given
		List<RoutablePipeline> pipelines = Arrays.asList(BY_HOST_PIPELINE, BY_PATH_PIPELINE, DEFAULT_ROUTABLE_PIPELINE);
		// when
		IPipelineRequestRouter router = PipelineRequestRouterDefaultImpl.from(pipelines);
		// then
		assertThat(router, notNullValue());
	}

	@Test
	public void shouldCreateRouterMovingDefaultRouterAtTail() throws Exception {
		// given
		List<RoutablePipeline> pipelines = Arrays.asList(BY_HOST_PIPELINE, DEFAULT_ROUTABLE_PIPELINE, BY_PATH_PIPELINE);
		// when
		PipelineRequestRouterDefaultImpl router = PipelineRequestRouterDefaultImpl.from(pipelines);
		// then
		assertThat(router, notNullValue());
		assertThat(router.getPipelines().get(2).getTypeRoute(), is(TypeRoute.DEFAULT));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingRouterWhenNoDefaultPipelineIsProvided() {
		// when
		PipelineRequestRouterDefaultImpl.from(Arrays.asList(BY_HOST_PIPELINE, BY_PATH_PIPELINE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingRouterWhenMoreThanOneDefaultPipelineIsProvided() {
		// when
		PipelineRequestRouterDefaultImpl
				.from(Arrays.asList(BY_HOST_PIPELINE, DEFAULT_ROUTABLE_PIPELINE, BY_PATH_PIPELINE, DEFAULT_ROUTABLE_PIPELINE));
	}

	@Test
	public void shouldRouteToDefaultWhenRequestNotMatchWithPreviousRoutes() throws URISyntaxException {
		// given
		IPipelineRequestRouter router = PipelineRequestRouterDefaultImpl.from(
				Arrays.asList(BY_HOST_PIPELINE, BY_PATH_PIPELINE, DEFAULT_ROUTABLE_PIPELINE)
		);
		// when
		RoutablePipeline pipelineAssigned = router.route(HttpRequest.get("http://www.sport.es"));
		// then
		assertThat(pipelineAssigned,is(DEFAULT_ROUTABLE_PIPELINE));
	}

	@Test
	public void shouldRouteRequestToPipelineByHost() throws URISyntaxException {
		// given
		IPipelineRequestRouter router = PipelineRequestRouterDefaultImpl.from(
				Arrays.asList(BY_HOST_PIPELINE, BY_PATH_PIPELINE, DEFAULT_ROUTABLE_PIPELINE)
		);
		// when
		RoutablePipeline pipelineAssigned = router.route(HttpRequest.get("http://www.as.com/some_path"));
		// then
		assertThat(pipelineAssigned,is(BY_HOST_PIPELINE));
	}

	@Test
	public void shouldRouteRequestToPipelineByPath() throws URISyntaxException {
		// given
		IPipelineRequestRouter router = PipelineRequestRouterDefaultImpl.from(
				Arrays.asList(BY_HOST_PIPELINE, BY_PATH_PIPELINE, DEFAULT_ROUTABLE_PIPELINE)
		);
		// when
		RoutablePipeline pipelineAssigned = router.route(HttpRequest.get("http://www.dexcell.com/some_path"));
		// then
		assertThat(pipelineAssigned,is(BY_PATH_PIPELINE));
	}

}