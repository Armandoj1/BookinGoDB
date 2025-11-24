
package service;

import model.Trabajador;

import java.sql.SQLException;
import java.util.List;

public interface ITrabajadorService {

    Trabajador crear(Trabajador trabajador) throws SQLException;

    Trabajador crearConProcedimiento(Trabajador trabajador) throws SQLException;

    Trabajador actualizar(Trabajador trabajador) throws SQLException;

    void eliminar(Long idTrabajador) throws SQLException;

    void activar(Long idTrabajador) throws SQLException;

    Trabajador buscarPorId(Long idTrabajador) throws SQLException;

    List<Trabajador> listarTodos() throws SQLException;

    Trabajador buscarPorUsuario(String usuario) throws SQLException;

    Trabajador autenticar(String usuario, String contrasena) throws SQLException;
}
