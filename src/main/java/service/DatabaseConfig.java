package service;

import ConexionBase.Database;

/**
 * Configuración centralizada de la base de datos
 * Esta clase proporciona la configuración de conexión utilizada en toda la
 * aplicación
 */
public class DatabaseConfig {

    // Configuración para SQL Server usando la instancia nombrada SQLEXPRESS
    // con autenticación de SQL Server (usuario y contraseña)
    private static final String SERVER = "localhost\\SQLEXPRESS";
    private static final String DATABASE_NAME = "BookinGoDB";
    private static final String USER = "sa";
    private static final String PASSWORD = "jose";

    /**
     * Obtiene la configuración de base de datos para usar en la aplicación
     * 
     * @return Objeto Database configurado con las credenciales correctas
     */
    public static Database getDatabase() {
        return new Database(SERVER, DATABASE_NAME, USER, PASSWORD);
    }

    /**
     * Obtiene el nombre del servidor
     * 
     * @return Nombre del servidor
     */
    public static String getServer() {
        return SERVER;
    }

    /**
     * Obtiene el nombre de la base de datos
     * 
     * @return Nombre de la base de datos
     */
    public static String getDatabaseName() {
        return DATABASE_NAME;
    }
}
