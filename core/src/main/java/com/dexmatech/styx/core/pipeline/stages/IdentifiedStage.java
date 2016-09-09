package com.dexmatech.styx.core.pipeline.stages;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by aortiz on 6/09/16.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class IdentifiedStage<T extends PipelineStage> {
	private String id;
	private T stage;
}