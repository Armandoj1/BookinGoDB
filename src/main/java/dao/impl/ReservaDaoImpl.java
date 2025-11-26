
package dao.impl;

import ConexionBase.Conexion;
import model.Cliente;
import model.Habitacion;
import model.Reserva;
import model.Trabajador;
import dao.IReservaDao;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDaoImpl implements IReservaDao {

    private final Conexion conexion;

    public ReservaDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO RESERVA (id_cliente, id_trabajador, id_habitacion, " +
                "fecha_reserva, fecha_entrada, fecha_salida, tipo_reserva, observacion, estado, total) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, reserva.getCliente().getIdCliente());
            stmt.setLong(2, reserva.getTrabajador().getIdTrabajador());
            stmt.setLong(3, reserva.getHabitacion().getIdHabitacion());
            stmt.setDate(4, Date.valueOf(reserva.getFechaReserva()));
            stmt.setDate(5, Date.valueOf(reserva.getFechaEntrada()));
            stmt.setDate(6, Date.valueOf(reserva.getFechaSalida()));
            stmt.setString(7, reserva.getTipoReserva());
            stmt.setString(8, reserva.getObservacion());
            stmt.setString(9, reserva.getEstado());
            stmt.setDouble(10, reserva.getTotal());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    reserva.setIdReserva(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Reserva read(Long id) throws SQLException {
        String sql = "SELECT r.*, " +
                "c.id_cliente, c.primer_nombre AS c_primer_nombre, c.primer_apellido AS c_primer_apellido, " +
                "h.id_habitacion, h.numero, h.tipo_habitacion, h.precio " +
                "FROM RESERVA r " +
                "JOIN CLIENTE c ON r.id_cliente = c.id_cliente " +
                "JOIN HABITACION h ON r.id_habitacion = h.id_habitacion " +
                "WHERE r.id_reserva = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReserva(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Reserva reserva) throws SQLException {
        String sql = "UPDATE RESERVA SET id_cliente = ?, id_trabajador = ?, id_habitacion = ?, " +
                "fecha_reserva = ?, fecha_entrada = ?, fecha_salida = ?, tipo_reserva = ?, " +
                "observacion = ?, estado = ?, total = ? WHERE id_reserva = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, reserva.getCliente().getIdCliente());
            stmt.setLong(2, reserva.getTrabajador().getIdTrabajador());
            stmt.setLong(3, reserva.getHabitacion().getIdHabitacion());
            stmt.setDate(4, Date.valueOf(reserva.getFechaReserva()));
            stmt.setDate(5, Date.valueOf(reserva.getFechaEntrada()));
            stmt.setDate(6, Date.valueOf(reserva.getFechaSalida()));
            stmt.setString(7, reserva.getTipoReserva());
            stmt.setString(8, reserva.getObservacion());
            stmt.setString(9, reserva.getEstado());
            stmt.setDouble(10, reserva.getTotal());
            stmt.setLong(11, reserva.getIdReserva());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM RESERVA WHERE id_reserva = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Reserva> findAll() throws SQLException {
        List<Reserva> reservas = new ArrayList<>();

        // Reemplazar dependencia de función SQL por un SELECT estándar con JOINs
        String sql = "SELECT r.*, c.documento, c.primer_nombre AS c_primer_nombre, c.primer_apellido AS c_primer_apellido, " +
                "h.id_habitacion, h.numero, h.tipo_habitacion, h.precio " +
                "FROM RESERVA r " +
                "JOIN CLIENTE c ON r.id_cliente = c.id_cliente " +
                "JOIN HABITACION h ON r.id_habitacion = h.id_habitacion";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Usamos el mapeo básico que ya contempla documento si está presente
                reservas.add(mapResultSetToReservaBasic(rs));
            }
        } catch (SQLException e) {
            // Fallback: intentar un SELECT simple por si existen diferencias en esquema
            String fallbackSql = "SELECT * FROM RESERVA";
            try (Connection conn = conexion.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(fallbackSql)) {
                while (rs.next()) {
                    reservas.add(mapResultSetToReservaBasic(rs));
                }
            }
        }

        return reservas;
    }

    @Override
    public List<Reserva> findByCliente(Long idCliente) throws SQLException {
        List<Reserva> RESERVA = new ArrayList<>();

        String sql = "SELECT r.*, " +
                "c.id_cliente, c.primer_nombre AS c_primer_nombre, c.primer_apellido AS c_primer_apellido, " +
                "h.id_habitacion, h.numero, h.tipo_habitacion, h.precio " +
                "FROM RESERVA r " +
                "JOIN CLIENTE c ON r.id_cliente = c.id_cliente " +
                "JOIN HABITACION h ON r.id_habitacion = h.id_habitacion " +
                "WHERE r.id_cliente = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RESERVA.add(mapResultSetToReserva(rs));
                }
            }
        }

        return RESERVA;
    }

    @Override
    public List<Reserva> findByEstado(String estado) throws SQLException {
        List<Reserva> RESERVA = new ArrayList<>();

        String sql = "SELECT r.*, " +
                "c.id_cliente, c.primer_nombre AS c_primer_nombre, c.primer_apellido AS c_primer_apellido, " +
                "h.id_habitacion, h.numero, h.tipo_habitacion, h.precio " +
                "FROM RESERVA r " +
                "JOIN CLIENTE c ON r.id_cliente = c.id_cliente " +
                "JOIN HABITACION h ON r.id_habitacion = h.id_habitacion " +
                "WHERE r.estado = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RESERVA.add(mapResultSetToReserva(rs));
                }
            }
        }

        return RESERVA;
    }

    @Override
    public List<Reserva> findByFecha(LocalDate fecha) throws SQLException {
        List<Reserva> RESERVA = new ArrayList<>();

        String sql = "SELECT r.*, " +
                "c.id_cliente, c.primer_nombre AS c_primer_nombre, c.primer_apellido AS c_primer_apellido, " +
                "h.id_habitacion, h.numero, h.tipo_habitacion, h.precio " +
                "FROM RESERVA r " +
                "JOIN CLIENTE c ON r.id_cliente = c.id_cliente " +
                "JOIN HABITACION h ON r.id_habitacion = h.id_habitacion " +
                "WHERE r.fecha_reserva = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RESERVA.add(mapResultSetToReserva(rs));
                }
            }
        }

        return RESERVA;
    }

    @Override
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM RESERVA";
        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countByEstado(String estado) throws SQLException {
        String sql = "SELECT COUNT(*) FROM RESERVA WHERE UPPER(estado) = UPPER(?)";

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

    // Mapea los resultados SQL a un objeto Reserva completo (cuando hay JOINs)
    private Reserva mapResultSetToReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getLong("id_reserva"));
        reserva.setFechaReserva(rs.getDate("fecha_reserva").toLocalDate());
        reserva.setFechaEntrada(rs.getDate("fecha_entrada").toLocalDate());
        reserva.setFechaSalida(rs.getDate("fecha_salida").toLocalDate());
        reserva.setTipoReserva(rs.getString("tipo_reserva"));
        reserva.setObservacion(rs.getString("observacion"));
        reserva.setEstado(rs.getString("estado"));
        reserva.setTotal(rs.getDouble("total"));

        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setPrimerNombre(rs.getString("c_primer_nombre"));
        cliente.setPrimerApellido(rs.getString("c_primer_apellido"));
        reserva.setCliente(cliente);

        Habitacion habitacion = new Habitacion();
        habitacion.setIdHabitacion(rs.getLong("id_habitacion"));
        habitacion.setNumero(rs.getString("numero"));
        habitacion.setTipoHabitacion(rs.getString("tipo_habitacion"));
        habitacion.setPrecio(rs.getDouble("precio"));
        reserva.setHabitacion(habitacion);

        // OJO: Trabajador no está en el SELECT.
        // Si necesitas setearlo, debes hacer el JOIN y mapearlo aquí.

        return reserva;
    }

    // Mapeo básico para SELECT * FROM RESERVA (sin JOINs)
    private Reserva mapResultSetToReservaBasic(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getLong("id_reserva"));
        reserva.setFechaReserva(rs.getDate("fecha_reserva").toLocalDate());
        reserva.setFechaEntrada(rs.getDate("fecha_entrada").toLocalDate());
        reserva.setFechaSalida(rs.getDate("fecha_salida").toLocalDate());
        reserva.setTipoReserva(rs.getString("tipo_reserva"));
        reserva.setObservacion(rs.getString("observacion"));
        reserva.setEstado(rs.getString("estado"));
        reserva.setTotal(rs.getDouble("total"));

        Cliente cliente = new Cliente();
        // id_cliente puede no estar en la función; si no existe, evitar excepción y usar 0L
        if (hasColumn(rs, "id_cliente")) {
            cliente.setIdCliente(rs.getLong("id_cliente"));
        } else {
            cliente.setIdCliente(0L);
        }
        // Si la función FN_GET_RESERVAS_CON_DOCUMENTO() provee el documento del cliente, lo mapeamos
        if (hasColumn(rs, "documento")) {
            cliente.setDocumento(rs.getString("documento"));
        }
        reserva.setCliente(cliente);

        Habitacion habitacion = new Habitacion();
        if (hasColumn(rs, "id_habitacion")) {
            habitacion.setIdHabitacion(rs.getLong("id_habitacion"));
        } else {
            habitacion.setIdHabitacion(0L);
        }
        reserva.setHabitacion(habitacion);

        Trabajador trabajador = new Trabajador();
        if (hasColumn(rs, "id_trabajador")) {
            trabajador.setIdTrabajador(rs.getLong("id_trabajador"));
        }
        reserva.setTrabajador(trabajador);

        return reserva;
    }

    // Helper para verificar si un ResultSet contiene una columna (case-insensitive)
    private boolean hasColumn(ResultSet rs, String column) {
        try {
            var md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                String label = md.getColumnLabel(i);
                if (label != null && label.equalsIgnoreCase(column)) {
                    return true;
                }
            }
        } catch (SQLException ignored) {
        }
        return false;
    }

    /**
     * Actualiza a FINALIZADA las reservas en curso cuyo tiempo haya terminado.
     * Regla: R.estado = 'EN_CURSO' y R.fecha_salida <= GETDATE() (SQL Server).
     */
    public void finalizarReservasVencidas() throws SQLException {
        String sql = "UPDATE R " +
                "SET estado = 'FINALIZADA' " +
                "FROM RESERVA R " +
                "INNER JOIN HABITACION H ON H.id_habitacion = R.id_habitacion " +
                "WHERE R.estado = 'EN_CURSO' " +
                "  AND R.fecha_salida <= GETDATE();";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    /**
     * Libera las habitaciones asociadas a reservas finalizadas, poniendo su estado en DISPONIBLE.
     */
    public void liberarHabitacionesFinalizadas() throws SQLException {
        String sql = "UPDATE HABITACION " +
                "SET estado = 'DISPONIBLE' " +
                "WHERE id_habitacion IN ( " +
                "    SELECT id_habitacion FROM RESERVA WHERE estado = 'FINALIZADA' " +
                ");";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}
