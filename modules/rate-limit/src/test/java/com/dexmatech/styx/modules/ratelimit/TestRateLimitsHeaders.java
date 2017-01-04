package com.dexmatech.styx.modules.ratelimit;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static com.dexmatech.styx.modules.ratelimit.RateLimitsHeaders.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 16/09/16.
 */
public class TestRateLimitsHeaders {

	@Test
	public void shouldParseRateLimitToHeaders() {
		// given
		RateLimitStatus status = new RateLimitStatus(
				true,
				Arrays.asList(
						new RateLimit("Hour", 1000, Duration.ofSeconds(10), 80),
						new RateLimit("Day", 2000, Duration.ofSeconds(20), 160)
				)
		);
		// when
		Headers headers = RateLimitsHeaders.asHeaders(status);
		// then
		assertThat("Headers size was not as expected", headers.size(), is(6));
		assertThat("Hourly rate limit header value was wrong", headers.get(HEADER_PREFIX + "Hour" + HEADER_LIMIT_SUFFIX), is("1000"));
		assertThat("Hourly rate limit header reset value was wrong", headers.get(HEADER_PREFIX + "Hour" + HEADER_RESET_SUFFIX), is("10"));
		assertThat("Hourly rate limit header remaining was wrong", headers.get(HEADER_PREFIX + "Hour" + HEADER_REMAINING_SUFFIX), is
				("80"));
		assertThat("Daily rate limit header value was wrong", headers.get(HEADER_PREFIX + "Day" + HEADER_LIMIT_SUFFIX), is("2000"));
		assertThat("Daily rate limit header reset was wrong", headers.get(HEADER_PREFIX + "Day" + HEADER_RESET_SUFFIX), is("20"));
		assertThat("Daily rate limit header remaining was wrong", headers.get(HEADER_PREFIX + "Day" + HEADER_REMAINING_SUFFIX), is("160"));
	}

	@Test
	public void shouldCreateAnEmptyHeadersWhenEmptyRateLimits() {
		// given
		RateLimitStatus status = new RateLimitStatus(true, Collections.emptyList());
		// when
		Headers headers = RateLimitsHeaders.asHeaders(status);
		// then
		assertThat("Headers were not empty",headers.size(), is(0));

	}


	@Test
	public void shouldCopyRateLimitHeadersFromRequestToResponse() {
		// given
		HttpRequest request = HttpRequest.get("/",Headers.from(HEADER_PREFIX + "Hour" + HEADER_LIMIT_SUFFIX, "1000"));
		HttpResponse response = HttpResponse.ok();
		// when
		HttpResponse finalResponse = COPY_RATELIMIT_HEADERS_TO_RESPONSE.apply(request, response);
		// then
		assertThat("Headers size was not as expected",finalResponse.getHeaders().size(), is(1));
		assertThat("Hourly rate limit header value was wrong",finalResponse.getHeaders().get(HEADER_PREFIX + "Hour" + HEADER_LIMIT_SUFFIX), is("1000"));
	}
}