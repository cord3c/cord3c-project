package io.cord3c.monitor.metrics.internal;

import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jpa.HibernateMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.internal.cordapp.CordappImpl;
import net.corda.core.node.AppServiceHub;
import net.corda.node.internal.cordapp.CordappProviderInternal;
import net.corda.node.services.api.ServiceHubInternal;
import org.hibernate.SessionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class MicrometerDaemonThread extends Thread {

	private final AppServiceHub appServiceHub;

	public MicrometerDaemonThread(AppServiceHub appServiceHub) {
		this.setDaemon(true);
		this.appServiceHub = appServiceHub;
	}

	private Object getInternalServiceHub(AppServiceHub appServiceHub) {
		try {
			Field serviceHub = appServiceHub.getClass().getDeclaredField("serviceHub");
			serviceHub.setAccessible(true);
			return serviceHub.get(appServiceHub);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private List<CordappImpl> getCordapps(AppServiceHub appServiceHub) {
		try {
			ServiceHubInternal serviceHubInternal = (ServiceHubInternal) getInternalServiceHub(appServiceHub);
			CordappProviderInternal cordappProvider = serviceHubInternal.getCordappProvider();
			List<CordappImpl> cordapps = cordappProvider.getCordapps();
			return cordapps;
		} catch (Exception e) {
			log.error("Unable to get deployed cordapps from CordappProvider.");
			throw new IllegalStateException(e);
		}
	}

	private MetricRegistry getMetricRegistry(AppServiceHub appServiceHub) {
		try {
			Object serviceHubInternal = getInternalServiceHub(appServiceHub);
			Object monitoringService = serviceHubInternal.getClass()
					.getMethod("getMonitoringService")
					.invoke(serviceHubInternal);
			Field field = monitoringService.getClass().getDeclaredField("metrics");
			field.setAccessible(true);
			return (MetricRegistry) field.get(monitoringService);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private SessionFactory getSessionFactory(AppServiceHub appServiceHub) {
		try {
			Object serviceHubInternal = getInternalServiceHub(appServiceHub);
			Object database = serviceHubInternal.getClass().getMethod("getDatabase").invoke(serviceHubInternal);

			Method field = database.getClass().getMethod("getEntityManagerFactory");
			field.setAccessible(true);
			Object hibernateConfig = field.invoke(database);
			return (SessionFactory) hibernateConfig;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void run() {
		log.info("Daemon micrometer thread started !");
		MeterRegistry registry = MetricsServlet.getRegistry();

		Counter.builder("cordapp")
				.description("number of loaded cordapps")
				.register(registry)
				.increment(getCordapps(appServiceHub).size());
		new JvmMemoryMetrics().bindTo(registry);
		new JvmThreadMetrics().bindTo(registry);
		new JvmGcMetrics().bindTo(registry);
		new JvmGcMetrics().bindTo(registry);
		new ProcessorMetrics().bindTo(registry);
		FlowMetricsImpl.INSTANCE.bindTo(registry);
		new UptimeMetrics().bindTo(registry);
		new FileDescriptorMetrics().bindTo(registry);
		new Log4j2Metrics().bindTo(registry);

		// following metrics result in NaN that cannot be parsed by metric-beat, maybe of use in the future:
		// new DiskSpaceMetrics(new File(System.getProperty("user.dir"))).bindTo(registry);

		new DropwizardAdapter(getMetricRegistry(appServiceHub)).bindTo(registry);

		SessionFactory sessionFactory = getSessionFactory(appServiceHub);
		sessionFactory.getStatistics().setStatisticsEnabled(true);
		new HibernateMetrics(sessionFactory, "hibernate", Tags.empty()).bindTo(registry);
	}
}
