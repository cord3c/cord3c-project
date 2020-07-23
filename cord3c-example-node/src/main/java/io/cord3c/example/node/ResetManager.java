package io.cord3c.example.node;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
@RequiredArgsConstructor
public class ResetManager {

	private final String databaseDriverClassName;

	private final String databaseUrl;

	private final String databaseUser;

	private final String databasePassword;

	@SneakyThrows
	public Connection createDatabaseConnection() {
		if (databaseDriverClassName != null) {
			Class.forName(databaseDriverClassName);
		}
		return DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
	}

	@SneakyThrows
	public void softReset() {
		log.info("performing softReset");
		try (Connection connection = createDatabaseConnection(); Statement stmt = connection.createStatement()) {
			DatabaseMetaData m = connection.getMetaData();
			ResultSet tables = m.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"});

			boolean hasCordaTables = false;
			while (tables.next()) {
				String schema = tables.getString(2);
				String name = tables.getString(3).toLowerCase();
				if (!name.startsWith("vault_") && !name.startsWith("state_") && !name.startsWith("node_") && !name.startsWith("databasechange") && !name.endsWith("_ext_change_log")) {
					String sql = String.format("DROP TABLE IF EXISTS %s.%s CASCADE", schema, name);
					log.info("executing {}", sql);
					stmt.executeUpdate(sql);
				} else {
					hasCordaTables = true;
				}
			}

			if (hasCordaTables) {
				stmt.executeUpdate("DELETE FROM node_attachments_contracts");
				stmt.executeUpdate("DELETE FROM node_attachments_signers");
				stmt.executeUpdate("DELETE FROM node_attachments");

				stmt.executeUpdate("DELETE FROM node_contract_upgrades");
				stmt.executeUpdate("DELETE FROM node_checkpoints");
				stmt.executeUpdate("DELETE FROM node_transactions");

				stmt.executeUpdate("DELETE FROM state_party");

				stmt.executeUpdate("DELETE FROM vault_fungible_states");
				stmt.executeUpdate("DELETE FROM vault_fungible_states_parts");
				stmt.executeUpdate("DELETE FROM vault_linear_states");
				stmt.executeUpdate("DELETE FROM vault_linear_states_parts");
				stmt.executeUpdate("DELETE FROM vault_states");
				stmt.executeUpdate("DELETE FROM vault_transaction_notes");
			}

		}
	}

}
