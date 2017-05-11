package com.dexmatech.styx.core.metrics;

import com.dexmatech.styx.core.http.HttpRequest;
import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * Created by aortiz on 28/04/17.
 */
@AllArgsConstructor
public abstract class RequestMetricCollector<T> {
	private String metricName;
	private Function<HttpRequest, T> valueExtractor;
}
