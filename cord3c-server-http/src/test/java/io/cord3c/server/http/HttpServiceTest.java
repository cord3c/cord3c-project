package io.cord3c.server.http;

import net.corda.core.node.AppServiceHub;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class HttpServiceTest implements WithAssertions {

	@Test
	public void verifyHttpServerAvailable() throws IOException {
		AppServiceHub serviceHub = Mockito.mock(AppServiceHub.class);
		HttpService service = new HttpService(serviceHub);
		try {
			String url = "http://127.0.0.1:" + service.getPort() + "/hello";
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(new HttpGet(url));
			assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
			assertThat(EntityUtils.toString(response.getEntity())).isEqualTo("World");
		} finally {
			service.destroy();
		}
	}
}
