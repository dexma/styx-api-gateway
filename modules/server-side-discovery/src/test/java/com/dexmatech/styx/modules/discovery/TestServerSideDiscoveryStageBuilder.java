package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by aortiz on 13/09/16.
 */
public class TestServerSideDiscoveryStageBuilder {

	@Test
	public void shouldCreateAnStage() {

		// when
		RequestPipelineStage stage = ServerSideDiscoveryStage
				.usingDefaults()
				.withBlackList(Arrays.asList())
				.whenBlackListMatchRespondWith(HttpResponse.notFound())
				.changingResultingCustomRoutingHeaderTo("")
				.withHostRoutingRule("/v3/.*", "apiv3.service.dmz")
				.withHostRoutingRule("/v2/.*", "apiv3.service.dmz")
				.withDefaultHostRule("apiv3.service.dmz")
				.build();

		// then
		assertThat(stage, notNullValue());
	}

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenNoDefaultHostRoutingRuleIsProvided() {
		// when
		ServerSideDiscoveryStage.usingDefaults().build();
	}

}