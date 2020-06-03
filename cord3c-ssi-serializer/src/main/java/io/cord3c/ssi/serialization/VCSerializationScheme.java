package io.cord3c.ssi.serialization;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.cord3c.ssi.api.SSIFactory;
import io.cord3c.ssi.serialization.internal.information.VerifiableCredentialRegistry;
import io.cord3c.ssi.serialization.internal.party.PartyRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.core.serialization.SerializationContext;
import net.corda.core.serialization.SerializedBytes;
import net.corda.core.transactions.WireTransaction;
import net.corda.core.utilities.ByteSequence;
import net.corda.serialization.internal.CordaSerializationMagic;
import net.corda.serialization.internal.SerializationScheme;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class VCSerializationScheme implements SerializationScheme {

	private static final byte[] MAGIC = "W3CVC".getBytes();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final VCMapper credentialMapper;

	public VCSerializationScheme(PartyRegistry partyRegistry, String baseUrl) {
		SSIFactory factory = new SSIFactory();
		VerifiableCredentialRegistry registry = new VerifiableCredentialRegistry(baseUrl, partyRegistry);
		ObjectMapper claimMapper = factory.getClaimMapper();
		credentialMapper = new VCMapper(registry, claimMapper);

		SimpleModule cordaModule = new SimpleModule("corda");
		cordaModule.addSerializer(new WireTransactionSerializer("networkmap.local", baseUrl, credentialMapper));
		cordaModule.addDeserializer(WireTransaction.class, new WireTransactionDeserializer());
		objectMapper.registerModule(cordaModule);
	}

	public VCMapper getCredentialMapper() {
		return credentialMapper;
	}

	@Override
	public boolean canDeserializeVersion(@NotNull CordaSerializationMagic magic, @NotNull SerializationContext.UseCase target) {
		return Arrays.equals(magic.getBytes(), MAGIC); // && target == SerializationContext.UseCase.Storage;
	}

	@NotNull
	@Override
	@SneakyThrows
	public <T> T deserialize(@NotNull ByteSequence byteSequence, @NotNull Class<T> clazz, @NotNull SerializationContext context) throws NotSerializableException {
		log.info("deserialize {}", clazz);

		ObjectReader objectReader = objectMapper.readerFor(clazz);
		return (T) objectReader.readValue(byteSequence.getBytes(), byteSequence.getOffset() + MAGIC.length, byteSequence.getSize() - MAGIC.length);
	}

	@NotNull
	@Override
	@SneakyThrows
	public <T> SerializedBytes<T> serialize(@NotNull T obj, @NotNull SerializationContext context) throws NotSerializableException {
		log.info("serialize {}", obj);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(MAGIC);
		ObjectWriter writer = objectMapper.writerFor(obj.getClass());
		writer.writeValue(output, obj);

		if (log.isInfoEnabled()) {
			String json = writer.withDefaultPrettyPrinter().writeValueAsString(obj);
			log.info(json);
		}

		return new SerializedBytes<>(output.toByteArray());
	}
}
