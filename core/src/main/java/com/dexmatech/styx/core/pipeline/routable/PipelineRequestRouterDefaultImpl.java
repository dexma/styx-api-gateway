package com.dexmatech.styx.core.pipeline.routable;

import com.dexmatech.styx.core.http.HttpRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dexmatech.styx.core.pipeline.routable.RoutablePipeline.TypeRoute.DEFAULT;

/**
 * Created by aortiz on 2/09/16.
 */
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PipelineRequestRouterDefaultImpl implements IPipelineRequestRouter {

	public static final IllegalArgumentException PROVIDE_A_DEFAULT_PIPELINE = new IllegalArgumentException(
			"Impossible create an http routable pipeline, you have to provide a default pipeline in order to handle all request");

	public static final IllegalArgumentException PROVIDE_ONLY_ONE_DEFAULT_PIPELINE = new IllegalArgumentException(
			"Impossible create an http routable pipeline, you have to provide only one default pipeline");

	private List<RoutablePipeline> pipelines;

	public static PipelineRequestRouterDefaultImpl from(List<RoutablePipeline> routablePipelines) {

		long numberOfDefaultPipelines = routablePipelines
				.stream()
				.filter(p -> p.getTypeRoute().equals(DEFAULT))
				.count();
		if (numberOfDefaultPipelines == 0) {
			throw PROVIDE_A_DEFAULT_PIPELINE;
		}
		if (numberOfDefaultPipelines > 1) {
			throw PROVIDE_ONLY_ONE_DEFAULT_PIPELINE;
		}

		List<RoutablePipeline> pipelinesWithDefaultAtTail = moveDefaultAtTail(routablePipelines);

		return new PipelineRequestRouterDefaultImpl(pipelinesWithDefaultAtTail);
	}

	public static List<RoutablePipeline> moveDefaultAtTail(List<RoutablePipeline> pipelines) {
		List<RoutablePipeline> pipelinesWithDefaultAtTail = new ArrayList<>();
		Optional<RoutablePipeline> tail = Optional.empty();
		for (RoutablePipeline pipeline : pipelines) {
			if (pipeline.getTypeRoute().equals(DEFAULT)) {
				tail = Optional.of(pipeline);
			} else {
				pipelinesWithDefaultAtTail.add(pipeline);
			}
		}
		tail.ifPresent(pipelinesWithDefaultAtTail::add);
		return pipelinesWithDefaultAtTail;
	}

	@Override public RoutablePipeline route(HttpRequest request) {

		RoutablePipeline selectedPipeline = pipelines.stream()
				.peek(r -> log.debug("Checking if pipeline '{}' can handle request", r.getTypeRoute())) // here put
				.filter(pipeline -> pipeline.canHandle(request))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
								String.format("Impossible to route '%s' to any pipeline, there is an setup error", request)
						)
				);
		log.debug("Pipeline '{}' is selected to handle request", selectedPipeline);
		return selectedPipeline;
	}
}
