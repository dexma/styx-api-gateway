package com.dexmatech.styx.core.pipeline;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.IdentifiedStage;
import com.dexmatech.styx.core.pipeline.stages.PipelineStage;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.pipeline.stages.request.StaticHostOnRoutingHeaderStage;
import com.dexmatech.styx.core.pipeline.stages.response.ResponsePipelineStage;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;
import com.dexmatech.styx.core.pipeline.stages.routing.RoutingStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import static com.dexmatech.styx.core.pipeline.HttpPipelines.applyPipelineStages;

/**
 * Created by aortiz on 9/08/16.
 */

//TODO:executors
@Slf4j
@Getter
@AllArgsConstructor
public class HttpRequestReplyPipeline {
	private List<IdentifiedStage<PipelineStage<HttpRequest>>> requestPipelineStages;
	private RoutingStage routingStage;
	private List<IdentifiedStage<PipelineStage<HttpResponse>>> responsePipelineStages;
	private Optional<BiConsumer<HttpRequest, Throwable>> nonExpectedErrorsConsumer = Optional.empty();
	private ExecutorService executorService;

	private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	public static Builder pipeline() {
		return new Builder();
	}

	public static class Builder {
		private List<IdentifiedStage<PipelineStage<HttpRequest>>> requestPipelineStages = new ArrayList<>();
		private RoutingStage routingStage = null;
		private List<IdentifiedStage<PipelineStage<HttpResponse>>> responsePipelineStages = new ArrayList<>();
		private Optional<BiConsumer<HttpRequest, Throwable>> nonExpectedErrorsConsumer = Optional.empty();
		private Optional<ExecutorService> executorService = Optional.empty();

		public Builder applyingPreRoutingStage(String id, RequestPipelineStage stage) {
			requestPipelineStages.add(new IdentifiedStage<>(id, stage));
			return this;
		}

		public Builder applyingPostRoutingStage(String id, ResponsePipelineStage stage) {
			responsePipelineStages.add(new IdentifiedStage<>(id, stage));
			return this;
		}

		//		public Builder applyingPreRoutingStages(List<RequestPipelineStage> stages) {
		//			stages.forEach(requestPipelineStages::add);
		//			return this;
		//		}

		public Builder applyingStaticHostOnRouteGeneration(String host) {
			this.applyingPreRoutingStage("static-routing", StaticHostOnRoutingHeaderStage.staticRoutingTo(host));
			return this;
		}

		public Builder applyingDefaultRoutingStage() {
			this.routingStage = DefaultRoutingStage.usingDefaults().build();
			return this;
		}

		public Builder applyingRoutingStage(RoutingStage routingStage) {
			this.routingStage = routingStage;
			return this;
		}

		//		public Builder applyingPostRoutingStages(List<ResponsePipelineStage> stages) {
		//			stages.forEach(responsePipelineStages::add);
		//			return this;
		//		}

		public Builder handlingNonExpectedErrorsWith(BiConsumer<HttpRequest, Throwable> nonExpectedErrorsConsumer) {
			this.nonExpectedErrorsConsumer = Optional.ofNullable(nonExpectedErrorsConsumer);
			return this;
		}

		public Builder usingThreadExecutor(ExecutorService executor) {
			this.executorService = Optional.of(executor);
			return this;
		}

		public HttpRequestReplyPipeline build() {
			Objects.requireNonNull(routingStage, "Routing stage can not be empty to construct an http reply pipeline");
			return new HttpRequestReplyPipeline(
					requestPipelineStages,
					routingStage,
					responsePipelineStages,
					nonExpectedErrorsConsumer,
					executorService.orElse(DEFAULT_EXECUTOR_SERVICE)
			);
		}
	}

	public CompletableFuture<HttpResponse> applyHttpReplyProtocol(final HttpRequest request) {

		log.debug("[PIPELINE] Entering to pipeline with request => '{}'", request);

		CompletableFuture<StageResult<HttpRequest>> resultOfApplyRequestPipeline = applyPipelineStages(
				request, requestPipelineStages, executorService
		);

		CompletableFuture<StageResult<HttpResponse>> resultOfApplyRoutingStage = resultOfApplyRequestPipeline
				.thenComposeAsync(
						stageRequest -> stageRequest.fold(
								success -> {
									log.debug("[STAGE -> 'routing'] Proceeding to apply with => '{}'", success);
									return routingStage.apply(success);
								},
								(fail,cause) -> CompletableFuture.completedFuture(StageResult.stageFailWith(fail,cause))
						), executorService);

		CompletableFuture<StageResult<HttpResponse>> resultOfApplyResponsePipeline = resultOfApplyRoutingStage.thenComposeAsync(
				stageRouting -> stageRouting.fold(
						success -> {
							log.debug("[STAGE -> 'routing'] Completed successfully with => '{}'", success);
							return applyPipelineStages(success, responsePipelineStages, executorService);
						},
						(fail,cause) -> {
							log.debug("[STAGE -> 'routing'] Failing with => '{}'", fail);
							return CompletableFuture.completedFuture(StageResult.stageFailWith(fail,cause));
						}
				)
				, executorService);

		return resultOfApplyResponsePipeline
				.thenApply(stageResponse -> stageResponse.fold(
						success -> {
							log.debug("[PIPELINE] Coming out from pipeline with response => '{}'", success);
							return success;
						},
						(fail,cause) -> {
							log.error("[STAGE ABORTED] Exceptionally pipeline interrupted by ", cause);
							nonExpectedErrorsConsumer.ifPresent(
									consumer -> consumer.accept(request, cause)
							);
							return fail;
						}))
				.exceptionally(throwable -> {
					log.error("[GENERAL BROKEN PIPELINE CAPTOR] Exceptionally pipeline interrupted by ", throwable);
					nonExpectedErrorsConsumer.ifPresent(
							consumer -> consumer.accept(request, throwable)
					);
					return HttpResponse.internalServerError();
				});

	}

}
