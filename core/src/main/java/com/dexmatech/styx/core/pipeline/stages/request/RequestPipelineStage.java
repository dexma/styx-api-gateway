package com.dexmatech.styx.core.pipeline.stages.request;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.pipeline.stages.PipelineStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 7/09/16.
 */
public interface RequestPipelineStage extends PipelineStage<HttpRequest> {
	CompletableFuture<StageResult<HttpRequest>> apply(HttpRequest httpMessage);
}