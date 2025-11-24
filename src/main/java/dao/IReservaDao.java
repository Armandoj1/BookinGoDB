
package dao;

import model.Reserva;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IReservaDao extends IBaseDao<Reserva> {
    List<Reserva> findByCliente(Long idCliente) throws SQLException;

    List<Reserva> findByEstado(String estado) throws SQLException;

    List<Reserva> findByFecha(LocalDate fecha) throws SQLException;

    // Conteo total de reservas (SELECT COUNT(*) FROM RESERVA)
    int countAll() throws SQLException;

    int countByEstado(String estado) throws SQLException;
}
