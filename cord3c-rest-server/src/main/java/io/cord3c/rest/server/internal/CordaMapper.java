package io.cord3c.rest.server.internal;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import io.cord3c.rest.api.map.NodeDTO;
import io.cord3c.rest.api.map.NotaryDTO;
import io.cord3c.rest.api.map.PartyDTO;
import io.cord3c.ssi.corda.internal.party.PartyToDIDMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.corda.core.crypto.Base58;
import net.corda.core.crypto.CryptoUtils;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.NotaryInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public abstract class CordaMapper {

	@Getter
	@Setter
	protected PartyToDIDMapper didMapper;

	@Mapping(target = "id", expression = "java(getId(nodeInfo))")
	public abstract NodeDTO map(NodeInfo nodeInfo);

	@Mapping(target = "id", expression = "java(getId(notaryInfo.getIdentity()))")
	public abstract NotaryDTO map(NotaryInfo notaryInfo);

	@Mapping(target = "id", expression = "java(getId(party))")
	@Mapping(target = "did", expression = "java(didMapper.toDid(party.getParty().getOwningKey()))")
	public abstract PartyDTO mapParty(PartyAndCertificate party);

	@Mapping(target = "id", expression = "java(getId(party))")
	@Mapping(target = "did", expression = "java(didMapper.toDid(party.getOwningKey()))")
	public abstract PartyDTO mapParty(Party party);

	public Party unmapParty(PartyDTO party) {
		String name = party.getName().toString().replaceAll("\\s+", "");
		return new Party(CordaX500Name.parse(name), party.decodeOwningKey());
	}

	public String map(PublicKey publicKey) {
		return Base58.encode(publicKey.getEncoded());
	}

	@SneakyThrows
	public PublicKey unmap(String base58) {
		byte[] bytes = Base58.decode(base58);
		KeyFactory keyFactory = KeyFactory.getInstance("EC");
		return keyFactory.generatePublic(new X509EncodedKeySpec(bytes));
	}

	protected String getId(NodeInfo nodeInfo) {
		CordaX500Name name = nodeInfo.getLegalIdentities().get(0).getName();
		StringBuilder builder = new StringBuilder();
		append(builder, name.getCommonName());
		append(builder, name.getOrganisation());
		append(builder, name.getOrganisationUnit());
		append(builder, name.getState());
		append(builder, name.getLocality());
		append(builder, name.getCountry());
		return builder.toString();
	}

	private void append(StringBuilder builder, String value) {
		if (value != null) {
			if (builder.length() > 0) {
				builder.append("_");
			}
			builder.append(value.replace(" ", "").toLowerCase());
		}
	}

	protected String getId(PartyAndCertificate party) {
		return getId(party.getParty());
	}

	protected String getId(Party party) {
		return CryptoUtils.toStringShort(party.getOwningKey());
	}
}