package io.cord3c.monitor.metrics.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;

import java.util.Map;


@NonNullApi
@NonNullFields
public class DropwizardAdapter implements MeterBinder {

	private final Iterable<Tag> tags;

	private MetricRegistry metricRegistry;

	public DropwizardAdapter(MetricRegistry metricRegistry) {
		this.tags = Tags.empty();
		this.metricRegistry = metricRegistry;
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		for (Map.Entry<String, com.codahale.metrics.Counter> entry : metricRegistry.getCounters().entrySet()) {
			Counter counter = entry.getValue();
			Gauge.builder("corda." + entry.getKey().toLowerCase(), counter, Counter::getCount)
					.tags(tags)
					.register(registry);
		}
	}
}
