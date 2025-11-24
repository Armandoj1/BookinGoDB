
package dao;

import model.Comprobante;
import java.sql.SQLException;
import java.util.List;


public interface IComprobanteDao extends IBaseDao<Comprobante> {
    List<Comprobante> findByReserva(Long idReserva) throws SQLException;
    List<Comprobante> findByEstado(String estado) throws SQLException;
}
