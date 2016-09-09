package com.dexmatech.styx.core.pipeline.stages.response;

import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.PipelineStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 7/09/16.
 */
public interface ResponsePipelineStage extends PipelineStage<HttpResponse> {
	CompletableFuture<StageResult<HttpResponse>> apply(HttpResponse httpMessage);
}
