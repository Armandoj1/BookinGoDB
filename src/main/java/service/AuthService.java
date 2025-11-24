package service;

import ConexionBase.Conexion;
import ConexionBase.Database;
import model.Rol;
import model.Trabajador;

import java.sql.*;

public class AuthService {

    /**
     * Autentica a un trabajador usando el stored procedure
     * sp_login_trabajador_seguro
     * 
     * @param usuario    Usuario del trabajador
     * @param contrasena Contraseña del trabajador
     * @return Objeto Trabajador con todos los datos si la autenticación es exitosa,
     *         null si falla
     * @throws SQLException Si hay error en la base de datos
     */
    public Trabajador loginTrabajador(String usuario, String contrasena) throws SQLException {
        Database db = DatabaseConfig.getDatabase();
        Conexion conexion = new Conexion(db);

        try {
            Connection conn = conexion.connect();

            // Preparar la llamada al stored procedure
            String sql = "{CALL sp_login_trabajador_validado(?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setString(1, usuario);
            stmt.setString(2, contrasena);

            // Ejecutar el stored procedure
            ResultSet rs = stmt.executeQuery();

            // Si hay resultado, el login fue exitoso
            if (rs.next()) {
                Trabajador trabajador = new Trabajador();

                // Obtener datos del trabajador del resultado
                trabajador.setIdTrabajador(rs.getLong("id_trabajador"));
                trabajador.setUsuario(usuario);

                // Crear y asignar el rol
                Rol rol = new Rol();
                rol.setIdRol(rs.getLong("id_rol"));
                rol.setNombreRol(rs.getString("nombre_rol"));
                trabajador.setRol(rol);

                // Obtener datos de persona
                trabajador.setNombre(rs.getString("primer_nombre"));
                trabajador.setPrimerApellido(rs.getString("primer_apellido"));

                System.out.println("Login exitoso para: " + trabajador.getNombreCompleto() +
                        " - Rol: " + trabajador.getRol().getNombreRol());

                rs.close();
                stmt.close();

                return trabajador;
            } else {
                System.out.println("Login fallido: credenciales incorrectas");
                rs.close();
                stmt.close();
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error durante el login: " + e.getMessage());
            throw e;
        } finally {
            conexion.disconnect();
        }
    }
}
