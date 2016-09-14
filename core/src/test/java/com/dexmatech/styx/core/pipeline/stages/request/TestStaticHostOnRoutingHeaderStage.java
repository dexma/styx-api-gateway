package com.dexmatech.styx.core.pipeline.stages.request;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 14/09/16.
 */
public class TestStaticHostOnRoutingHeaderStage {

	public static final String HEADER = DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE;

	@Test
	public void shouldCompleteStageAddingRouteHeaderWhenStaticHost() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = StaticHostOnRoutingHeaderStage.staticRoutingTo("api.service.dmz");
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().contains(HEADER), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get(HEADER), is("http://api.service.dmz/"));
	}

	@Test
	public void shouldCompleteStageAddingRouteHeaderWhenStaticHostAndResultingRouteHeader() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = StaticHostOnRoutingHeaderStage.staticRoutingTo("api.service.dmz", "X-custom-route");
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().contains("X-custom-route"), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get("X-custom-route"), is("http://api.service.dmz/"));
	}

}