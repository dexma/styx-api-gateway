package com.dexmatech.styx.core.metrics;

import java.time.ZonedDateTime;

/**
 * Created by aortiz on 28/04/17.
 */
public abstract class Metric<T> {
	private String name;
	private ZonedDateTime timestamp;
	private T value;
}
