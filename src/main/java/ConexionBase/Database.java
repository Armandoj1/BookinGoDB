package ConexionBase;

public class Database {
    private final String server;
    private final String database;
    private final String user;
    private final String password;

    // Constructor con usuario y contraseña para SQL Server Authentication
    public Database(String server, String database, String user, String password) {
        this.server = server;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    // Constructor sin credenciales (usa autenticación integrada de Windows)
    public Database(String server, String database) {
        this(server, database, null, null);
    }

    public String getJdbcUrl() {
        // Usar la instancia nombrada SQLEXPRESS como en la prueba exitosa
        return "jdbc:sqlserver://" + server + ";databaseName=" + database +
                ";encrypt=false;trustServerCertificate=true;";
    }

    public String getServer() {
        return server;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
