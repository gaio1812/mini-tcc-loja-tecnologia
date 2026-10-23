package br.com.lojatecnologia;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

public final class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static Database configured() throws IOException {
        Properties props = new Properties();
        Path config = Path.of("config.properties");
        if (Files.exists(config)) {
            try (Reader in = Files.newBufferedReader(config, StandardCharsets.UTF_8)) { props.load(in); }
        }
        String url = setting(props, "db.url", "LOJA_DB_URL", "");
        if (url.isBlank()) {
            Files.createDirectories(Path.of("data"));
            url = "jdbc:h2:file:./data/loja;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE";
        }
        if (!url.startsWith("jdbc:h2:") && !url.startsWith("jdbc:mysql:")) {
            throw new IOException("Banco não suportado. Use H2 ou MySQL em config.properties.");
        }
        return new Database(url, setting(props, "db.user", "LOJA_DB_USER", "sa"),
                setting(props, "db.password", "LOJA_DB_PASSWORD", ""));
    }

    private static String setting(Properties p, String key, String env, String fallback) {
        String value = System.getenv(env);
        return value == null ? p.getProperty(key, fallback) : value;
    }

    public boolean isLocal() { return url.startsWith("jdbc:h2:file:"); }

    public Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }

    public void initialize() throws SQLException, IOException {
        String schema;
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IOException("O arquivo schema.sql não foi incluído no aplicativo.");
            schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection c = open(); Statement s = c.createStatement()) {
            for (String command : schema.split(";")) {
                if (!command.isBlank()) s.execute(command);
            }
            try { s.executeUpdate("INSERT INTO TBAPPLOCK(id) VALUES (1)"); }
            catch (SQLException e) {
                if (e.getSQLState() == null || !e.getSQLState().startsWith("23")) throw e;
            }
        }
    }
}
