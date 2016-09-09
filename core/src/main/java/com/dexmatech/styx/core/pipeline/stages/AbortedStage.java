package com.dexmatech.styx.core.pipeline.stages;

import lombok.Getter;

/**
 * Created by aortiz on 7/09/16.
 */
@Getter
public class AbortedStage extends Exception {

	public AbortedStage(String message) {
		super(message);
	}
	public static AbortedStage because(String cause) {
		return new AbortedStage(cause);
	}

}
