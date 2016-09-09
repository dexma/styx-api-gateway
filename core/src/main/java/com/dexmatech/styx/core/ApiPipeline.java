package com.dexmatech.styx.core;

import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.HttpRequestReplyPipeline;
import com.dexmatech.styx.core.pipeline.routable.PipelineRequestRouterDefaultImpl;
import com.dexmatech.styx.core.pipeline.routable.RoutablePipeline;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by aortiz on 9/08/16.
 */
@Slf4j
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ApiPipeline {

	public static SinglePipelineBuilder singlePipeline() {
		return new SinglePipelineBuilder();
	}

	public static MultiPipelineBuilder multiPipeline() {
		return new MultiPipelineBuilder();
	}

	public static class SinglePipelineBuilder {

		private HttpRequestReplyPipeline pipeline;

		public SinglePipelineBuilder using(HttpRequestReplyPipeline pipeline) {
			this.pipeline = pipeline;
			return this;
		}

		public ApiPipeline build() {
			ApiSinglePipeline apiSinglePipeline = new ApiSinglePipeline(pipeline);
			log.info("[PIPELINE] Single pipeline handling all incoming requests was successfully created");
			return apiSinglePipeline;
		}
	}

	public static class MultiPipelineBuilder {

		private List<RoutablePipeline> routablePipelines;

		public MultiPipelineBuilder addPipeline(RoutablePipeline pipeline) {
			if (this.routablePipelines == null) {
				this.routablePipelines = new ArrayList<>();
			}
			this.routablePipelines.add(pipeline);
			return this;
		}

		public ApiPipeline build() {
			ApiMultiPipeline apiMultiPipeline = new ApiMultiPipeline(PipelineRequestRouterDefaultImpl.from(routablePipelines));
			routablePipelines.forEach(
					p -> log.info(String.format("[PIPELINE] '%s' all incoming requests was successfully created", p))
			);
			return apiMultiPipeline;
		}
	}

	public abstract CompletableFuture<HttpResponse> reply(final HttpRequest request);
}
