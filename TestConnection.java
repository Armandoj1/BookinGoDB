import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        // Conexión con autenticación integrada de Windows
        String url = "jdbc:sqlserver://localhost:1433;databaseName=BookinGoDB;integratedSecurity=true;encrypt=false;trustServerCertificate=true;";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("✔ Conexión exitosa papá 😎");
            System.out.println("URL: " + url);
            conn.close();
        } catch (Exception e) {
            System.out.println("✘ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
