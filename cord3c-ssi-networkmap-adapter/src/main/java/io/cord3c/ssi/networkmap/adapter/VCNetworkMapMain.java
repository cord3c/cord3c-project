package io.cord3c.ssi.networkmap.adapter;

public class VCNetworkMapMain {

	public static void main(String[] args) {
		VCNetworkMapMain app = new VCNetworkMapMain();
		app.run();
	}

	public VCNetworkMapMain() {
		int portOffset = 8000;
		System.setProperty("server.port", Integer.toString(portOffset + 110));
		System.setProperty("management.server.port", Integer.toString(portOffset + 110));
		System.setProperty("spring.profiles.active", "dev");
	}

	public void run() {
		String[] args = new String[0];
		VCNetworkMapMain.main(args);
	}
}
