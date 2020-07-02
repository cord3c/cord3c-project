package io.cord3c.rest.server.internal;

import java.lang.reflect.Field;

import lombok.SneakyThrows;
import net.corda.core.internal.ServiceHubCoreInternal;
import net.corda.core.node.AppServiceHub;
import net.corda.node.internal.AbstractNode;
import net.corda.node.services.statemachine.StateMachineManager;

public class ServiceHubUtils {

	public static AbstractNode getNode(AppServiceHub serviceHub) {
		ServiceHubCoreInternal internalServiceHub = getServiceHubInternal(serviceHub);
		return (AbstractNode) getField(internalServiceHub, "this$0");
	}

	public static ServiceHubCoreInternal getServiceHubInternal(AppServiceHub serviceHub) {
		return (ServiceHubCoreInternal) getField(serviceHub, "serviceHub");
	}

	private static Object getField(Object object, String name) {
		return getField(object.getClass(), object, name);
	}

	@SneakyThrows
	private static Object getField(Class declaringClass, Object object, String name) {
		Field field = declaringClass.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(object);
	}

	public static StateMachineManager getStateMachineManager(AppServiceHub serviceHub) {
		AbstractNode node = ServiceHubUtils.getNode(serviceHub);
		return (StateMachineManager) getField(AbstractNode.class, node, "smm");
	}
}
