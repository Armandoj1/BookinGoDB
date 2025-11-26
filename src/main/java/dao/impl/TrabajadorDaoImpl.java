
package dao.impl;

import ConexionBase.Conexion;
import model.Rol;
import model.Trabajador;
import dao.ITrabajadorDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TrabajadorDaoImpl implements ITrabajadorDao {

    private final Conexion conexion;

    public TrabajadorDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Trabajador trabajador) throws SQLException {
        String sql = "INSERT INTO TRABAJADOR (primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, " +
                "tipo_documento, documento, estado, email, telefono, usuario, contrasena, sueldo, id_rol) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, trabajador.getPrimerNombre());
            stmt.setString(2, trabajador.getSegundoNombre());
            stmt.setString(3, trabajador.getPrimerApellido());
            stmt.setString(4, trabajador.getSegundoApellido());
            stmt.setString(5, trabajador.getTipoDocumento());
            stmt.setString(6, trabajador.getDocumento());
            stmt.setString(7, trabajador.getEstado());
            stmt.setString(8, trabajador.getEmail());
            stmt.setLong(9, trabajador.getTelefono());
            stmt.setString(10, trabajador.getUsuario());
            stmt.setString(11, trabajador.getContrasena());
            stmt.setDouble(12, trabajador.getSueldo());
            stmt.setLong(13, trabajador.getRol().getIdRol());

            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    throw new SQLException("Documento, usuario o datos únicos ya registrados.", e);
                }
                throw e;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    trabajador.setIdTrabajador(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Long createUsingProcedure(Trabajador trabajador) throws SQLException {
        String call = "{ call PR_TRABAJADOR_CREATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        try (Connection conn = conexion.connect();
             java.sql.CallableStatement cstmt = conn.prepareCall(call)) {

            // Orden según el ejemplo proporcionado por el usuario
            cstmt.setString(1, trabajador.getPrimerNombre());
            cstmt.setString(2, trabajador.getSegundoNombre());
            cstmt.setString(3, trabajador.getPrimerApellido());
            cstmt.setString(4, trabajador.getSegundoApellido());
            cstmt.setString(5, trabajador.getTipoDocumento());
            cstmt.setString(6, trabajador.getDocumento());
            // Teléfono como String para ser flexible con el SP
            cstmt.setString(7, String.valueOf(trabajador.getTelefono()));
            cstmt.setString(8, trabajador.getEmail());
            cstmt.setString(9, trabajador.getEstado() == null ? "ACTIVO" : trabajador.getEstado());

            cstmt.setLong(10, trabajador.getRol().getIdRol());
            cstmt.setString(11, trabajador.getUsuario());
            cstmt.setString(12, trabajador.getContrasena());
            cstmt.setDouble(13, trabajador.getSueldo());

            cstmt.registerOutParameter(14, java.sql.Types.INTEGER);
            cstmt.execute();

            long nuevoId = cstmt.getLong(14);
            trabajador.setIdTrabajador(nuevoId);
            return nuevoId;
        }
    }

    @Override
    public Trabajador read(Long id) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE t.id_trabajador = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }

        return null;
    }

    @Override
    public void update(Trabajador trabajador) throws SQLException {
        // Actualizar datos personales en PERSONA (usando id_trabajador para localizar la persona)
        String sqlPersona = "UPDATE p SET p.primer_nombre = ?, p.segundo_nombre = ?, p.primer_apellido = ?, p.segundo_apellido = ?, " +
                "p.tipo_documento = ?, p.documento = ?, p.estado = ?, p.email = ?, p.telefono = ? " +
                "FROM PERSONA p INNER JOIN TRABAJADOR t ON p.id_persona = t.id_persona WHERE t.id_trabajador = ?";

        // Actualizar datos propios de TRABAJADOR (usuario, contraseña, sueldo, rol)
        String sqlTrabajador = "UPDATE TRABAJADOR SET usuario = ?, contrasena = ?, sueldo = ?, id_rol = ? WHERE id_trabajador = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmtPersona = conn.prepareStatement(sqlPersona);
                PreparedStatement stmtTrabajador = conn.prepareStatement(sqlTrabajador)) {

            // PERSONA
            stmtPersona.setString(1, trabajador.getPrimerNombre());
            stmtPersona.setString(2, trabajador.getSegundoNombre());
            stmtPersona.setString(3, trabajador.getPrimerApellido());
            stmtPersona.setString(4, trabajador.getSegundoApellido());
            stmtPersona.setString(5, trabajador.getTipoDocumento());
            stmtPersona.setString(6, trabajador.getDocumento());
            stmtPersona.setString(7, trabajador.getEstado());
            stmtPersona.setString(8, trabajador.getEmail());
            stmtPersona.setLong(9, trabajador.getTelefono());
            stmtPersona.setLong(10, trabajador.getIdTrabajador());
            try {
                stmtPersona.executeUpdate();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    throw new SQLException("El documento ya está asociado a otra persona.", e);
                }
                throw e;
            }

            // TRABAJADOR
            stmtTrabajador.setString(1, trabajador.getUsuario());
            stmtTrabajador.setString(2, trabajador.getContrasena());
            stmtTrabajador.setDouble(3, trabajador.getSueldo());
            stmtTrabajador.setLong(4, trabajador.getRol().getIdRol());
            stmtTrabajador.setLong(5, trabajador.getIdTrabajador());
            try {
                stmtTrabajador.executeUpdate();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    throw new SQLException("El usuario ya existe para otro trabajador.", e);
                }
                throw e;
            }
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        // Borrado lógico a nivel PERSONA: desactivar por id_trabajador
        // Actualiza PERSONA.estado haciendo JOIN con TRABAJADOR para ubicar la persona
        String sql = "UPDATE p SET p.estado = 'INACTIVO' " +
                     "FROM PERSONA p INNER JOIN TRABAJADOR t ON p.id_persona = t.id_persona " +
                     "WHERE t.id_trabajador = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void activate(Long id) throws SQLException {
        // Activación lógica a nivel PERSONA: activar por id_trabajador
        String sql = "UPDATE p SET p.estado = 'ACTIVO' " +
                     "FROM PERSONA p INNER JOIN TRABAJADOR t ON p.id_persona = t.id_persona " +
                     "WHERE t.id_trabajador = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Trabajador> findAll() throws SQLException {
        List<Trabajador> lista = new ArrayList<>();

        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona";

        try (Connection conn = conexion.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapResultSetToTrabajador(rs));
            }
        }

        return lista;
    }

    @Override
    public Trabajador findByUsuario(String usuario) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE t.usuario = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }

        return null;
    }

    @Override
    public Trabajador findByDocumento(String documento) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE p.documento = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Trabajador findByEmail(String email) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE p.email = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Trabajador findByTelefono(long telefono) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE p.telefono = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Trabajador authenticate(String usuario, String contrasena) throws SQLException {
        String sql = "SELECT " +
                "t.id_trabajador, t.usuario, t.contrasena, t.sueldo, " +
                "r.id_rol, r.nombre_rol, r.descripcion, " +
                "p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.email, p.telefono, p.estado AS estado " +
                "FROM TRABAJADOR t " +
                "LEFT JOIN ROL r ON t.id_rol = r.id_rol " +
                "LEFT JOIN PERSONA p ON t.id_persona = p.id_persona " +
                "WHERE t.usuario = ? AND t.contrasena = ? AND p.estado = 'ACTIVO'";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrabajador(rs);
                }
            }
        }

        return null;
    }

    @Override
    public int countByRol(Long idRol) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TRABAJADOR WHERE id_rol = ?";

        try (Connection conn = conexion.connect();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private Trabajador mapResultSetToTrabajador(ResultSet rs) throws SQLException {
        Trabajador t = new Trabajador();
        t.setIdTrabajador(rs.getLong("id_trabajador"));
        t.setPrimerNombre(rs.getString("primer_nombre"));
        t.setSegundoNombre(rs.getString("segundo_nombre"));
        t.setPrimerApellido(rs.getString("primer_apellido"));
        t.setSegundoApellido(rs.getString("segundo_apellido"));
        t.setTipoDocumento(rs.getString("tipo_documento"));
        t.setDocumento(rs.getString("documento"));
        t.setEstado(rs.getString("estado"));
        t.setEmail(rs.getString("email"));
        t.setTelefono(rs.getLong("telefono"));
        t.setUsuario(rs.getString("usuario"));
        t.setContrasena(rs.getString("contrasena"));
        t.setSueldo(rs.getDouble("sueldo"));

        Rol rol = new Rol();
        rol.setIdRol(rs.getLong("id_rol"));
        rol.setNombreRol(rs.getString("nombre_rol"));
        rol.setDescripcion(rs.getString("descripcion"));
        t.setRol(rol);

        return t;
    }

    private boolean isDuplicateKey(SQLException e) {
        int code = e.getErrorCode();
        String state = e.getSQLState();
        return code == 2601 || code == 2627 || (state != null && state.startsWith("23"));
    }
}
