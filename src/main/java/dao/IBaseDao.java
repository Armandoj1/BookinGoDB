
package dao;

import java.sql.SQLException;
import java.util.List;

public interface IBaseDao<T> {
    void create(T entity) throws SQLException;
    T read(Long id) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(Long id) throws SQLException;
    List<T> findAll() throws SQLException;
}
