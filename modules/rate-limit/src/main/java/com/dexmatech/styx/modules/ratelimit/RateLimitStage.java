package com.dexmatech.styx.modules.ratelimit;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.AbortedStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.dexmatech.styx.core.http.HttpResponse.*;
import static com.dexmatech.styx.core.pipeline.stages.AbortedStage.because;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.*;

/**
 * Created by aortiz on 13/09/16.
 */
@Slf4j
public class RateLimitStage {

	public static final HttpResponse RESPONSE_WHEN_NO_KEY = unauthorized("RATE LIMIT KEY CAN NOT BE EXTRACTED".getBytes());
	private static Function<String, Function<HttpRequest, Optional<String>>> BY_TOKEN =
			token -> httpRequest -> Optional.ofNullable(httpRequest.getHeaders().get(token));

	public static Builder rateByHeader(String header) {
		return new Builder(BY_TOKEN.apply(header));
	}

	public static Builder rateBy(Function<HttpRequest, Optional<String>> rateKeyExtractor) {
		return new Builder(rateKeyExtractor);
	}

	public static class Builder {

		private RateLimitProvider rateLimitProvider;

		private final Function<HttpRequest, Optional<String>> keyExtractor;

		public Builder(Function<HttpRequest, Optional<String>> tokenExtractor) {
			this.keyExtractor = tokenExtractor;
		}

		public Builder withRateLimitProvider(RateLimitProvider rateLimitProvider) {
			this.rateLimitProvider = rateLimitProvider;
			return this;
		}

		public RequestPipelineStage build() {
			Objects.requireNonNull(rateLimitProvider, "Please provide a rate limit provider");

			return httpRequest -> {
				Optional<String> key = keyExtractor.apply(httpRequest);
				return key.map(t -> rateLimitProvider.apply(t)
						.thenApply(rateLimitStatus -> {
									Headers headers = RateLimitsHeaders.asHeaders(rateLimitStatus);
									StageResult<HttpRequest> result;
									if (rateLimitStatus.isAllowed()) {
										result = stageSuccessWith(httpRequest.addHeaders(headers));
									} else {
										AbortedStage cause = because(String.format("Rate limit reached for key '%s'", key.get()));
										result = stageFailWith(toManyRequests().addHeaders(headers), cause);
									}
									return result;
								}

						).exceptionally(throwable -> {
							log.debug("Aborting ROUTING stage => ", throwable);
							return stageFailWith(internalServerError(), throwable);
						})).orElseGet(() -> {
							AbortedStage because = because(
									String.format("Impossible to eval rate limit, can't extract key from '%s'", httpRequest)
							);
							return completeStageFailingWith(RESPONSE_WHEN_NO_KEY, because);
						}
				);
			};

		}

	}
}
