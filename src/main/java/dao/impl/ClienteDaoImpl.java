
package dao.impl;
import ConexionBase.Conexion;
import model.Cliente;
import dao.IClienteDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;



public class ClienteDaoImpl implements IClienteDao{

        private final Conexion conexion;  // Dependencia inyectada para obtener conexiones

    // Constructor con inyección de dependencia
    public ClienteDaoImpl(Conexion conexion) {
        this.conexion = conexion;
    }

    @Override
    public void create(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO CLIENTE (primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, " +
                     "tipo_documento, documento, estado, email, telefono, categoria) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getPrimerNombre());
            stmt.setString(2, cliente.getSegundoNombre());
            stmt.setString(3, cliente.getPrimerApellido());
            stmt.setString(4, cliente.getSegundoApellido());
            stmt.setString(5, cliente.getTipoDocumento());
            stmt.setString(6, cliente.getDocumento());
            stmt.setString(7, cliente.getEstado());
            stmt.setString(8, cliente.getEmail());
            stmt.setLong(9, cliente.getTelefono());
            stmt.setString(10, cliente.getCategoria());

            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    throw new SQLException("El documento o datos únicos del cliente ya existen.", e);
                }
                throw e;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdCliente(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public Long createUsingProcedure(Cliente cliente) throws SQLException {
        // Llamada al procedimiento almacenado PR_CLIENTE_CREATE según el ejemplo del usuario
        // Parámetros esperados:
        // 1: @p_primer_nombre (VARCHAR)
        // 2: @p_segundo_nombre (VARCHAR, puede ser NULL)
        // 3: @p_primer_apellido (VARCHAR)
        // 4: @p_segundo_apellido (VARCHAR)
        // 5: @p_tipo_documento (VARCHAR)
        // 6: @p_documento (VARCHAR)
        // 7: @p_telefono (VARCHAR)
        // 8: @p_email (VARCHAR)
        // 9: @p_estado_persona (VARCHAR)
        // 10: @p_categoria (VARCHAR)
        // 11: @p_id_cliente OUTPUT (INT)

        String call = "{ call PR_CLIENTE_CREATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        try (Connection conn = conexion.connect();
             java.sql.CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.setString(1, cliente.getPrimerNombre());
            cstmt.setString(2, cliente.getSegundoNombre());
            cstmt.setString(3, cliente.getPrimerApellido());
            cstmt.setString(4, cliente.getSegundoApellido());
            cstmt.setString(5, cliente.getTipoDocumento());
            cstmt.setString(6, cliente.getDocumento());
            // Teléfono como String para alinearse con el SP
            cstmt.setString(7, String.valueOf(cliente.getTelefono()));
            cstmt.setString(8, cliente.getEmail());
            cstmt.setString(9, cliente.getEstado() == null ? "ACTIVO" : cliente.getEstado());
            cstmt.setString(10, cliente.getCategoria());

            cstmt.registerOutParameter(11, java.sql.Types.INTEGER);
            try {
                cstmt.execute();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    // Mensaje amigable para el usuario ante violaciones de UNIQUE/PK
                    throw new SQLException("Documento, email o teléfono ya registrados en el sistema. Corrige los datos e intenta nuevamente.", e);
                }
                throw e;
            }

            long nuevoId = cstmt.getLong(11);
            cliente.setIdCliente(nuevoId);
            return nuevoId;
        }
    }

