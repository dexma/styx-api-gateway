package com.dexmatech.styx.modules.ratelimit;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;

/**
 * Created by aortiz on 16/09/16.
 */
public class RateLimitsHeaders {

	public static final String HEADER_PREFIX = "X-Ratelimit-";
	public static final String HEADER_LIMIT_SUFFIX = "-Limit";
	public static final String HEADER_RESET_SUFFIX = "-Reset";
	public static final String HEADER_REMAINING_SUFFIX = "-Remaining";

	public static Headers asHeaders(RateLimitStatus rateLimitStatus) {
		return rateLimitStatus.getCurrentRateLimits().stream().map(rateLimit ->
				Headers.from(HEADER_PREFIX + rateLimit.getTypePrefix() + HEADER_LIMIT_SUFFIX, valueOf(rateLimit.getLimit()))
						.put(HEADER_PREFIX + rateLimit.getTypePrefix() + HEADER_RESET_SUFFIX,
								valueOf(rateLimit.getReset().abs().getSeconds()))
						.put(HEADER_PREFIX + rateLimit.getTypePrefix() + HEADER_REMAINING_SUFFIX, valueOf(rateLimit.getRemaining()))
		).collect(Collectors.reducing(Headers.empty(), Headers::merge));
	}

	public static BiFunction<HttpRequest, HttpResponse, HttpResponse> COPY_RATELIMIT_HEADERS_TO_RESPONSE
			= (httpRequest, httpResponse) -> {
		Map<String, String> rateHeadersFromRequest = httpRequest.getHeaders()
				.toMap()
				.entrySet()
				.stream()
				.filter(e -> e.getKey().contains(HEADER_PREFIX))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return httpResponse.addHeaders(Headers.from(rateHeadersFromRequest));
	};

}
