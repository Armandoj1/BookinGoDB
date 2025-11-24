
package dao.impl;

import ConexionBase.Conexion;
import dao.IRolDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Rol;


public class RolDaoImpl implements IRolDao {

  private final Conexion conexion;

    public RolDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Rol rol) throws SQLException {
        String sql = "INSERT INTO rol (nombre_rol, descripcion) VALUES (?, ?)";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    rol.setIdRol(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Rol read(Long id) throws SQLException {
        String sql = "SELECT * FROM rol WHERE id_rol = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRol(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Rol rol) throws SQLException {
        String sql = "UPDATE rol SET nombre_rol = ?, descripcion = ? WHERE id_rol = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.getNombreRol());
            stmt.setString(2, rol.getDescripcion());
            stmt.setLong(3, rol.getIdRol());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM rol WHERE id_rol = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Rol> findAll() throws SQLException {
        List<Rol> rol = new ArrayList<>();
        String sql = "SELECT * FROM rol ORDER BY id_rol ASC";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rol.add(mapResultSetToRol(rs));
            }
        }

        return rol;
    }

    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setIdRol(rs.getLong("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("descripcion"));
        return rol;
    }
}
