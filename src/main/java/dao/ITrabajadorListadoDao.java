package dao;

import model.Trabajador;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO de solo lectura para listar trabajadores desde FN_GET_TRABAJADORES()
 * Columnas esperadas: ID, NombreCompleto, Usuario, Rol, Sueldo, estado
 */
public interface ITrabajadorListadoDao {
    List<Trabajador> findAll() throws SQLException;
}