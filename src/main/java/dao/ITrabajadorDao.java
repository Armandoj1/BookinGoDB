
package dao;

import model.Trabajador;
import java.sql.SQLException;

public interface ITrabajadorDao extends IBaseDao<Trabajador> {
    Trabajador findByUsuario(String usuario) throws SQLException;

    Trabajador authenticate(String usuario, String contrasena) throws SQLException;

    int countByRol(Long idRol) throws SQLException;

    void activate(Long id) throws SQLException;

    Long createUsingProcedure(Trabajador trabajador) throws SQLException;
}
