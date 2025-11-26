package service;

import model.Huesped;

import java.sql.SQLException;
import java.util.List;

public interface IHuespedService {

    void crearPersona(Huesped huesped) throws SQLException;

    List<Huesped> listarTodos() throws SQLException;
}