package com.dexmatech.styx.authentication;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.http.StatusLine;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.utils.IOUTils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.dexmatech.styx.authentication.AuthenticationStageDefaults.CUSTOM_HEADER_PREFIX;
import static com.dexmatech.styx.authentication.AuthenticationStageDefaults.DEFAULT_PERMISSIONS_HEADER_KEY;
import static java.util.Collections.emptyList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Created by aortiz on 15/09/16.
 */
public class TestAuthenticationStage {

	public static final AuthenticationProvider SUCCESS_AUTHENTICATION_PROVIDER = token ->
			completedFuture(Optional.of(new Principal(emptyList(), MetaInfo.empty())));

	public static final Function<Principal, AuthenticationProvider> SUCCESS_FROM = principal -> token ->
			completedFuture(Optional.of(principal));

	public static final AuthenticationProvider FAIL_AUTHENTICATION_PROVIDER = token -> completedFuture(Optional.empty());

	@Test
	public void shouldCompleteStageAuthenticating() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage.authenticationByToken("X-token").withAuthenticationProvider(SUCCESS_AUTHENTICATION_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
	}

	@Test
	public void shouldAbortStageWhenTokenIsNotPresentAuthenticationFails() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/");
		RequestPipelineStage stage = AuthenticationStage.authenticationByToken("X-token").withAuthenticationProvider(FAIL_AUTHENTICATION_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isFail(), is(true));
		assertThat(stageResult.getFail().getStatusLine().getStatusCode(), is(401));
		assertThat(stageResult.getFailCause().getMessage(), startsWith("Authentication fail, impossible extract token from "));

	}


	@Test
	public void shouldAbortStageWhenAuthenticationFails() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage.authenticationByToken("X-token").withAuthenticationProvider(FAIL_AUTHENTICATION_PROVIDER).build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isFail(), is(true));
		assertThat(stageResult.getFail().getStatusLine().getStatusCode(), is(401));
		assertThat(stageResult.getFailCause().getMessage(), startsWith("Authentication fail on "));

	}

	@Test
	public void shouldAbortStageWithACustomResponseWhenAuthenticationFails() throws Exception {
		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage
				.authenticationByToken("X-token")
				.withAuthenticationProvider(FAIL_AUTHENTICATION_PROVIDER)
				.whenAuthenticationFailsRespondWith(r-> HttpResponse.from(StatusLine.UNAUTHORIZED,Headers.empty(),new ByteArrayInputStream("AUTH FAILS".getBytes
						())))
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isFail(), is(true));
		assertThat(stageResult.getFail().getStatusLine().getStatusCode(), is(403));
		assertThat(stageResult.getFailCause().getMessage(), startsWith("Authentication fail on "));
		assertThat(stageResult.getFail().getMessageBody().isPresent(), is( true));
		assertThat(IOUTils.toString(stageResult.getFail().getMessageBody().get()), is( "AUTH FAILS"));
	}

	@Test
	public void shouldCompleteStageAddingPermissionsAndMetaInfoHeaders() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage
				.authenticationByToken("X-token")
				.withAuthenticationProvider(SUCCESS_FROM.apply(new Principal(
								Arrays.asList(Permission.of("users", "R"), Permission.of("users", "W")), MetaInfo.initWith("account", "3")
						))
				)
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().toMap().entrySet(), hasSize(3));
		assertThat(stageResult.getSuccess().getHeaders().contains(DEFAULT_PERMISSIONS_HEADER_KEY), is(true));
		assertThat(stageResult.getSuccess().getHeaders().get(DEFAULT_PERMISSIONS_HEADER_KEY), is("users:R,users:W"));
		assertThat(stageResult.getSuccess().getHeaders().contains(CUSTOM_HEADER_PREFIX + "account"), is(true));
	}



	@Test
	public void shouldCompleteStageAddingPermissionsHeadersWhenCustomPermissionHeadersGeneratorIsProvided() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage
				.authenticationByToken("X-token")
				.withAuthenticationProvider(SUCCESS_AUTHENTICATION_PROVIDER)
				.generatingPermissionHeadersWith(permissions -> Headers.from("X-custom-permission",""))
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().toMap().entrySet(), hasSize(2));
		assertThat(stageResult.getSuccess().getHeaders().contains("X-custom-permission"), is(true));

	}

	@Test
	public void shouldCompleteStageAddingMetaInfoHeadersWhenCustomMetaInfoHeadersGeneratorIsProvided() throws Exception {

		// given
		HttpRequest httpRequest = HttpRequest.get("/",Headers.from("X-token","XXX"));
		RequestPipelineStage stage = AuthenticationStage
				.authenticationByToken("X-token")
				.withAuthenticationProvider(SUCCESS_AUTHENTICATION_PROVIDER)
				.generatingMetaInfoHeadersWith(metaInfo-> Headers.from("X-meta",""))
				.build();
		// when
		StageResult<HttpRequest> stageResult = stage.apply(httpRequest).get();
		// then
		assertThat(stageResult.isSuccess(), is(true));
		assertThat(stageResult.getSuccess().getHeaders().toMap().entrySet(), hasSize(2));
		assertThat(stageResult.getSuccess().getHeaders().contains("X-meta"), is(true));

	}

}