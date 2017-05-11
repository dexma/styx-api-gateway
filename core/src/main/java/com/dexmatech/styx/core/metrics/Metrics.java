package com.dexmatech.styx.core.metrics;

import com.dexmatech.styx.core.http.HttpRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by aortiz on 28/04/17.
 */
@Getter
@AllArgsConstructor
public class Metrics {

	private List<RequestMetricCollector> collectors;
	private List<Consumer<List<Metric>>> reporters;
	private int windowSizeInSeconds;
	private Executor executor;

	public static Builder enable() {
		return new Builder();
	}

	@Getter
	@NoArgsConstructor
	public static class Builder {

		private List<RequestMetricCollector> collectors = new ArrayList<>();
		private List<Consumer<List<Metric>>> reporters = new ArrayList<>();
		private int windowSizeInSeconds = 0;
		private Executor executor = Executors.newFixedThreadPool(2);

		public Builder registerRequestMetric(RequestMetricCollector metricCollector) {
			this.collectors.add(metricCollector);
			return this;
		}

		public Builder registerRequestCounterMetric(String name, Function<HttpRequest, Integer> valueExtractor, int windowSizeInSeconds) {
			this.collectors.add(new MetricCounterCollector(name, valueExtractor));
			return this;
		}

		public Builder registerReporter(Consumer<List<Metric>> reporter) {
			reporters.add(reporter);
			return this;
		}

		public Builder withExecutor(Executor executor) {
			this.executor = executor;
			return this;
		}

		public Builder reportingWindow(int windowSizeInSeconds) {
			this.windowSizeInSeconds = windowSizeInSeconds;
			return this;
		}

		public Metrics build() {

			if(windowSizeInSeconds < 1 ) {
				throw new IllegalStateException("Please provide a reporting window greater than 1 second");
			}

			if (collectors.isEmpty()) {
				throw new IllegalStateException("Impossible register metrics with any metrics registered");
			}

			if (reporters.isEmpty()) {
				throw new IllegalStateException("Impossible report metrics with any reporters registered");
			}

			return new Metrics(collectors, reporters, executor);
		}

	}



	public void initCollectionAndReporting() {
		// TODO: on init https://docs.oracle.com/javase/7/docs/api/java/util/Timer.html
	}

	public void apply(HttpRequest request) {

		CompletableFuture.runAsync(
				() ->
				{
					// TODO collect on map of something
				}, executor);




	}
}
