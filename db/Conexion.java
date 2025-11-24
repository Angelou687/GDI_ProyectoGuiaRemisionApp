package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    // Defaults (used only if no env / system prop / properties file provided)
    private static final String DEFAULT_URL  = "jdbc:postgresql://localhost:5432/guia_remision";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASS = "root";

    private static String URL = DEFAULT_URL;
    private static String USER = DEFAULT_USER;
    private static String PASS = DEFAULT_PASS;

    static {
        try {
            Class.forName("org.postgresql.Driver"); // Driver JDBC de PostgreSQL
        } catch (ClassNotFoundException e) {
            System.err.println("No se pudo cargar el driver de PostgreSQL: " + e.getMessage());
        }

        // 1) System properties (e.g. -Ddb.url=..)
        String pUrl = System.getProperty("db.url");
        String pUser = System.getProperty("db.user");
        String pPass = System.getProperty("db.pass");

        // prefer system properties
        if (isValid(pUrl) && isValid(pUser)) {
            URL = pUrl; USER = pUser; PASS = pPass == null ? "" : pPass;
            System.out.println("Conexion: usando configuración desde system properties");
        } else {
            // env vars
            String eUrl = System.getenv("DB_URL");
            String eUser = System.getenv("DB_USER");
            String ePass = System.getenv("DB_PASS");
            if (isValid(eUrl) && isValid(eUser)) {
                URL = eUrl; USER = eUser; PASS = ePass == null ? "" : ePass;
                System.out.println("Conexion: usando configuración desde variables de entorno");
            } else {
                // properties file
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("db.properties")) {
                    props.load(fis);
                    String fUrl = props.getProperty("db.url");
                    String fUser = props.getProperty("db.user");
                    String fPass = props.getProperty("db.pass");
                    if (isValid(fUrl) && isValid(fUser)) {
                        URL = fUrl; USER = fUser; PASS = fPass == null ? "" : fPass;
                        System.out.println("Conexion: usando configuración desde db.properties");
                    } else {
                        System.out.println("Conexion: usando valores por defecto (comprueba URL/usuario/clave si no funciona)");
                    }
                } catch (IOException ignored) {
                    System.out.println("Conexion: usando valores por defecto (db.properties no encontrada)");
                }
            }
        }
    }

    private static boolean isValid(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException ex) {
            // Mejor mensaje para ayudar al usuario a depurar
            String msg = String.format("Error conectando a DB. URL=%s user=%s : %s", URL, USER, ex.getMessage());
            throw new SQLException(msg, ex);
        }
    }
}
