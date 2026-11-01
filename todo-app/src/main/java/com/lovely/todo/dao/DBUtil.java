package com.lovely.todo.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBUtil {
    private static final Properties props = new Properties();
    private static boolean driverLoaded = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException ex) {
                driverLoaded = false;
            }
        }

        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String val = System.getProperty(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        String envKey = key.replace('.', '_').toUpperCase();
        val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        if (key.equals("db.host") && System.getenv("MYSQL_HOST") != null) {
            return System.getenv("MYSQL_HOST");
        }
        if (key.equals("db.port") && System.getenv("MYSQL_PORT") != null) {
            return System.getenv("MYSQL_PORT");
        }
        if (key.equals("db.name") && System.getenv("MYSQL_DATABASE") != null) {
            return System.getenv("MYSQL_DATABASE");
        }
        if (key.equals("db.user") && System.getenv("MYSQL_USER") != null) {
            return System.getenv("MYSQL_USER");
        }
        if (key.equals("db.password") && System.getenv("MYSQL_PASSWORD") != null) {
            return System.getenv("MYSQL_PASSWORD");
        }

        return props.getProperty(key, defaultValue);
    }

    public static String getJdbcUrl() {
        String host = getProperty("db.host", "100.53.19.151");
        String port = getProperty("db.port", "3306");
        String dbName = getProperty("db.name", "tododb");
        String connectTimeout = getProperty("db.connectTimeout", "3000");
        String socketTimeout = getProperty("db.socketTimeout", "5000");

        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                + "&connectTimeout=" + connectTimeout
                + "&socketTimeout=" + socketTimeout
                + "&characterEncoding=UTF-8&useUnicode=true";
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC Driver is not loaded.");
        }

        String url = getJdbcUrl();
        String user = getProperty("db.user", "root");
        String pass = getProperty("db.password", "password123");

        Connection conn = DriverManager.getConnection(url, user, pass);
        ensureTableCreated(conn);
        return conn;
    }

    private static void ensureTableCreated(Connection conn) {
        String ddl = "CREATE TABLE IF NOT EXISTS todos ("
                   + " id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                   + " title VARCHAR(255) NOT NULL,"
                   + " category VARCHAR(100) DEFAULT 'Personal 🌷',"
                   + " priority VARCHAR(20) DEFAULT 'MEDIUM',"
                   + " completed BOOLEAN DEFAULT FALSE,"
                   + " due_date VARCHAR(50),"
                   + " created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                   + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        } catch (SQLException ignored) {
        }
    }
}
