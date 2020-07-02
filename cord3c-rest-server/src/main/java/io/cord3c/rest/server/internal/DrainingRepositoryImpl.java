package io.cord3c.rest.server.internal;

import java.util.Collections;
import java.util.List;

import io.cord3c.rest.api.DrainingRepository;
import io.cord3c.rest.api.DrainingStatus;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import net.corda.core.flows.FlowLogic;
import net.corda.core.node.AppServiceHub;
import net.corda.node.internal.AbstractNode;
import net.corda.node.services.persistence.FlowsDrainingModeOperationsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrainingRepositoryImpl extends ResourceRepositoryBase<DrainingStatus, String> implements DrainingRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(DrainingRepositoryImpl.class);

	private static final String CURRENT_ID = "current";

	private final AbstractNode node;

	public DrainingRepositoryImpl(AppServiceHub serviceHub) {
		super(DrainingStatus.class);
		this.node = ServiceHubUtils.getNode(serviceHub);
	}

	@Override
	public ResourceList<DrainingStatus> findAll(QuerySpec querySpec) {
		return new DefaultResourceList<>(Collections.singletonList(getCurrent()), null, null);
	}

	@Override
	public DrainingStatus create(DrainingStatus input) {
		LOGGER.info("Enabling Corda node draining");
		FlowsDrainingModeOperationsImpl flowsDrainingMode = node.getNodeProperties().getFlowsDrainingMode();
		flowsDrainingMode.setEnabled(true, true);
		LOGGER.info("Returning draining status entity");
		return getCurrent();
	}

	@Override
	public void delete(String id) {
		LOGGER.info("Disabling Corda node draining");
		FlowsDrainingModeOperationsImpl flowsDrainingMode = node.getNodeProperties().getFlowsDrainingMode();
		flowsDrainingMode.setEnabled(false, true);
	}

	private DrainingStatus getCurrent() {
		DrainingStatus drainingStatus = new DrainingStatus(DrainingStatus.Status.OFF, CURRENT_ID, false);
		FlowsDrainingModeOperationsImpl flowsDrainingMode = node.getNodeProperties().getFlowsDrainingMode();

		Boolean drainingEnabled = flowsDrainingMode.isEnabled();
		if (drainingEnabled) {
			List<FlowLogic<?>> stateMachines = node.getSmm().getAllStateMachines();
			drainingStatus.setActive(true);
			boolean isDone = stateMachines.isEmpty();
			drainingStatus.setStatus(isDone ? DrainingStatus.Status.DRAINED : DrainingStatus.Status.DRAINING);
		}
		else {
			drainingStatus.setStatus(DrainingStatus.Status.OFF);
		}
		return drainingStatus;
	}
}
