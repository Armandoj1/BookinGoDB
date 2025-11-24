package ConexionBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConexion {
    public static void main(String[] args) {
        System.out.println("=== PROBANDO CONEXIONES A SQL SERVER ===\n");

        // Prueba 1: Conexión sin autenticación integrada (la que sugeriste)
        System.out.println("Prueba 1: Conexión directa sin autenticación integrada");
        String url1 = "jdbc:sqlserver://localhost\\SQLEXPRESS;"
                + "databaseName=BookinGoDB;"
                + "encrypt=false;"
                + "trustServerCertificate=true;";
        probarConexion(url1, null, null);

        // Prueba 2: Conexión con autenticación integrada
        System.out.println("\nPrueba 2: Conexión con autenticación integrada de Windows");
        String url2 = "jdbc:sqlserver://localhost\\SQLEXPRESS;"
                + "databaseName=BookinGoDB;"
                + "integratedSecurity=true;"
                + "encrypt=false;"
                + "trustServerCertificate=true;";

        probarConexion(url2, null, null);

        // Prueba 3: Conexión con usuario y contraseña (si tienes credenciales SQL
        // Server)
        System.out.println("\nPrueba 3: Conexión con usuario SQL Server (descomentarla si tienes credenciales)");
        String url3 = "jdbc:sqlserver://localhost\\SQLEXPRESS;" +
                "databaseName=BookinGoDB;" +
                "encrypt=false;" +
                "trustServerCertificate=true;";

        probarConexion(url3, "sa", "jose");

        System.out.println("\n=== FIN DE PRUEBAS ===");
    }

    private static void probarConexion(String url, String user, String password) {
        try {
            // Cargar el driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            Connection conn;
            if (user != null && password != null) {
                conn = DriverManager.getConnection(url, user, password);
            } else {
                conn = DriverManager.getConnection(url);
            }

            System.out.println("✓ CONEXIÓN EXITOSA!");
            System.out.println("  URL: " + url);

            // Probar una consulta simple
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DB_NAME() AS DatabaseName, @@VERSION AS Version");

            if (rs.next()) {
                System.out.println("  Base de datos: " + rs.getString("DatabaseName"));
                System.out.println("  Versión SQL Server: " + rs.getString("Version").substring(0, 100) + "...");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("✗ ERROR en la conexión:");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Causa: " + e.getCause().getMessage());
            }
        }
    }
}
