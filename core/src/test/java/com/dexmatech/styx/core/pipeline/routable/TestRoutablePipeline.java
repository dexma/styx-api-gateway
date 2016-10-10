package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import org.junit.Test;

import java.net.URISyntaxException;

import static com.dexmatech.styx.core.http.HttpResponse.ok;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by aortiz on 5/09/16.
 */
public class TestRoutablePipeline {

	private static HttpRequestReplyPipeline PIPELINE = HttpRequestReplyPipeline.pipeline().applyingRoutingStage(
			r -> StageResult.completeStageSuccessfullyWith(ok())).build();

	@Test
	public void shouldHandleRequestWhenDefaultPipeline() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.defaultPipeline().using(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}

	@Test
	public void shouldHandleRequestMatchingByHostWhenHostMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByHost("www.as.com").using(PIPELINE)
				.build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}

	@Test
	public void shouldNotHandleRequestMatchingByHostWhenHostDoesntMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByHost("www.ass.com").using(PIPELINE)
				.build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(false));

	}


	@Test
	public void shouldHandleRequestMatchingByPathWhenPathMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByPathRegexPattern("/soccer.*")
				.using
				(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer/asd");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}


	@Test
	public void shouldHandleRequestMatchingByPathWhenPathMatchWithComplexRegex() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByPathRegexPattern(".*(/v3/ping|/v3/app-status|/v3/info|/v3/oauth).*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/v3/ping");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}

	@Test
	public void shouldHandleRequestMatchingByPathWhenPathMatchWithQueryParams() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByPathRegexPattern("/soccer.*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer?test=true");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}

	@Test
	public void shouldNotHandleRequestMatchingByPathWhenPathDoesntMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsByPathRegexPattern("/soccer/.*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/socccer/asd");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(false));

	}

	@Test
	public void shouldHandleRequestMatchingByAllWhenHostAndPathMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsBy("www.as.com","/soccer/.*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/soccer/asd");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(true));

	}

	@Test
	public void shouldNotHandleRequestMatchingByAllWhenHostMatchAndPathNotMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsBy("www.as.com","/soccer/.*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.as.com/socccer/asd");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(false));

	}

	@Test
	public void shouldNotHandleRequestMatchingByAllWhenHostNotMatchAndPathMatch() throws URISyntaxException {
		// given
		RoutablePipeline pipeline = RoutablePipeline.matchingRequestsBy("www.as.com","/soccer/.*")
				.using
						(PIPELINE).build();
		HttpRequest request = HttpRequest.get("http://www.ass.com/soccer/asd");
		// when
		boolean handle = pipeline.canHandle(request);
		// then
		assertThat(handle, is(false));

	}

}