
package service;

import model.Cliente;

import java.sql.SQLException;
import java.util.List;

public interface IClienteService {

    Cliente crear(Cliente cliente) throws SQLException;

    Cliente actualizar(Cliente cliente) throws SQLException;

    void eliminar(Long idCliente) throws SQLException;

    void activar(Long idCliente) throws SQLException;

    Cliente buscarPorId(Long idCliente) throws SQLException;

    Cliente buscarPorDocumento(String documento) throws SQLException;

    List<Cliente> listarTodos() throws SQLException;

    List<Cliente> listarPorEstado(String estado) throws SQLException;
}