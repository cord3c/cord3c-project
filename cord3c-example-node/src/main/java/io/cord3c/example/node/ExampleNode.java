package io.cord3c.example.node;

import com.google.common.base.Verify;
import kotlin.Pair;
import lombok.SneakyThrows;
import net.corda.core.crypto.Crypto;
import net.corda.node.SharedNodeCmdLineOptions;
import net.corda.node.internal.Node;
import net.corda.node.internal.NodeStartup;
import net.corda.node.internal.RunAfterNodeInitialisation;
import net.corda.node.internal.subcommands.InitialRegistration;
import net.corda.nodeapi.internal.crypto.CertificateAndKeyPair;
import net.corda.nodeapi.internal.crypto.CertificateType;
import net.corda.nodeapi.internal.crypto.X509Utilities;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;

public class ExampleNode {

	private String networkMapUrl = "http://localhost:8080";

	private File configDir;

	private File baseDir;

	private File truststoreFile;

	private File nodeConfigFile;

	private int h2Port = 8084;

	private String databaseUser = "sa";

	private String databasePassword = "helloworld";

	private Server h2Server;

	private boolean networkMapEnabled;

	private Logger log;

	private String env;

	public static void main(String[] args) {
		ExampleNode node = new ExampleNode();
		node.start();
	}

	private void start() {
		configure();
		configureLogging();
		if (!networkMapEnabled) {
			generateNetworkParameters();
		}
		configureNode();
		startDatabase();
		downloadTrustStore();
		run();
	}

	@SneakyThrows
	private void generateNetworkParameters() {
		File file = new File(baseDir, "network-parameters");
		if (!networkMapEnabled && !file.exists()) {
			FileUtils.copyFile(new File(configDir, file.getName()), file);
		}
	}


	private static CertificateAndKeyPair createDevNetworkMapCa(CertificateAndKeyPair rootCa) {
		KeyPair keyPair = Crypto.generateKeyPair();
		Pair<Duration, Duration> validityWindow = new Pair<>(Duration.ZERO, Duration.ofDays(365 * 10L));
		X509Certificate cert = X509Utilities.createCertificate(CertificateType.NETWORK_MAP, rootCa.getCertificate(),
				rootCa.getKeyPair(), new X500Principal("CN=Network Map,O=R3 Ltd,L=London,C=GB"), keyPair.getPublic(),
				validityWindow, null, null, null); // TODO add in corda 4.0 , null, null
		return new CertificateAndKeyPair(cert, keyPair);
	}

	private void configure() {
		File projectDir = new File("").getAbsoluteFile();
		if (!projectDir.getName().equals("cord3c-example-node")) {
			projectDir = new File(projectDir, "cord3c-example-node");
		}
		Verify.verify(projectDir.exists());

		configDir = new File(projectDir, "src/main/extraFiles/etc/cord3c/");

		baseDir = new File(projectDir, "build/data");

		env = PropertyUtils.getProperty("cord3c.env", "dev");
		networkMapEnabled = Boolean.parseBoolean(PropertyUtils.getProperty("cord3c.networkmap.enabled", Boolean.toString("prod".equals(env))));
		baseDir.mkdirs();

		System.setProperty("cord3c.server.url", "http://localhost:8090");
		System.setProperty("cord3c.networkmap.url", "http://localhost:8080");
	}

	@SneakyThrows
	private void startDatabase() {
		h2Server = Server.createTcpServer("-tcpPassword", databasePassword, "-tcpPort", Integer.toString(h2Port),
				"-ifNotExists", "-tcpAllowOthers",
				"-tcpDaemon", "-baseDir", baseDir.getAbsolutePath()).start();
		if (h2Server.isRunning(true)) {
			log.info("H2 server was started and is running on port " + h2Port
					+ " use jdbc:h2:tcp://localhost:" + h2Port + "/build/data");
		} else {
			throw new IllegalStateException("Could not start H2 server.");
		}
	}


	@SneakyThrows
	private void configureNode() {
		System.setProperty("cord3c.ssi.networkmap.url", networkMapUrl);
		nodeConfigFile = new File(configDir, "node-" + env + ".conf");
		String nodeConfig = FileUtils.readFileToString(nodeConfigFile, StandardCharsets.UTF_8);
		nodeConfig = nodeConfig.replace("${networkMapUrl}", networkMapUrl);
		FileUtils.writeStringToFile(new File(baseDir, "node.conf"), nodeConfig, StandardCharsets.UTF_8);
		truststoreFile = new File(baseDir, "network-truststore.jks");
	}

	private void configureLogging() {
		File logFile = new File(configDir, "log4j2.xml");
		Verify.verify(logFile.exists(), logFile.getAbsolutePath());
		System.setProperty("log4j.configurationFile", logFile.getAbsolutePath());
		log = LoggerFactory.getLogger(getClass());
		System.setProperty("net.corda.node.printErrorsToStdErr", "true");
	}

	@SneakyThrows
	private void run() {
		NodeStartup startup = new NodeStartup();
		RunAfterNodeInitialisation registration = node -> {
			Verify.verify(truststoreFile.exists());

			if (networkMapEnabled && !isRegistered()) {
				InitialRegistration initialRegistration = new InitialRegistration(baseDir.toPath(), truststoreFile.toPath(), "trustpass", startup);
				initialRegistration.run(node);
				log.warn("*************************************************************************************************************");
				log.warn("performed registration with network map, please restart or fix https://github.com/corda/corda/issues/6318 :-)");
				log.warn("*************************************************************************************************************");
				System.exit(0);
			}
			startup.startNode(node, System.currentTimeMillis());
		};
		boolean requireCertificates = false;

		SharedNodeCmdLineOptions cmdOptions = new SharedNodeCmdLineOptions();
		cmdOptions.setBaseDirectory(baseDir.toPath());
		cmdOptions.setDevMode(false);

		startup.initialiseAndRun(cmdOptions, registration, requireCertificates);
	}

	private boolean isRegistered() {
		return Arrays.asList(baseDir.listFiles()).stream().filter(it -> it.getName().startsWith("nodeInfo-")).findAny().isPresent();
	}

	@SneakyThrows
	private void skipSerializationSetup(Node node) {
		// fix this in corda, registration and running does not work together
		System.out.println(node.getClass());
		System.out.println(Arrays.asList(node.getClass().getDeclaredFields()));

		Field initialiseSerialization = node.getClass().getDeclaredField("initialiseSerialization");
		initialiseSerialization.setAccessible(true);
		initialiseSerialization.set(node, false);

	}

	@SneakyThrows
	private void downloadTrustStore() {
		String url = networkMapUrl + "/network-map/truststore";
		try (CloseableHttpClient client = HttpClientBuilder.create().useSystemProperties().build()) {
			HttpGet get = new HttpGet(url);
			CloseableHttpResponse response = client.execute(get);
			Verify.verify(response.getStatusLine().getStatusCode() == 200);
			FileUtils.writeByteArrayToFile(truststoreFile, EntityUtils.toByteArray(response.getEntity()));
		}
	}
}
