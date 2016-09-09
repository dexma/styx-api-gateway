package com.dexmatech.styx.core.pipeline.stages;

import com.dexmatech.styx.core.http.HttpMessage;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 9/08/16.
 */
@FunctionalInterface
public interface PipelineStage<T extends HttpMessage> {

	CompletableFuture<StageResult<T>> apply(T httpMessage);

}
