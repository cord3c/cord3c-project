package io.cord3c.ssi.api.internal.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@UtilityClass
public class KeyFactoryHelper {

	public static final String KEY_ALGORITHM = "EC";

	public static final String CERTIFICATE_ALGORITHM = "SHA256withECDSA";

	public static final int DEFAULT_VALIDITY_IN_YEARS = 100;

	@SneakyThrows
	public static PublicKey constructPublicKeyFromEncodedBytes(byte[] encodedBytes) {
		return KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(encodedBytes));
	}

	@SneakyThrows
	public static Certificate generateCertificate(String legalName, KeyPair keyPair) {
		return generateSignedCertificate(keyPair, legalName);
	}

	@SneakyThrows
	public static KeyStore storeKeyPairInKeyStore(KeyPair keyPair, String legalName, String alias, String password) {
		Certificate[] chain = {generateCertificate(legalName, keyPair)};

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, null);
		keyStore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), chain);

		return keyStore;
	}

	@SneakyThrows
	public static ByteArrayOutputStream getKeyStoreAsByteStream(KeyStore keyStore, String password) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		keyStore.store(bos, password.toCharArray());
		return bos;
	}

	@SneakyThrows
	public static PublicKey loadPublicKeyFromKeyStoreBytes(byte[] keyStoreBytes, String alias, String password) {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(new ByteArrayInputStream(keyStoreBytes), password.toCharArray());
		return keyStore.getCertificate(alias).getPublicKey();
	}

	@SneakyThrows
	public static PrivateKey loadPrivateKeyFromKeyStoreBytes(byte[] keyStoreBytes, String alias, String password) {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(new ByteArrayInputStream(keyStoreBytes), password.toCharArray());
		return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
	}

	private static Certificate generateSignedCertificate(KeyPair keyPair, String subjectDN) throws OperatorCreationException, IOException, CertificateException {
		Provider bcProvider = new BouncyCastleProvider();
		Security.addProvider(bcProvider);

		long now = System.currentTimeMillis();
		Date startDate = new Date(now);

		X500Name dnName = new X500Name(subjectDN);
		BigInteger certSerialNumber = new BigInteger(Long.toString(now));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		calendar.add(Calendar.YEAR, DEFAULT_VALIDITY_IN_YEARS);

		Date endDate = calendar.getTime();

		ContentSigner contentSigner = new JcaContentSignerBuilder(CERTIFICATE_ALGORITHM).build(keyPair.getPrivate());

		JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

		// Extensions --------------------------

		// Basic Constraints
		BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

		certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true,
				basicConstraints); // Basic Constraints is usually marked as critical.

		// -------------------------------------

		return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
	}

}
