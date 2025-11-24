
package dao;

import model.Cliente;
import java.sql.SQLException;
import java.util.List;

public interface IClienteDao extends IBaseDao<Cliente> {

    Cliente findByDocumento(String documento) throws SQLException;

    List<Cliente> findByEstado(String estado) throws SQLException;

    void activate(Long id) throws SQLException;
}