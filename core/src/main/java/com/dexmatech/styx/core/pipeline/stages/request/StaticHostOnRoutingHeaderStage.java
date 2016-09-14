package com.dexmatech.styx.core.pipeline.stages.request;

import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;

import static com.dexmatech.styx.core.http.utils.Uris.changeHost;

/**
 * Created by aortiz on 14/09/16.
 */
public class StaticHostOnRoutingHeaderStage {

	public static RequestPipelineStage staticRoutingTo(String host) {
		return httpRequest -> {
			String routeValue = changeHost(httpRequest.getRequestLine().getUri(), host);
			return StageResult
					.completeStageSuccessfullyWith(httpRequest.addHeader(DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE, routeValue));
		};
	}

	public static RequestPipelineStage staticRoutingTo(String host, String resultingRouteHeader) {
		return httpRequest -> {
			String routeValue = changeHost(httpRequest.getRequestLine().getUri(), host);
			return StageResult.completeStageSuccessfullyWith(httpRequest.addHeader(resultingRouteHeader, routeValue));
		};
	}

}
