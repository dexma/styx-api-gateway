package com.dexmatech.styx.core;

import com.dexmatech.styx.core.pipeline.routable.RoutablePipeline;
import com.dexmatech.styx.core.pipeline.stages.routing.RoutingStage;
import org.junit.Test;

import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline.pipeline;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.stageSuccessWith;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by aortiz on 9/08/16.
 */
public class TestApiPipelineBuilder {

	public static final RoutingStage ROUTING_STAGE = r -> completedFuture(stageSuccessWith(ok()));

	@Test
	public void shouldCreateSimplestApiPipeline() {
		// when
		ApiPipeline apiPipeline = ApiPipeline
				.singlePipeline()
				.using(pipeline().applyingDefaultRoutingStage().build())
				.build();
		// Then
		assertThat("Api pipeline was null", apiPipeline, notNullValue());
	}

	@Test
	public void shouldCreateSimpleApiPipeline() throws Exception {

		// When
		ApiPipeline apiPipeline = ApiPipeline.
				singlePipeline()
				.using(
						pipeline()
								.applyingPreRoutingStage("some-action", httpRequest -> null)
								.applyingRoutingStage(ROUTING_STAGE)
								.build()
				).build();

		// Then
		assertThat("Api pipeline was null", apiPipeline, notNullValue());
	}

	@Test
	public void shouldCreateMultipleApiPipeline() throws Exception {

		// When
		ApiPipeline apiPipeline = ApiPipeline.
				multiPipeline()
				.addPipeline(
						RoutablePipeline
								.matchingRequestsBy("www.dexcell.com", "/apiv3/.*").using(
								pipeline()
										.applyingRoutingStage(ROUTING_STAGE)
										.build()
						).build()
				)
				.addPipeline(RoutablePipeline.defaultPipeline().using(
						pipeline()
								.applyingRoutingStage(ROUTING_STAGE)
								.build()
						).build()
				)
				.build();

		// Then
		assertThat("Api pipeline was null", apiPipeline, notNullValue());
	}
}