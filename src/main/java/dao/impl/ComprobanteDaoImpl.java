
package dao.impl;

import ConexionBase.Conexion;
import model.Comprobante;
import model.Reserva;
import dao.IComprobanteDao;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ComprobanteDaoImpl implements IComprobanteDao {

 private final Conexion conexion;

    public ComprobanteDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Comprobante comprobante) throws SQLException {
        String sql = "INSERT INTO COMPROBANTE (ID_RESERVA, TIPO_COMPROBANTE, FECHA_EMISION, " +
                "MONTO_TOTAL, METODO_PAGO, DESCRIPCION, ESTADO) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, comprobante.getReserva().getIdReserva());
            stmt.setString(2, comprobante.getTipoComprobante());
            stmt.setDate(3, Date.valueOf(comprobante.getFechaEmision()));
            stmt.setDouble(4, comprobante.getMontoTotal());
            stmt.setString(5, comprobante.getMetodoPago());
            stmt.setString(6, comprobante.getDescripcion());
            stmt.setString(7, comprobante.getEstado());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    comprobante.setIdComprobante(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Comprobante read(Long id) throws SQLException {
        String sql = "SELECT * FROM COMPROBANTE WHERE ID_COMPROBANTE = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Comprobante comprobante) throws SQLException {
        String sql = "UPDATE COMPROBANTE SET ID_RESERVA = ?, TIPO_COMPROBANTE = ?, " +
                "FECHA_EMISION = ?, MONTO_TOTAL = ?, METODO_PAGO = ?, DESCRIPCION = ?, " +
                "ESTADO = ? WHERE ID_COMPROBANTE = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, comprobante.getReserva().getIdReserva());
            stmt.setString(2, comprobante.getTipoComprobante());
            stmt.setDate(3, Date.valueOf(comprobante.getFechaEmision()));
            stmt.setDouble(4, comprobante.getMontoTotal());
            stmt.setString(5, comprobante.getMetodoPago());
            stmt.setString(6, comprobante.getDescripcion());
            stmt.setString(7, comprobante.getEstado());
            stmt.setLong(8, comprobante.getIdComprobante());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM COMPROBANTE WHERE ID_COMPROBANTE = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Comprobante> findAll() throws SQLException {
        List<Comprobante> comprobantes = new ArrayList<>();
        String sql = "SELECT * FROM COMPROBANTE ORDER BY ID_COMPROBANTE DESC";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                comprobantes.add(mapResultSetToEntity(rs));
            }
        }

        return comprobantes;
    }

    @Override
    public List<Comprobante> findByReserva(Long idReserva) throws SQLException {
        List<Comprobante> comprobantes = new ArrayList<>();
        String sql = "SELECT * FROM COMPROBANTE WHERE ID_RESERVA = ? ORDER BY FECHA_EMISION DESC";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idReserva);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comprobantes.add(mapResultSetToEntity(rs));
                }
            }
        }

        return comprobantes;
    }

    @Override
    public List<Comprobante> findByEstado(String estado) throws SQLException {
        List<Comprobante> comprobantes = new ArrayList<>();
        String sql = "SELECT * FROM COMPROBANTE WHERE ESTADO = ? ORDER BY FECHA_EMISION DESC";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comprobantes.add(mapResultSetToEntity(rs));
                }
            }
        }

        return comprobantes;
    }

    private Comprobante mapResultSetToEntity(ResultSet rs) throws SQLException {
        Comprobante comprobante = new Comprobante();
        comprobante.setIdComprobante(rs.getLong("ID_COMPROBANTE"));

        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getLong("ID_RESERVA"));
        comprobante.setReserva(reserva);

        comprobante.setTipoComprobante(rs.getString("TIPO_COMPROBANTE"));
        comprobante.setFechaEmision(rs.getDate("FECHA_EMISION").toLocalDate());
        comprobante.setMontoTotal(rs.getDouble("MONTO_TOTAL"));
        comprobante.setMetodoPago(rs.getString("METODO_PAGO"));
        comprobante.setDescripcion(rs.getString("DESCRIPCION"));
        comprobante.setEstado(rs.getString("ESTADO"));

        return comprobante;
    }
}