package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 14/09/16.
 */
public class TestServerSideDiscoveryStage {

	private static final String DEFAULT_HEADER = DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE;

	@Test
	public void shouldCompleteStageAddingRoutingHeaderWithDefaultHostRule() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = ServerSideDiscoveryStage.usingDefaults().withDefaultHostRule("api.service.dmz").build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().contains(DEFAULT_HEADER), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get(DEFAULT_HEADER), is("http://api.service.dmz/"));
	}

	@Test
	public void shouldCompleteStageAddingRoutingHeaderWithDefaultHostRuleAndExpectedHEaderIsChanged() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.changingResultingCustomRoutingHeaderTo("X-custom-route")
				.withDefaultHostRule("api.service.dmz")
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().contains("X-custom-route"), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get("X-custom-route"), is("http://api.service.dmz/"));
	}

	@Test
	public void shouldCompleteStageAddingRoutingHeaderMatchingHostRule() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/some_path?param=1");
		RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withHostRoutingRule("/some_path.*","api2.service.dmz")
				.withDefaultHostRule("api.service.dmz")
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().contains(DEFAULT_HEADER), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get(DEFAULT_HEADER), is("http://api2.service.dmz/some_path?param=1"));
	}

	@Test
	public void shouldCompleteStageFailingWhenRequestedPathIsInBlackList() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/apiv3/users");
		RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withBlackList(Arrays.asList("/apiv3/users.*"))
				.withDefaultHostRule("api.service.dmz")
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(false));
		assertThat(stageResult.getFail().getStatusLine().getStatusCode(), is(404));
		assertThat(stageResult.getFailCause().getMessage(), is("Aborting stage because path '/apiv3/users' is in blacklist '[/apiv3/users.*]'"));
	}

	@Test
	public void shouldCompleteStageFailingWhenRequestedPathIsInBlackListAndResponseIsChanged() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/apiv3/users");
		RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withBlackList(Arrays.asList("/apiv3/users.*"))
				.whenBlackListMatchRespondWith(HttpResponse.unauthorized())
				.withDefaultHostRule("api.service.dmz")
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(false));
		assertThat(stageResult.getFail().getStatusLine().getStatusCode(), is(403));
		assertThat(stageResult.getFailCause().getMessage(), is("Aborting stage because path '/apiv3/users' is in blacklist '[/apiv3/users.*]'"));
	}

}