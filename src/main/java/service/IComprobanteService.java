
package service;

import model.Comprobante;

import java.sql.SQLException;
import java.util.List;

public interface IComprobanteService {

    Comprobante crear(Comprobante comprobante) throws SQLException;

    Comprobante actualizar(Comprobante comprobante) throws SQLException;

    void eliminar(Long idComprobante) throws SQLException;

    Comprobante buscarPorId(Long idComprobante) throws SQLException;

    List<Comprobante> listarTodos() throws SQLException;

    List<Comprobante> listarPorReserva(Long idReserva) throws SQLException;

    List<Comprobante> listarPorEstado(String estado) throws SQLException;
}