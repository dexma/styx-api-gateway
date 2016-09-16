package com.dexmatech.styx.authentication;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.AbortedStage;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.dexmatech.styx.authentication.AuthenticationStageDefaults.*;
import static com.dexmatech.styx.core.http.HttpResponse.internalServerError;
import static com.dexmatech.styx.core.pipeline.stages.AbortedStage.because;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.completeStageFailingWith;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.stageFailWith;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.stageSuccessWith;

/**
 * Created by aortiz on 13/09/16.
 */
@Slf4j
public class AuthenticationStage {

	private static Function<String, Function<HttpRequest, Optional<String>>> BY_TOKEN =
			token -> httpRequest -> Optional.ofNullable(httpRequest.getHeaders().get(token));

	public static Builder authenticationByToken(String header) {
		return new Builder(BY_TOKEN.apply(header));
	}

	public static Builder authenticationBy(Function<HttpRequest, Optional<String>> authenticationIdExtractor) {
		return new Builder(authenticationIdExtractor);
	}

	public static class Builder {

		private AuthenticationProvider authenticationProvider;

		private Optional<String> permissionHeaderKey = Optional.empty();

		private Optional<Function<List<Permission>, Headers>> permissionsToHeaders = Optional.empty();

		private Optional<Function<MetaInfo, Headers>> metaInfoToHeaders = Optional.empty();

		private Optional<Function<HttpRequest, HttpResponse>> authenticationFailResponse = Optional.empty();

		private final Function<HttpRequest, Optional<String>> tokenExtractor;

		public Builder(Function<HttpRequest, Optional<String>> tokenExtractor) {
			this.tokenExtractor = tokenExtractor;
		}

		public Builder withAuthenticationProvider(AuthenticationProvider authenticationProvider) {
			this.authenticationProvider = authenticationProvider;
			return this;
		}

		public Builder whenAuthenticationFailsRespondWith(Function<HttpRequest, HttpResponse> responseGenerator) {
			this.authenticationFailResponse = Optional.ofNullable(responseGenerator);
			return this;
		}

		public Builder generatingPermissionHeadersWith(Function<List<Permission>, Headers> permissionToHeaders) {
			this.permissionsToHeaders = Optional.ofNullable(permissionToHeaders);
			return this;
		}

		public Builder generatingMetaInfoHeadersWith(Function<MetaInfo, Headers> metaToHeaders) {
			this.metaInfoToHeaders = Optional.ofNullable(metaToHeaders);
			return this;
		}

		public RequestPipelineStage build() {
			Objects.requireNonNull(authenticationProvider, "Please provide an authenticator");
			Function<List<Permission>, Headers> permissionHeadersGenerator =
					permissionsToHeaders.orElseGet(
							() -> PARSE_PERMISSIONS_TO_ONE_HEADER.apply(permissionHeaderKey.orElse
									(DEFAULT_PERMISSIONS_HEADER_KEY))
					);
			Function<MetaInfo, Headers> metaInfoHeadersGenerator = metaInfoToHeaders.orElseGet(() -> PARSE_METAINFO_TO_HEADERS);

			return httpRequest -> {
				Optional<String> token = tokenExtractor.apply(httpRequest);
				return token.map(t-> authenticationProvider.authenticate(t)
						.thenApply(principal ->
								principal.map(p -> {
									Headers permissionHeaders = permissionHeadersGenerator.apply(p.getPermissions());
									Headers metaInfoHeaders = metaInfoHeadersGenerator.apply(p.getMetaInfo());
									return stageSuccessWith(httpRequest.addHeaders(permissionHeaders).addHeaders(metaInfoHeaders));
								}).orElseGet(() -> {
									AbortedStage because = because(String.format("Authentication fail on '%s'", httpRequest));
									HttpResponse httpResponse = authenticationFailResponse
											.map(f -> f.apply(httpRequest))
											.orElseGet(() -> DEFAULT_AUTHENTICATION_FAIL_RESPONSE);
									return stageFailWith(httpResponse, because);
								})
						).exceptionally(throwable -> {
							log.debug("Aborting ROUTING stage => ", throwable);
							return stageFailWith(internalServerError(), throwable);
						})).orElseGet(() -> {
							AbortedStage because = because(String.format("Authentication fail, impossible extract token from '%s'",
									httpRequest));
							HttpResponse httpResponse = authenticationFailResponse
									.map(f -> f.apply(httpRequest))
									.orElseGet(() -> DEFAULT_AUTHENTICATION_FAIL_RESPONSE);
							return completeStageFailingWith(httpResponse, because);
						}
				);
			};

		}
	}
}
