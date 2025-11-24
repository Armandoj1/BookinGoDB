package ConexionBase;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Configuración para SQL Server usando la instancia nombrada SQLEXPRESS
        // con autenticación de SQL Server (usuario y contraseña)
        Database dbConfig = new Database(
                "localhost\\SQLEXPRESS", // server (con instancia nombrada)
                "BookinGoDB", // database
                "sa", // usuario SQL Server
                "jose" // contraseña
        );

        IConexion conexion = new Conexion(dbConfig);

        try {
            Connection conn = conexion.connect();
            System.out.println("Conexión exitosa a SQL Server.");
            System.out.println("Base de datos: " + dbConfig.getDatabase());
            System.out.println("Servidor: " + dbConfig.getServer());
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                conexion.disconnect();
            } catch (Exception e) {
                System.out.println("No se pudo cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
