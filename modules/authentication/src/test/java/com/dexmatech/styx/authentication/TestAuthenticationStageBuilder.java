package com.dexmatech.styx.authentication;

import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by aortiz on 15/09/16.
 */
public class TestAuthenticationStageBuilder {

	public static final AuthenticationProvider AUTHENTICATION_PROVIDER = token ->
			CompletableFuture.completedFuture(Optional.of(new Principal(Collections.emptyList(), MetaInfo.empty())));

	@Test
	public void shouldCreateAnStageWithDefaults() {
		// when
		RequestPipelineStage stage = AuthenticationStage.authenticationByToken("X-token").withAuthenticationProvider(AUTHENTICATION_PROVIDER).build();
		// then
		assertThat(stage, notNullValue());
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenAuthenticatorWasNotProvided() {
		// when
		AuthenticationStage.authenticationByToken("X-token").build();
	}

	@Test
	public void shouldCreateAnStageWithCustomConfig() {
		// when
		RequestPipelineStage stage = AuthenticationStage
				.authenticationByToken("X-token")
				.withAuthenticationProvider(AUTHENTICATION_PROVIDER)
				.whenAuthenticationFailsRespondWith(httpRequest -> null)
				.generatingMetaInfoHeadersWith(metaInfo -> null)
				.generatingPermissionHeadersWith(permissions -> null)
				.build();
		// then
		assertThat(stage, notNullValue());
	}

}