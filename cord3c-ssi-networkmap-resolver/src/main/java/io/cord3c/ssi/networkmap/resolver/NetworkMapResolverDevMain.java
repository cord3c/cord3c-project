package io.cord3c.ssi.networkmap.resolver;

public class NetworkMapResolverDevMain {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "dev");
		NetworkMapResolverMain.main(args);
	}
}
