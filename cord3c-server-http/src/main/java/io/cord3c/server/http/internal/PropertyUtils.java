package io.cord3c.server.http.internal;

public class PropertyUtils {


	public static String getProperty(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			value = System.getenv(toEnvName(name));
		}
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	protected static String toEnvName(String name) {
		StringBuilder envKey = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (c == '.') {
				envKey.append('_');
			} else {
				envKey.append(Character.toUpperCase(c));
			}
		}
		return envKey.toString();
	}

}
