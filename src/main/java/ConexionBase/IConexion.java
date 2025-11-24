package ConexionBase;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConexion {
    Connection connect() throws SQLException;
    void disconnect() throws SQLException;
}
