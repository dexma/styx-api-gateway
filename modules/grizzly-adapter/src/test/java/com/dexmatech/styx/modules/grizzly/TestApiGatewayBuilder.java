package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.ApiPipeline;
import org.junit.Test;

import java.util.concurrent.Executors;

import static com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline.pipeline;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestApiGatewayBuilder {

	@Test(expected = NullPointerException.class)
	public void shouldFailWhenPipelineIsNotProvided() {
		// when
		ApiGateway.runningOverGrizzly().build();
	}

	@Test
	public void shouldCreateSimplestGateway(){
		// given
		ApiPipeline apiPipeline = ApiPipeline
				.singlePipeline()
				.using(pipeline().applyingDefaultRoutingStage().build())
				.build();
		// when
		ApiGateway apiGateway = ApiGateway.runningOverGrizzly().withPipeline(apiPipeline).build();

		// Then
		assertThat(apiGateway, notNullValue());
	}

	@Test
	public void shouldCreateGatewayChangingDefaults(){
		// given
		ApiPipeline apiPipeline = ApiPipeline
				.singlePipeline()
				.using(pipeline().applyingDefaultRoutingStage().build())
				.build();
		// when
		ApiGateway apiGateway = ApiGateway
				.runningOverGrizzly()
				.withDefaultServerRunningOnPort(8081)
				.withExecutorService(Executors.newFixedThreadPool(4))
				.withPipeline(apiPipeline)
				.build();


		// Then
		assertThat(apiGateway, notNullValue());
	}

}