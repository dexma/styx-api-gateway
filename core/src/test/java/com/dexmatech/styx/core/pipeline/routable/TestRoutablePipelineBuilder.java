package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import org.junit.Test;

import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static com.dexmatech.styx.core.pipeline.routable.RoutablePipeline.TypeRoute.*;
import static com.dexmatech.styx.core.pipeline.routable.RoutablePipeline.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 5/09/16.
 */
public class TestRoutablePipelineBuilder {


	private static HttpRequestReplyPipeline PIPELINE = HttpRequestReplyPipeline.pipeline().applyingRoutingStage(
			r -> StageResult.completeStageSuccessfullyWith(ok())).build();

	@Test
	public void shouldCreateDefaultRoutablePipeline() {
		// when
		RoutablePipeline pipeline = defaultPipeline().using(PIPELINE).build();
		// then
		assertThat(pipeline.getTypeRoute(), is(DEFAULT));
	}

	@Test
	public void shouldCreateMatchingByHostRoutablePipeline() {
		// when
		RoutablePipeline pipeline = matchingRequestsByHost("127.0.0.1").using(PIPELINE)
				.build();
		// then
		assertThat(pipeline.getTypeRoute(), is(MATCHING_BY_HOST));
	}

	@Test
	public void shouldCreateMatchingRequestsByPathRegexPatternRoutablePipeline() {
		// when
		RoutablePipeline pipeline = matchingRequestsByPathRegexPattern("/*").using(PIPELINE).build();
		// then
		assertThat(pipeline.getTypeRoute(), is(MATCHING_BY_PATH_REGEX_PATTERN));
	}

	@Test
	public void shouldCreateMatchingRequestsByAllRoutablePipeline() {
		// when
		RoutablePipeline pipeline = matchingRequestsBy("127.0.0.1", "/*").using(PIPELINE).build();
		// then
		assertThat(pipeline.getTypeRoute(), is(MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingMatchingByHostRoutablePipelineWhenMalformedHost() {
		// when
		matchingRequestsByHost("127.0.0.0.0.0").using(PIPELINE).build();

	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingMatchingByAllWhenMalformedHost() {
		// when
		matchingRequestsBy("127.0.0.0.0.0", "/").using(PIPELINE).build();

	}

	@Test(expected = NullPointerException.class)
	public void shouldFailCreatingRoutablePipelineWithoutPipeline() {
		// when
		defaultPipeline().build();

	}


}