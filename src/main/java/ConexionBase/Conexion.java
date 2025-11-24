
package ConexionBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion implements IConexion {

    private final Database config;
    private Connection connection;

    public Conexion(Database config) {
        this.config = config;
    }

    @Override
    public Connection connect() throws SQLException {
        try {
            // Cargar el driver JDBC de SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Intentar establecer la conexión con o sin credenciales
            if (config.getUser() != null && config.getPassword() != null) {
                connection = DriverManager.getConnection(
                        config.getJdbcUrl(),
                        config.getUser(),
                        config.getPassword());
            } else {
                connection = DriverManager.getConnection(config.getJdbcUrl());
            }

            System.out.println("Conexión establecida con SQL Server (" + config.getDatabase() + ")");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de SQL Server", e);
        } catch (SQLException e) {
            throw new SQLException("Error al conectar con la base de datos: " + e.getMessage(), e);
        }

        return connection;
    }

    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("Conexión cerrada correctamente");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
