package com.dexmatech.styx.core.pipeline;

import com.dexmatech.styx.core.http.HttpMessage;
import com.dexmatech.styx.core.pipeline.stages.IdentifiedStage;
import com.dexmatech.styx.core.pipeline.stages.PipelineStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.dexmatech.styx.core.utils.ArrayUtils.head;
import static com.dexmatech.styx.core.utils.ArrayUtils.tail;

/**
 * Created by aortiz on 11/08/16.
 */
@Slf4j
public class HttpPipelines {
	public static <T extends HttpMessage, P extends PipelineStage<T>> CompletableFuture<StageResult<T>> applyPipelineStages(T httpMessage,
			List<IdentifiedStage<P>> stagesToApply, ExecutorService executorService) {
		if (stagesToApply.isEmpty()) {
			return CompletableFuture.completedFuture(StageResult.stageSuccessWith(httpMessage));
		} else {
			IdentifiedStage<P> head = head(stagesToApply);
			log.debug("[STAGE -> '{}'] Proceeding to apply with => '{}'", head.getId(), httpMessage);
			return head.getStage().apply(httpMessage).thenComposeAsync(stageResult -> stageResult.fold(
					success -> {
						log.debug("[STAGE -> '{}'] Completed successfully with => '{}'", head.getId(), success);
						return applyPipelineStages(success, tail(stagesToApply), executorService);
					},
					(fail, cause) -> {
						log.debug("[STAGE -> '{}'] Failing with => '{}' cause ", head.getId(), fail, cause);
						return CompletableFuture.completedFuture(StageResult.stageFailWith(fail, cause));
					}
			), executorService);
		}
	}
}