    @Override
    public Cliente read(Long id) throws SQLException {
        String sql = "SELECT * FROM CLIENTE WHERE id_cliente = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void update(Cliente cliente) throws SQLException {
        // Actualizamos los datos personales en PERSONA usando el id_cliente para localizar la fila relacionada
        String sqlPersona = "UPDATE p SET p.primer_nombre = ?, p.segundo_nombre = ?, p.primer_apellido = ?, p.segundo_apellido = ?, " +
                "p.tipo_documento = ?, p.documento = ?, p.estado = ?, p.email = ?, p.telefono = ? " +
                "FROM PERSONA p INNER JOIN CLIENTE c ON p.id_persona = c.id_persona WHERE c.id_cliente = ?";

        // Actualizamos datos propios de CLIENTE (por ahora, categoría)
        String sqlCliente = "UPDATE CLIENTE SET categoria = ? WHERE id_cliente = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmtPersona = conn.prepareStatement(sqlPersona);
             PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {

            // PERSONA
            stmtPersona.setString(1, cliente.getPrimerNombre());
            stmtPersona.setString(2, cliente.getSegundoNombre());
            stmtPersona.setString(3, cliente.getPrimerApellido());
            stmtPersona.setString(4, cliente.getSegundoApellido());
            stmtPersona.setString(5, cliente.getTipoDocumento());
            stmtPersona.setString(6, cliente.getDocumento());
            stmtPersona.setString(7, cliente.getEstado());
            stmtPersona.setString(8, cliente.getEmail());
            stmtPersona.setLong(9, cliente.getTelefono());
            stmtPersona.setLong(10, cliente.getIdCliente());
            try {
                stmtPersona.executeUpdate();
            } catch (SQLException e) {
                if (isDuplicateKey(e)) {
                    throw new SQLException("El documento ya está registrado en otra persona.", e);
                }
                throw e;
            }

            // CLIENTE
            stmtCliente.setString(1, cliente.getCategoria());
            stmtCliente.setLong(2, cliente.getIdCliente());
            stmtCliente.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        // Borrado lógico a nivel PERSONA: desactivar por id_cliente
        // Actualiza PERSONA.estado haciendo JOIN con clientes para ubicar la persona
        String sql = "UPDATE p SET p.estado = 'INACTIVO' " +
                     "FROM PERSONA p INNER JOIN CLIENTE c ON p.id_persona = c.id_persona " +
                     "WHERE c.id_cliente = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void activate(Long id) throws SQLException {
        // Activación lógica a nivel PERSONA: activar por id_cliente
        String sql = "UPDATE p SET p.estado = 'ACTIVO' " +
                     "FROM PERSONA p INNER JOIN CLIENTE c ON p.id_persona = c.id_persona " +
                     "WHERE c.id_cliente = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Cliente> findAll() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT " +
                "c.id_cliente, c.categoria, " +
                "p.id_persona, p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.estado, p.email, p.telefono " +
                "FROM CLIENTE c INNER JOIN PERSONA p ON c.id_persona = p.id_persona";

        try (Connection conn = conexion.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        }
        return clientes;
    }

    @Override
    public Cliente findByDocumento(String documento) throws SQLException {
        String sql = "SELECT " +
                "c.id_cliente, c.categoria, " +
                "p.id_persona, p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.estado, p.email, p.telefono " +
                "FROM CLIENTE c " +
                "INNER JOIN PERSONA p ON c.id_persona = p.id_persona " +
                "WHERE p.documento = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, documento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Cliente findByEmail(String email) throws SQLException {
        String sql = "SELECT " +
                "c.id_cliente, c.categoria, " +
                "p.id_persona, p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.estado, p.email, p.telefono " +
                "FROM CLIENTE c " +
                "INNER JOIN PERSONA p ON c.id_persona = p.id_persona " +
                "WHERE p.email = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Cliente findByTelefono(long telefono) throws SQLException {
        String sql = "SELECT " +
                "c.id_cliente, c.categoria, " +
                "p.id_persona, p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.estado, p.email, p.telefono " +
                "FROM CLIENTE c " +
                "INNER JOIN PERSONA p ON c.id_persona = p.id_persona " +
                "WHERE p.telefono = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, telefono);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cliente> findByEstado(String estado) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT " +
                "c.id_cliente, c.categoria, " +
                "p.id_persona, p.primer_nombre, p.segundo_nombre, p.primer_apellido, p.segundo_apellido, " +
                "p.tipo_documento, p.documento, p.estado, p.email, p.telefono " +
                "FROM CLIENTE c INNER JOIN PERSONA p ON c.id_persona = p.id_persona " +
                "WHERE p.estado = ?";

        try (Connection conn = conexion.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
        }
        return clientes;
    }

    // Método privado de mapeo: responsabilidad interna del DAO
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setPrimerNombre(rs.getString("primer_nombre"));
        cliente.setSegundoNombre(rs.getString("segundo_nombre"));
        cliente.setPrimerApellido(rs.getString("primer_apellido"));
        cliente.setSegundoApellido(rs.getString("segundo_apellido"));
        cliente.setTipoDocumento(rs.getString("tipo_documento"));
        cliente.setDocumento(rs.getString("documento"));
        cliente.setEstado(rs.getString("estado"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getLong("telefono"));
        cliente.setCategoria(rs.getString("categoria"));
        return cliente;
    }

    private boolean isDuplicateKey(SQLException e) {
        int code = e.getErrorCode();
        String state = e.getSQLState();
        return code == 2601 || code == 2627 || (state != null && state.startsWith("23"));
    }
}
    

