package io.cord3c.monitor.metrics.internal;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * Accessed dynamically by FlowMetrics class.
 */
public class FlowMetricsImpl implements MeterBinder {

	public static final String PROGRESS_COUNTER_PREFIX = "cordapp.progress.";

	public static final String RECORDING_COUNTER_NAME = "cordapp.recordings";

	public static FlowMetricsImpl INSTANCE = new FlowMetricsImpl();

	private FlowMetricsImpl() {
		// only a singleton
	}

	private MeterRegistry registry;

	private Counter recordings;

	private ConcurrentHashMap<String, Counter> progressCounters = new ConcurrentHashMap();

	@Override
	public void bindTo(MeterRegistry registry) {
		this.registry = registry;
		this.recordings = registry.counter(RECORDING_COUNTER_NAME);
	}

	public void incrementProgress(String name) {
		verifyInitialized();
		String key = PROGRESS_COUNTER_PREFIX + name.toLowerCase();
		Counter counter = progressCounters.computeIfAbsent(key, (it) -> registry.counter(it));
		counter.increment();
	}

	public void incrementBulkRecording(int count) {
		verifyInitialized();
		recordings.increment(count);
	}

	private void verifyInitialized() {
		Objects.requireNonNull(registry);
	}

}
