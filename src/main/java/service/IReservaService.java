package service;

import model.Reserva;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface IReservaService {

    Reserva crear(Reserva reserva) throws SQLException;

    Reserva actualizar(Reserva reserva) throws SQLException;

    void eliminar(Long idReserva) throws SQLException;

    Reserva buscarPorId(Long idReserva) throws SQLException;

    List<Reserva> listarTodas() throws SQLException;

    List<Reserva> listarPorCliente(Long idCliente) throws SQLException;

    List<Reserva> listarPorEstado(String estado) throws SQLException;

    List<Reserva> listarPorFecha(LocalDate fechaReserva) throws SQLException;
}
