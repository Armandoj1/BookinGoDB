
package dao.impl;

import ConexionBase.Conexion;
import model.Habitacion;
import dao.IHabitacionDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDaoImpl implements IHabitacionDao {

    private final Conexion conexion;

    public HabitacionDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Habitacion habitacion) throws SQLException {
        String sql = "INSERT INTO HABITACION " +
                "(numero, tipo_habitacion, precio, estado, " +
                "capacidad, piso, descripcion, caracteristicas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, habitacion.getNumero());
            stmt.setString(2, habitacion.getTipoHabitacion());
            stmt.setDouble(3, habitacion.getPrecio());
            stmt.setString(4, habitacion.getEstado());
            stmt.setInt(5, habitacion.getCapacidad());
            stmt.setInt(6, habitacion.getPiso());
            stmt.setString(7, habitacion.getDescripcion());
            stmt.setString(8, habitacion.getCaracteristicas());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    habitacion.setIdHabitacion(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Habitacion read(Long id) throws SQLException {
        String sql = "SELECT * FROM HABITACION WHERE id_habitacion = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHabitacion(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Habitacion habitacion) throws SQLException {
        String sql = "UPDATE HABITACION SET " +
                "numero = ?, tipo_habitacion = ?, precio = ?, " +
                "estado = ?, capacidad = ?, piso = ?, " +
                "descripcion = ?, caracteristicas = ? " +
                "WHERE id_habitacion = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, habitacion.getNumero());
            stmt.setString(2, habitacion.getTipoHabitacion());
            stmt.setDouble(3, habitacion.getPrecio());
            stmt.setString(4, habitacion.getEstado());
            stmt.setInt(5, habitacion.getCapacidad());
            stmt.setInt(6, habitacion.getPiso());
            stmt.setString(7, habitacion.getDescripcion());
            stmt.setString(8, habitacion.getCaracteristicas());
            stmt.setLong(9, habitacion.getIdHabitacion());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM HABITACION WHERE id_habitacion = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Habitacion> findAll() throws SQLException {
        List<Habitacion> HABITACION = new ArrayList<>();
        String sql = "SELECT * FROM HABITACION ORDER BY id_habitacion DESC";

        try (Connection conn = conexion.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                HABITACION.add(mapResultSetToHabitacion(rs));
            }
        }

        return HABITACION;
    }

    @Override
    public List<Habitacion> findByEstado(String estado) throws SQLException {
        List<Habitacion> HABITACION = new ArrayList<>();
        String sql = "SELECT * FROM HABITACION WHERE UPPER(estado) = UPPER(?)";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HABITACION.add(mapResultSetToHabitacion(rs));
                }
            }
        }

        return HABITACION;
    }

    @Override
    public List<Habitacion> findByTipo(String tipo) throws SQLException {
        List<Habitacion> HABITACION = new ArrayList<>();
        String sql = "SELECT * FROM HABITACION WHERE tipo_habitacion = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HABITACION.add(mapResultSetToHabitacion(rs));
                }
            }
        }

        return HABITACION;
    }

    @Override
    public Habitacion findByNumero(String numero) throws SQLException {
        String sql = "SELECT * FROM HABITACION WHERE numero = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHabitacion(rs);
                }
            }
        }

        return null;
    }

    @Override
    public int countByEstado(String estado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HABITACION WHERE estado = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private Habitacion mapResultSetToHabitacion(ResultSet rs) throws SQLException {
        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(rs.getLong("id_habitacion"));
        habitacion.setNumero(rs.getString("numero"));
        habitacion.setTipoHabitacion(rs.getString("tipo_habitacion"));
        habitacion.setPrecio(rs.getDouble("precio"));
        habitacion.setEstado(rs.getString("estado"));
        habitacion.setCapacidad(rs.getInt("capacidad"));
        habitacion.setPiso(rs.getInt("piso"));
        habitacion.setDescripcion(rs.getString("descripcion"));
        habitacion.setCaracteristicas(rs.getString("caracteristicas"));
        return habitacion;
    }
}
