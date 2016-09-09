package com.dexmatech.styx.core;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.routable.IPipelineRequestRouter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 12/08/16.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ApiMultiPipeline extends ApiPipeline {

	private final IPipelineRequestRouter router;

	public CompletableFuture<HttpResponse> reply(final HttpRequest request) {
		return router.route(request).getPipeline().applyHttpReplyProtocol(request);
	}
}
