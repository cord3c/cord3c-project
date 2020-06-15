package io.cord3c.rest.server.internal;

import com.google.auto.service.AutoService;
import io.cord3c.rest.server.CordaRestModule;
import io.cord3c.server.http.HttpServletFactory;
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
import org.mapstruct.factory.Mappers;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import java.util.concurrent.Callable;
import java.util.function.Supplier;


@AutoService(HttpServletFactory.class)
public class RestServletFactory implements HttpServletFactory {

	private static AppServiceHub serviceHub;

	@Getter
	@Setter
	private static String networkMapUrl;


	@Override
	public String getPattern() {
		return "/api/node/*";
	}

	@Override
	public Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub) {
		RestServletFactory.serviceHub = serviceHub;

		return CordaCrnkServlet.class;
	}


	public static class CordaCrnkServlet extends CrnkServlet {

		private CordaMapper cordaMapper = Mappers.getMapper(CordaMapper.class);

		private ThreadLocal<EntityManager> emLocal = new ThreadLocal<>();

		@Override
		protected void initCrnk(CrnkBoot boot) {
			if (networkMapUrl == null) {
				networkMapUrl = System.getenv("CORD3C_SSI_NETWORKMAP_URL");
			}
			if (networkMapUrl == null) {
				networkMapUrl = System.getProperty("cord3c.networkmap.url");
			}
			if (networkMapUrl == null) {
				throw new IllegalStateException("please configure CORD3C_SSI_NETWORKMAP_HOST or set manually on RestServletFactory");
			}

			PartyToDIDMapper didMapper = new PartyToDIDMapper();
			didMapper.setNetworkMapUrl(networkMapUrl);
			cordaMapper.setDidMapper(didMapper);

			boot.addModule(new CordaRestModule(serviceHub, cordaMapper));
			boot.addModule(HomeModule.create());
			boot.addModule(createJpaModule());
		}

		private Module createJpaModule() {
			// TODO move transaction handling into repository? keep it away from other repositories? focus on repository.getEntityManager?
			JpaModuleConfig config = new JpaModuleConfig();
			Supplier<EntityManager> supplier = () -> emLocal.get();// serviceHub.withEntityManager(it -> it);
			TransactionRunner transactionRunner = new TransactionRunner() {
				@Override
				@SneakyThrows
				public <T> T doInTransaction(Callable<T> callable) {
					return (T) serviceHub.getDatabase().transaction(new CordaTransaction(callable));
				}
			};
			return JpaModule.createServerModule(config, supplier, transactionRunner);
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
