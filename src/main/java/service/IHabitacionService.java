
package service;

import model.Habitacion;

import java.sql.SQLException;
import java.util.List;

public interface IHabitacionService {

    Habitacion crear(Habitacion habitacion) throws SQLException;

    Habitacion actualizar(Habitacion habitacion) throws SQLException;

    void eliminar(Long idHabitacion) throws SQLException;

    Habitacion buscarPorId(Long idHabitacion) throws SQLException;

    Habitacion buscarPorNumero(String numero) throws SQLException;

    List<Habitacion> listarTodas() throws SQLException;

    List<Habitacion> listarPorEstado(String estado) throws SQLException;

    List<Habitacion> listarPorTipo(String tipo) throws SQLException;

    void cambiarEstado(Long idHabitacion, String nuevoEstado) throws SQLException;
}
