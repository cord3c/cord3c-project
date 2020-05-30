package io.cord3c.ssi.networkmap.adapter;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import io.cord3c.ssi.networkmap.adapter.did.NetworkMapDocumentServlet;
import io.cord3c.ssi.networkmap.adapter.did.NodeInfoDocumentServlet;
import io.cord3c.ssi.networkmap.adapter.repository.NetworkParametersRepository;
import io.cord3c.ssi.networkmap.adapter.repository.NodeInfoRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.core.utilities.NetworkHostAndPort;
import net.corda.node.VersionInfo;
import net.corda.node.serialization.amqp.AMQPServerSerializationScheme;
import net.corda.node.services.network.NetworkMapClient;
import net.corda.nodeapi.internal.crypto.X509Utilities;
import net.corda.serialization.internal.SerializationFactoryImpl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({ CommonRestConfiguration.class })
@EnableConfigurationProperties({ VCNetworkMapProperties.class })
@Slf4j
public class VCNetworkMapConfiguration {

	@Autowired
	private VCNetworkMapProperties properties;

	@Bean
	@SneakyThrows
	public NetworkMapClient NetworkMapClient() {
		initSerialization();

		URI networkMapUrl = new URI(properties.getNetworkMapUrl());

		X509Certificate rootCertificate = fetchTrustStore();

		properties.setRootCertificate(rootCertificate);

		VersionInfo version = new VersionInfo(0, "", "", "");
		NetworkMapClient networkMapClient = new NetworkMapClient(networkMapUrl.toURL(), version);
		networkMapClient.start(rootCertificate);
		return networkMapClient;
	}

	@Bean
	public ServletRegistrationBean didServlet() {
		String url = "/.well-known/did.json";
		return new ServletRegistrationBean(new NetworkMapDocumentServlet(properties), url);
	}

	@Bean
	public ServletRegistrationBean didServlet(NodeInfoRepository repository) {
		String url = "/.well-known/did.json";
		return new ServletRegistrationBean(new NodeInfoDocumentServlet(repository), url);
	}

	@Bean
	public NetworkParametersRepository networkParametersRepository(NetworkMapClient networkMapClient) {
		return new NetworkParametersRepository(networkMapClient);
	}

	@Bean
	public NodeInfoRepository nodeInfoRepository(NetworkMapClient networkMapClient) {
		return new NodeInfoRepository(networkMapClient);
	}

	private void initSerialization() {
		// quite a bit hacky to misuse corda rpc, but the entire serialization stack seems rather complicated
		final SerializationFactoryImpl factoryImpl = new SerializationFactoryImpl();
		factoryImpl.registerScheme(new AMQPServerSerializationScheme());
		new CordaRPCClient(NetworkHostAndPort.parse("127.0.0.1:1"));
	}

	@SneakyThrows
	private X509Certificate fetchTrustStore() {
		String url = properties.getNetworkMapUrl() + "/network-map/truststore";
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.useSystemProperties();
		try (CloseableHttpClient client = builder.build()) {
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() <= 299) {
				byte[] bytes = EntityUtils.toByteArray(response.getEntity());

				KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
				truststore.load(new ByteArrayInputStream(bytes), null);
				Certificate certificate = truststore.getCertificate(X509Utilities.CORDA_ROOT_CA);

				return (X509Certificate) certificate;
			} else {
				throw new IllegalStateException("failed to obtain network truststore: got " + response.getStatusLine().toString());
			}
		}
	}
}
