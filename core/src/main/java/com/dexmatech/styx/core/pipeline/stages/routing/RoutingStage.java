package com.dexmatech.styx.core.pipeline.stages.routing;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.StageResult;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 9/08/16.
 */
@FunctionalInterface
public interface RoutingStage {

	CompletableFuture<StageResult<HttpResponse>> apply(HttpRequest httpRequest);
}
