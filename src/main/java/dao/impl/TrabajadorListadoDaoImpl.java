package dao.impl;

import ConexionBase.Conexion;
import dao.ITrabajadorListadoDao;
import model.Rol;
import model.Trabajador;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación para listar trabajadores desde la función FN_GET_TRABAJADORES()
 * Columnas esperadas: ID, NombreCompleto, Usuario, Rol, Sueldo, estado
 */
public class TrabajadorListadoDaoImpl implements ITrabajadorListadoDao {

    private final Conexion conexion;

    public TrabajadorListadoDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Trabajador> findAll() throws SQLException {
        List<Trabajador> trabajadores = new ArrayList<>();
        String sql = "SELECT * FROM FN_GET_TRABAJADORES();";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trabajador t = new Trabajador();
                t.setIdTrabajador(rs.getLong("ID"));
                // Asignar partes del nombre para que getNombreCompleto funcione
                try { t.setPrimerNombre(rs.getString("primer_nombre")); } catch (SQLException ignored) {}
                try { t.setSegundoNombre(rs.getString("segundo_nombre")); } catch (SQLException ignored) {}
                try { t.setPrimerApellido(rs.getString("primer_apellido")); } catch (SQLException ignored) {}
                try { t.setSegundoApellido(rs.getString("segundo_apellido")); } catch (SQLException ignored) {}

                t.setUsuario(rs.getString("Usuario"));
                t.setSueldo(rs.getDouble("Sueldo"));
                t.setEstado(rs.getString("estado"));

                Rol rol = new Rol();
                rol.setNombreRol(rs.getString("Rol"));
                t.setRol(rol);

                trabajadores.add(t);
            }
        }

        return trabajadores;
    }
}