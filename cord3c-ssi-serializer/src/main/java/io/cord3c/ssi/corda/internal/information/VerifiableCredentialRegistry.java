package io.cord3c.ssi.corda.internal.information;

import com.google.common.base.Verify;
import io.cord3c.ssi.api.vc.W3CHelper;
import io.cord3c.ssi.corda.state.annotations.Subject;
import io.cord3c.ssi.corda.internal.party.PartyAdapterAccessor;
import io.cord3c.ssi.corda.internal.party.PartyRegistry;
import io.cord3c.ssi.corda.state.annotations.Claim;
import io.cord3c.ssi.corda.state.annotations.Issuer;
import io.cord3c.ssi.corda.state.annotations.VerifiableCredentialType;
import io.cord3c.ssi.corda.state.credential.EventState;
import io.cord3c.ssi.corda.internal.VerifiableCredentialUtils;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.internal.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import net.corda.core.identity.Party;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class VerifiableCredentialRegistry {

	private final String baseUrl;

	private Map<Class, VerifiableCredentialInformation> credentials = new ConcurrentHashMap<>();

	private final PartyRegistry partyRegistry;

	public VerifiableCredentialInformation get(Class implementationClass) {
		if (!credentials.containsKey(implementationClass)) {
			credentials.put(implementationClass, constructInformation(implementationClass));
		}
		VerifiableCredentialInformation information = credentials.get(implementationClass);
		Verify.verifyNotNull(information);
		return information;
	}

	public VerifiableCredentialInformation get(List<String> types) {
		Optional<VerifiableCredentialInformation> information = credentials.values().stream().filter(it -> it.getTypes().equals(types)).findFirst();
		return information.get();
	}

	private VerifiableCredentialInformation constructInformation(Class implementationClass) {
		VerifiableCredentialInformation information = new VerifiableCredentialInformation();
		information.setImplementationType(implementationClass);
		information.getTypes().addAll(deriveTypes(implementationClass));
		information.setTimestampAccessor(createTimestampAccessor());
		information.getContexts().addAll(createContexts());
		information.setIssuerAccessor(createPartyAccessor(Issuer.class, implementationClass));
		information.setSubjectAccessor(createPartyAccessor(Subject.class, implementationClass));
		information.setIdAccessor(createIdAccessor(information));

		BeanInformation beanInformation = BeanInformation.get(implementationClass);
		for (String name : beanInformation.getAttributeNames()) {
			BeanAttributeInformation attribute = beanInformation.getAttribute(name);
			if (attribute.getAnnotation(Claim.class).isPresent()) {
				ClaimInformation claimInformation = new ClaimInformation();
				claimInformation.setJsonName(attribute.getJsonName());
				claimInformation.setName(name);
				claimInformation.setAccessor(new ReflectionValueAccessor(attribute));
				information.getClaims().put(name, claimInformation);
			}
		}

		ClaimInformation subjectInformation = new ClaimInformation();
		subjectInformation.setJsonName(W3CHelper.CLAIM_SUBJECT_ID);
		subjectInformation.setName(W3CHelper.CLAIM_SUBJECT_ID);
		subjectInformation.setAccessor(information.getSubjectAccessor());
		information.getClaims().put(subjectInformation.getName(), subjectInformation);
		return information;
	}

	private ValueAccessor<String> createIdAccessor(VerifiableCredentialInformation information) {
		return new ValueAccessor<String>() {
			@Override
			public String getValue(Object state) {
				String type = information.getTypes().get(0);
				return UrlUtils.concat(baseUrl, type, "FIXME");
			}

			@Override
			public void setValue(Object state, String fieldValue) {
				// ignore, maybe at some point some objects like to have it
			}

			@Override
			public Class<? extends String> getImplementationClass() {
				return String.class;
			}
		};
	}

	private ValueAccessor<String> createPartyAccessor(Class annotation, Class implementationClass) {
		ValueAccessor accessor = VerifiableCredentialUtils.getAccessorForAnnotation(annotation, implementationClass);
		if (accessor.getImplementationClass() == Party.class) {
			accessor = new PartyAdapterAccessor(accessor, partyRegistry);
		}
		return accessor;
	}


	private static List<String> createContexts() {
		List<String> contexts = new ArrayList<>();
		contexts.add(W3CHelper.VC_CONTEXT_V1);
		//	contexts.add(W3CHelper.DEFAULT_VC_CONTEXT_2);
		return contexts;
	}

	private static ValueAccessor createTimestampAccessor() {
		return new ValueAccessor<Instant>() {
			@Override
			public Instant getValue(Object state) {
				if (state instanceof EventState) {
					return ((EventState) state).getTimestamp();
				}
				throw new IllegalStateException();
			}

			@Override
			public void setValue(Object state, Instant fieldValue) {
				((EventState) state).setTimestamp(fieldValue);
			}

			@Override
			public Class getImplementationClass() {
				return Instant.class;
			}
		};
	}

	private static Collection<? extends String> deriveTypes(Class implementationClass) {
		List<String> types = new ArrayList<>();
		types.add(W3CHelper.DEFAULT_VERIFIABLE_CREDENTIAL);

		VerifiableCredentialType annotation = (VerifiableCredentialType) implementationClass.getAnnotation(VerifiableCredentialType.class);
		String type = annotation.type();
		if (!type.isEmpty()) {
			types.add(type);
		}
		return types;

	}
}
