package com.dexmatech.styx.modules.grizzly;

import com.dexmatech.styx.core.ApiSinglePipeline;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;

/**
 * Created by aortiz on 13/09/16.
 */
public class Runner {
	public static void main(String[] args) {
		ApiGateway.runningOverGrizzly().withDefaultServerRunningOnPort(8080).withPipeline(ApiSinglePipeline.singlePipeline().using
				(HttpRequestReplyPipeline.pipeline()
						.applyingStaticHostOnRouteGeneration("api.pre.dexcell.com")
				.applyingDefaultRoutingStage()
				.build()
		).build()).build()
				.startAndKeepRunning();
	}
}
