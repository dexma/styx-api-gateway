package com.dexmatech.styx.core.pipeline.stages.routing;

import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by aortiz on 8/09/16.
 */
public class TestDefaultRoutingStageBuilder {

	@Test
	public void shouldCreateAnDeFaultInstance() {
		// when
		RoutingStage stage = DefaultRoutingStage.usingDefaults().build();
		//then
		assertThat(stage,notNullValue());
	}

}