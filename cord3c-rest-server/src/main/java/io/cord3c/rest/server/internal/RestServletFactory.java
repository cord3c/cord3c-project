package io.cord3c.rest.server.internal;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;

import com.google.auto.service.AutoService;
import io.cord3c.rest.api.types.CordaTypesModule;
import io.cord3c.rest.server.RestConfigurer;
import io.cord3c.rest.server.RestContext;
import io.cord3c.server.http.HttpServletFactory;
import io.cord3c.ssi.api.internal.PropertyUtils;
import io.cord3c.ssi.corda.internal.party.PartyToDIDMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.Module;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.home.HomeModule;
import io.crnk.servlet.CrnkServlet;
import kotlin.jvm.functions.Function1;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.vault.SessionScope;


@AutoService(HttpServletFactory.class)
public class RestServletFactory implements HttpServletFactory {

	@Getter
	@Setter
	private static String networkMapUrl;


	@Override
	public String getPattern() {
		return PropertyUtils.getProperty("cord3c.rest.contextPath", "/api/") + "*";
	}

	@Override
	public HttpServlet getImplementation(AppServiceHub serviceHub) {
		return new CordaCrnkServlet(serviceHub);
	}


	@RequiredArgsConstructor
	public class CordaCrnkServlet extends CrnkServlet {

		private final AppServiceHub serviceHub;

		private CordaMapper cordaMapper = new CordaMapperImpl();

		private ThreadLocal<EntityManager> emLocal = new ThreadLocal<>();

		@Override
		protected void initCrnk(CrnkBoot boot) {
			if (networkMapUrl == null) {
				networkMapUrl = PropertyUtils.getProperty("cord3c.networkmap.url", null);
			}

			if (networkMapUrl == null) {
				throw new IllegalStateException(
						"please configure CORD3C_SSI_NETWORKMAP_HOST or set manually on RestServletFactory");
			}

			PartyToDIDMapper didMapper = new PartyToDIDMapper();
			didMapper.setNetworkMapUrl(networkMapUrl);
			cordaMapper.setDidMapper(didMapper);

			TransactionRunner transactionRunner = createTransactionRunner();

			boot.addModule(new CordaRestModule(serviceHub, cordaMapper));
			boot.addModule(HomeModule.create());
			boot.addModule(createJpaModule(transactionRunner));
			boot.addModule(new CordaTypesModule());

			RestContext context = new RestContext();
			context.setTransactionRunner(transactionRunner);
			context.setServiceHub(serviceHub);
			for (RestConfigurer configurer : ServiceLoader.load(RestConfigurer.class)) {
				configurer.configure(context, boot);
			}
		}

		private Module createJpaModule(TransactionRunner transactionRunner) {
			// TODO move transaction handling into repository? keep it away from other repositories? focus on repository
			//  .getEntityManager?
			JpaModuleConfig config = new JpaModuleConfig();
			Supplier<EntityManager> supplier = () -> Objects.requireNonNull(emLocal.get());

			return JpaModule.createServerModule(config, supplier, transactionRunner);
		}

		private TransactionRunner createTransactionRunner() {
			return new TransactionRunner() {
				@Override
				@SneakyThrows
				public <T> T doInTransaction(Callable<T> callable) {
					return (T) serviceHub.getDatabase().transaction(new CordaTransaction(callable));
				}
			};
		}

		@RequiredArgsConstructor
		private class CordaTransaction<T> implements Function1<SessionScope, T> {

			private final Callable<T> callable;

			@Override
			public T invoke(SessionScope sessionScope) {
				return serviceHub.withEntityManager(em -> {
					emLocal.set(em);
					T result = safeCall(callable);
					emLocal.remove();
					return result;
				});
			}

			@SneakyThrows
			private T safeCall(Callable<T> callable) {
				return callable.call();
			}
		}
	}
}
