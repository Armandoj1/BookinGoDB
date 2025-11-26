
package dao;

import model.Trabajador;
import java.sql.SQLException;

public interface ITrabajadorDao extends IBaseDao<Trabajador> {
    Trabajador findByUsuario(String usuario) throws SQLException;

    Trabajador findByDocumento(String documento) throws SQLException;
    Trabajador findByEmail(String email) throws SQLException;
    Trabajador findByTelefono(long telefono) throws SQLException;

    Trabajador authenticate(String usuario, String contrasena) throws SQLException;

    int countByRol(Long idRol) throws SQLException;

    void activate(Long id) throws SQLException;

    Long createUsingProcedure(Trabajador trabajador) throws SQLException;
}
