package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.extractors.HostExtractor;
import com.dexmatech.styx.core.http.extractors.PathExtractor;
import com.dexmatech.styx.core.http.utils.HostValidators;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.dexmatech.styx.core.pipeline.routable.RoutablePipeline.TypeRoute.*;

/**
 * Created by aortiz on 2/09/16.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RoutablePipeline {

	public enum TypeRoute {DEFAULT, MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN, MATCHING_BY_HOST, MATCHING_BY_PATH_REGEX_PATTERN}

	final private TypeRoute typeRoute;
	final private HttpRequestReplyPipeline pipeline;

	public static Builder matchingRequestsBy(String host, String pathPattern) {
		return new Builder(TypeRoute.MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN, Optional.of(host), Optional.of(pathPattern));
	}

	public static Builder matchingRequestsByHost(String host) {
		return new Builder(MATCHING_BY_HOST, Optional.of(host), Optional.empty());
	}

	public static Builder matchingRequestsByPathRegexPattern(String pathPattern) {
		return new Builder(MATCHING_BY_PATH_REGEX_PATTERN, Optional.empty(), Optional.of(pathPattern));
	}

	public static Builder defaultPipeline() {
		return new Builder(TypeRoute.DEFAULT, Optional.empty(), Optional.empty());
	}

	public static class Builder {

		final private TypeRoute typeRoute;
		final private Optional<String> host;
		final private Optional<String> pathPattern;
		private HttpRequestReplyPipeline pipeline;

		private Builder(TypeRoute typeRoute, Optional<String> host, Optional<String> pathPattern) {
			this.typeRoute = typeRoute;
			this.host = host;
			this.pathPattern = pathPattern;
		}

		public Builder using(HttpRequestReplyPipeline pipeline) {
			this.pipeline = pipeline;
			return this;
		}

		public RoutablePipeline build() {
			Objects.requireNonNull(pipeline, "Pipeline can not be empty to construct a routable http reply pipeline");
			if (typeRoute.equals(MATCHING_BY_HOST) || typeRoute.equals(TypeRoute.MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN)) {
				host.ifPresent(h -> {
							if (!HostValidators.HOST_VALIDATOR.test(h)) {
								throw new IllegalArgumentException(
										"Impossible create a routable pipeline, host provided is malformed => " + h
								);
							}
						}
				);
			}
			switch (this.typeRoute) {
			case MATCHING_BY_HOST:
				return new RoutablePipelineByHost(host.get(), pipeline);
			case MATCHING_BY_PATH_REGEX_PATTERN:
				return new RoutablePipelineByRegexPath(pathPattern.map(Pattern::compile).get(), pipeline);
			case MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN:
				return new RoutablePipelineByHostAndRegexPath(host.get(), pathPattern.map(Pattern::compile).get(), pipeline);
			default:
				return new RoutablePipelineByDefault(pipeline);
			}

		}
	}

	public abstract boolean canHandle(HttpRequest request);

	@Getter
	public static class RoutablePipelineByHost extends RoutablePipeline {

		final private String host;

		private RoutablePipelineByHost(String host, HttpRequestReplyPipeline pipeline) {
			super(MATCHING_BY_HOST, pipeline);
			this.host = host;
		}

		@Override public boolean canHandle(HttpRequest request) {
			Optional<String> requestedHost = HostExtractor.INSTANCE.extract(request);
			return requestedHost.map(host::equals).orElse(false);
		}

		@Override
		public String toString() {
			return String.format("Type : %s with Host : %s", this.getTypeRoute(), this.host);
		}

	}

	@Getter
	public static class RoutablePipelineByRegexPath extends RoutablePipeline {

		final private Pattern pathRegexPattern;

		private RoutablePipelineByRegexPath(Pattern pathRegexPattern, HttpRequestReplyPipeline pipeline) {
			super(MATCHING_BY_PATH_REGEX_PATTERN, pipeline);
			this.pathRegexPattern = pathRegexPattern;
		}

		@Override public boolean canHandle(HttpRequest request) {
			Optional<String> requestedPath = PathExtractor.INSTANCE.extract(request);
			return requestedPath.map(rp -> pathRegexPattern.matcher(rp).matches()).orElse(false);

		}

		@Override
		public String toString() {
			return String.format("Type : %s with Regex path : %s", this.getTypeRoute(), this.pathRegexPattern.pattern());
		}

	}

	@Getter
	public static class RoutablePipelineByHostAndRegexPath extends RoutablePipeline {
		final private String host;

		final private Pattern pathRegexPattern;

		private RoutablePipelineByHostAndRegexPath(String host, Pattern pathRegexPattern, HttpRequestReplyPipeline pipeline) {
			super(MATCHING_BY_HOST_AND_PATH_REGEX_PATTERN, pipeline);
			this.host = host;
			this.pathRegexPattern = pathRegexPattern;
		}

		@Override public boolean canHandle(HttpRequest request) {
			Optional<String> requestedHost = HostExtractor.INSTANCE.extract(request);
			Optional<String> requestedPath = PathExtractor.INSTANCE.extract(request);
			return requestedHost.map(host::equals).orElse(false) && requestedPath.map(rp -> pathRegexPattern.matcher(rp).matches())
					.orElse(false);
		}

		@Override
		public String toString() {
			return String.format(
					"Type : '%s' with Host : '%s' and Regex path : '%s'", this.getTypeRoute(), this.host, this.pathRegexPattern.pattern()
			);
		}

	}

	@Getter
	public static class RoutablePipelineByDefault extends RoutablePipeline {

		private RoutablePipelineByDefault(HttpRequestReplyPipeline pipeline) {
			super(DEFAULT, pipeline);
		}

		@Override public boolean canHandle(HttpRequest request) {
			return true;
		}

		@Override
		public String toString() {
			return String.format("Type : '%s' ", this.getTypeRoute());
		}

	}
}



