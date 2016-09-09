package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.http.HttpRequest;

/**
 * Created by aortiz on 2/09/16.
 */
public interface IPipelineRequestRouter {
	RoutablePipeline route(HttpRequest request);
}
