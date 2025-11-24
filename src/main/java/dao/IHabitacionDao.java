
package dao;

import model.Habitacion;
import java.sql.SQLException;
import java.util.List;

public interface IHabitacionDao extends IBaseDao<Habitacion> {
    List<Habitacion> findByEstado(String estado) throws SQLException;

    List<Habitacion> findByTipo(String tipo) throws SQLException;

    Habitacion findByNumero(String numero) throws SQLException;

    int countByEstado(String estado) throws SQLException;
}
