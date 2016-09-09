package com.dexmatech.styx.core.pipeline;

import com.dexmatech.styx.core.pipeline.stages.routing.RoutingStage;
import org.junit.Test;

import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.stageSuccessWith;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 11/08/16.
 */
public class TestHttpRequestReplyPipelineBuilder {

	public static final RoutingStage SIMPLE_ROUTING_STAGE
			= request -> completedFuture(stageSuccessWith(ok()));

	@Test
	public void shouldCreateSimplePipeline() throws Exception {

		// when
		HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.applyingRoutingStage(SIMPLE_ROUTING_STAGE)
				.build();

		// then
		assertThat(pipeline, notNullValue());

	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenNoRoutingStageIsProvided() throws Exception {

		// when
		HttpRequestReplyPipeline pipeline = HttpRequestReplyPipeline
				.pipeline()
				.build();

		// then
		assertThat(pipeline, notNullValue());

	}
}