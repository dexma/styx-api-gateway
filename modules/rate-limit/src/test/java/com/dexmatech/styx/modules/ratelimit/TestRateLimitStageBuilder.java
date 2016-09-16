package com.dexmatech.styx.modules.ratelimit;

import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by aortiz on 16/09/16.
 */
public class TestRateLimitStageBuilder {

	@Test
	public void shouldCreateAnStageWithDefaults() {
		// when
		RequestPipelineStage stage = RateLimitStage.rateByHeader("X-token").withRateLimitProvider(key -> null).build();
		// then
		assertThat(stage, notNullValue());
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenAuthenticatorWasNotProvided() {
		// when
		RateLimitStage.rateByHeader("X-token").build();
	}

}