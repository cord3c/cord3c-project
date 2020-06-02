package io.cord3c.rest.server.internal;

import com.google.auto.service.AutoService;
import io.cord3c.rest.server.CordaRestModule;
import io.cord3c.server.http.HttpServletFactory;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.Module;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.home.HomeModule;
import io.crnk.servlet.CrnkServlet;
import kotlin.jvm.functions.Function1;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.vault.CordaTransactionSupport;
import net.corda.core.node.services.vault.SessionScope;
import org.jetbrains.annotations.NotNull;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;


@AutoService(HttpServletFactory.class)
public class RestServletFactory implements HttpServletFactory {

	private static AppServiceHub serviceHub;

	@Override
	public String getPattern() {
		return "/api/*";
	}

	@Override
	public Class<? extends HttpServlet> getImplementation(AppServiceHub serviceHub) {
		RestServletFactory.serviceHub = serviceHub;

		return CordaCrnkServlet.class;
	}

	public static class CordaCrnkServlet extends CrnkServlet {

		@Override
		protected void initCrnk(CrnkBoot boot) {
			boot.addModule(new CordaRestModule(serviceHub));
			boot.addModule(HomeModule.create());
			boot.addModule(createJpaModule());
		}

		private Module createJpaModule() {
			JpaModuleConfig config = new JpaModuleConfig();
			Supplier<EntityManager> supplier = () -> serviceHub.withEntityManager(it -> it);
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
		private static class CordaTransaction<T> implements Function1<SessionScope, T> {

			private final Callable<T> callable;

			@SneakyThrows
			@Override
			public T invoke(SessionScope sessionScope) {
				return (T) callable.call();
			}
		}
	}
}
