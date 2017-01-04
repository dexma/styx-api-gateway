package com.dexmatech.styx.modules.ratelimit;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.utils.IOUTils;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 16/09/16.
 */
public class TestRateLimitStage {

	public static final RateLimitProvider ALLOW_PROVIDER = token ->
			CompletableFuture.completedFuture(new RateLimitStatus(
					true,
					Arrays.asList(
							new RateLimit("Hour", 1000, Duration.ofSeconds(10), 80),
							new RateLimit("Day", 2000, Duration.ofSeconds(20), 160)
					)
			));

	public static final RateLimitProvider NOT_ALLOW_PROVIDER = token ->
			CompletableFuture.completedFuture(new RateLimitStatus(
					false,
					Arrays.asList(
							new RateLimit("Hour", 1000, Duration.ofSeconds(10), 80),
							new RateLimit("Day", 2000, Duration.ofSeconds(20), 160)
					)
			));

	@Test
	public void shouldCompleteStageAllowing() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/", Headers.from("X-token", "XXX"));
		RequestPipelineStage stage = RateLimitStage.rateByHeader("X-token").withRateLimitProvider(ALLOW_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat("Stage was not succeed", stageResult.isSuccess(), is(true));
	}

	@Test
	public void shouldAbortStageWhenTokenIsNotPresentRateLimitFails() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = RateLimitStage.rateByHeader("X-token").withRateLimitProvider(ALLOW_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat("Stage was not failed", stageResult.isFail(), is(true));
		assertThat("Fail response status code was not '403'", stageResult.getFail().getStatusLine().getStatusCode(), is(403));
		assertThat("Fail cause was wrong", stageResult.getFailCause().getMessage(), startsWith("Impossible to eval rate limit, can't "
				+ "extract key from"));
		assertThat("Fail body message was wrong", IOUTils.toString(stageResult.getFail().getMessageBody().get()), is("RATE LIMIT "
				+ "KEY CAN NOT BE EXTRACTED"));

	}

	@Test
	public void shouldAbortStageWhenRateLimitIsNotAllowed() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/", Headers.from("X-token", "XXX"));
		RequestPipelineStage stage = RateLimitStage.rateByHeader("X-token").withRateLimitProvider(NOT_ALLOW_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat("Stage was not failed", stageResult.isFail(), is(true));
		assertThat("Fail response status code was not '429'", stageResult.getFail().getStatusLine().getStatusCode(), is(429));
		assertThat("Fail cause was wrong", stageResult.getFailCause().getMessage(), is("Rate limit reached for key 'XXX'"));

	}

}