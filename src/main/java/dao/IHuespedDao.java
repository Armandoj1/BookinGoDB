package dao;

import model.Huesped;
import java.sql.SQLException;
import java.util.List;

public interface IHuespedDao {
    List<Huesped> findAll() throws SQLException;
    void createPersona(Huesped huesped) throws SQLException;
}