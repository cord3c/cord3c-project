package io.cord3c.ssi.networkmap.adapter;

public class VCNetworkMapDevMain {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		VCNetworkMapMain.main(args);
	}
}
