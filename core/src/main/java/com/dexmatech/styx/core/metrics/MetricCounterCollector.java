package com.dexmatech.styx.core.metrics;

import com.dexmatech.styx.core.http.HttpRequest;

import java.util.function.Function;

/**
 * Created by aortiz on 28/04/17.
 */
public class MetricCounterCollector extends RequestMetricCollector<Integer> {
	public MetricCounterCollector(String name, Function<HttpRequest, Integer> valueExtractor) {
		super(name, valueExtractor);
	}
}
