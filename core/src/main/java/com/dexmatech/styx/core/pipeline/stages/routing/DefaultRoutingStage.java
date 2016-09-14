package com.dexmatech.styx.core.pipeline.stages.routing;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.http.client.HttpMappers;
import com.dexmatech.styx.core.pipeline.stages.AbortedStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by aortiz on 9/08/16.
 */
@Slf4j
public class DefaultRoutingStage {

	public static final String HTTP_1_1 = "HTTP/1.1";

	// TODO: this var must be readed from constants
	public static final String DEFAULT_HEADER_USED_TO_ROUTE = "X-routing-url";

	public static final int REQUEST_TIMEOUT_IN_MS = 5000;

	private static final Function<String, Function<HttpRequest, Optional<String>>> DEFAULT_ROUTE_EXTRACTOR_FROM_HEADER =
			header -> request -> Optional.ofNullable(
					(request.getHeaders().contains(header)?request.getHeaders().get(header):request.getHeaders().get(header.toLowerCase()))
			);

	public static final Supplier<AsyncHttpClientConfig> SUPPLY_DEFAULT_HTTP_CLIENT_CONFIG =
			() -> new DefaultAsyncHttpClientConfig.Builder()
					.setFollowRedirect(true)
					.setRequestTimeout(REQUEST_TIMEOUT_IN_MS)
					.build();

	public static Builder usingDefaults() {
		return new Builder();
	}

	public static class Builder {
		private Optional<AsyncHttpClientConfig> config = Optional.empty();
		private Optional<AsyncHttpClient> client = Optional.empty();
		private Optional<Function<HttpRequest, Optional<String>>> routeExtractor = Optional.empty();
		private Optional<String> headerUsedToRoute = Optional.empty();
		private Optional<BiFunction<HttpRequest, HttpResponse, HttpResponse>> transformationAfterRouting = Optional.empty();
		private String extractingRouteFrom = "extract route from custom function";

		public Builder usingDefaultClientwithConfig(AsyncHttpClientConfig config) {
			this.config = Optional.of(config);
			return this;
		}

		public Builder withConfiguredClient(AsyncHttpClient client) {
			this.client = Optional.of(client);
			return this;
		}

		public Builder applyAfterRoutingSuccess(BiFunction<HttpRequest, HttpResponse, HttpResponse> transformationAfterRouting) {
			this.transformationAfterRouting = Optional.ofNullable(transformationAfterRouting);
			return this;
		}

		public Builder usingStrategyToRoute(Function<HttpRequest, Optional<String>> routeExtractor) {
			this.routeExtractor = Optional.of(routeExtractor);
			return this;
		}

		public Builder usingHeaderToRoute(String headerKey) {
			this.headerUsedToRoute = Optional.of(headerKey);
			return this;
		}

		public RoutingStage build() {

			AsyncHttpClient client = this.client.orElse(new DefaultAsyncHttpClient(config.orElseGet(SUPPLY_DEFAULT_HTTP_CLIENT_CONFIG)));

			Function<HttpRequest, Optional<String>> extractRouteFromRequest = routeExtractor.orElseGet(() -> {
				String header = headerUsedToRoute.orElse(DEFAULT_HEADER_USED_TO_ROUTE);
				extractingRouteFrom = String.format("extract route from header '%s'", header);
				return DEFAULT_ROUTE_EXTRACTOR_FROM_HEADER.apply(header);
			});

			return httpRequest -> {

				Optional<String> urlToRedirect = extractRouteFromRequest.apply(httpRequest);

				Function<Response, HttpResponse> responseToResponseMapper = HttpMappers
						.generateResponseMapperFromHttpVersion(httpRequest.getRequestLine().getHttpVersion());

				return urlToRedirect.map(
						url ->
								client.executeRequest(HttpMappers.asClientRequest(url, httpRequest))
										.toCompletableFuture()
										.thenApply(responseToResponseMapper)
										.thenApply(response ->
												transformationAfterRouting.map(f -> f.apply(httpRequest, response)).orElse(response)
										)
										.thenApply(StageResult::stageSuccessWith)
										.exceptionally(throwable -> {
											log.debug("Aborting ROUTING stage => ", throwable);
											return StageResult.stageFailWith(HttpResponse.internalServerError(), throwable);
										})
				).orElseGet(() -> {
							AbortedStage because = AbortedStage.because(String.format("Impossible %s and '%s'", extractingRouteFrom, httpRequest));
							log.debug("Aborting ROUTING stage => ", because);
							return StageResult.completeStageFailingWith(HttpResponse.internalServerError(), because);
						}
				);
			};

		}

	}

}
