package dao.impl;

import ConexionBase.Conexion;
import dao.IHuespedDao;
import model.Huesped;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para obtener huéspedes desde la función FN_GET_HUESPEDES()
 */
public class HuespedDaoImpl implements IHuespedDao {

    private final Conexion conexion;

    public HuespedDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void createPersona(Huesped huesped) throws SQLException {
        String sql = "INSERT INTO PERSONA (primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, " +
                "tipo_documento, documento, email, telefono, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, huesped.getPrimerNombre());
            stmt.setString(2, huesped.getSegundoNombre());
            stmt.setString(3, huesped.getPrimerApellido());
            stmt.setString(4, huesped.getSegundoApellido());
            stmt.setString(5, huesped.getTipoDocumento());
            stmt.setString(6, huesped.getDocumento());
            stmt.setString(7, huesped.getEmail());
            // Telefono se guarda como numérico; si viene como String, intentamos parsear
            long tel = 0L;
            try { tel = huesped.getTelefono() == null ? 0L : Long.parseLong(huesped.getTelefono().trim()); } catch (Exception ignored) {}
            stmt.setLong(8, tel);
            stmt.setString(9, huesped.getEstado() == null ? "ACTIVO" : huesped.getEstado());

            stmt.executeUpdate();

            try (java.sql.ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    // Asignar el ID generado de PERSONA al modelo de huésped
                    huesped.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public List<Huesped> findAll() throws SQLException {
        List<Huesped> huespedes = new ArrayList<>();
        String sql = "SELECT * FROM FN_GET_HUESPEDES();";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Huesped h = new Huesped();
                h.setId(rs.getLong("ID"));

                // Construir nombre completo desde columnas separadas
                String pNom = safeGet(rs, "primer_nombre");
                String sNom = safeGet(rs, "segundo_nombre");
                String pApe = safeGet(rs, "primer_apellido");
                String sApe = safeGet(rs, "segundo_apellido");
                h.setPrimerNombre(pNom);
                h.setSegundoNombre(sNom);
                h.setPrimerApellido(pApe);
                h.setSegundoApellido(sApe);
                String nombreCompleto = String.join(" ",
                        joinPart(pNom), joinPart(sNom), joinPart(pApe), joinPart(sApe)).trim();
                h.setNombreCompleto(nombreCompleto);

                // Documento: mantener solo el valor para búsquedas; TipoDocumento se puede usar en UI si se requiere
                String tipoDoc = safeGet(rs, "tipo_documento");
                if (tipoDoc == null) tipoDoc = safeGet(rs, "TipoDocumento");
                h.setTipoDocumento(tipoDoc);
                h.setDocumento(safeGet(rs, "Documento"));
                h.setEmail(safeGet(rs, "Email"));
                // Telefono podría ser numérico; lo convertimos a String para la vista
                String tel = safeGet(rs, "Telefono");
                if (tel == null) {
                    try { tel = String.valueOf(rs.getLong("Telefono")); } catch (SQLException ignored) { tel = null; }
                }
                h.setTelefono(tel);
                h.setCategoria(safeGet(rs, "Categoria"));
                h.setEstado(safeGet(rs, "Estado"));
                huespedes.add(h);
            }
        }

        return huespedes;
    }

    private String safeGet(ResultSet rs, String column) {
        try { return rs.getString(column); } catch (SQLException e) { return null; }
    }

    private String joinPart(String s) { return (s == null || s.isEmpty()) ? "" : s; }
}