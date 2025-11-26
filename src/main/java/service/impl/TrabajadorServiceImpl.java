package service.impl;

import dao.ITrabajadorDao;
import model.Trabajador;
import service.ITrabajadorService;

import java.sql.SQLException;
import java.util.List;

public class TrabajadorServiceImpl implements ITrabajadorService {

    private final ITrabajadorDao trabajadorDao;

    public TrabajadorServiceImpl(ITrabajadorDao trabajadorDao) {
        this.trabajadorDao = trabajadorDao;
    }

    @Override
    public Trabajador crear(Trabajador trabajador) throws SQLException {
        validarTrabajador(trabajador, true);
        trabajadorDao.create(trabajador);
        return trabajador;
    }

    @Override
    public Trabajador crearConProcedimiento(Trabajador trabajador) throws SQLException {
        validarTrabajador(trabajador, true);
        Long id = trabajadorDao.createUsingProcedure(trabajador);
        // id ya se asignó a trabajador en el DAO, pero igual retornamos objeto completo
        return trabajador;
    }

    @Override
    public Trabajador actualizar(Trabajador trabajador) throws SQLException {
        if (trabajador.getIdTrabajador() == null || trabajador.getIdTrabajador() <= 0) {
            throw new IllegalArgumentException("El trabajador debe tener un ID válido para actualizarse.");
        }
        validarTrabajador(trabajador, false);
        trabajadorDao.update(trabajador);
        return trabajador;
    }

    @Override
    public void eliminar(Long idTrabajador) throws SQLException {
        if (idTrabajador == null || idTrabajador <= 0) {
            throw new IllegalArgumentException("ID de trabajador inválido.");
        }
        trabajadorDao.delete(idTrabajador);
    }

    @Override
    public void activar(Long idTrabajador) throws SQLException {
        if (idTrabajador == null || idTrabajador <= 0) {
            throw new IllegalArgumentException("ID de trabajador inválido.");
        }
        trabajadorDao.activate(idTrabajador);
    }

    @Override
    public Trabajador buscarPorId(Long idTrabajador) throws SQLException {
        if (idTrabajador == null || idTrabajador <= 0) {
            throw new IllegalArgumentException("ID de trabajador inválido.");
        }
        return trabajadorDao.read(idTrabajador);
    }

    @Override
    public List<Trabajador> listarTodos() throws SQLException {
        return trabajadorDao.findAll();
    }

    @Override
    public Trabajador buscarPorUsuario(String usuario) throws SQLException {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        return trabajadorDao.findByUsuario(usuario);
    }

    @Override
    public Trabajador autenticar(String usuario, String contrasena) throws SQLException {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        return trabajadorDao.authenticate(usuario, contrasena);
    }

    // ================= VALIDACIONES =================

    private void validarTrabajador(Trabajador t, boolean esCreacion) throws SQLException {
        if (t == null) {
            throw new IllegalArgumentException("El trabajador no puede ser null.");
        }

        // Nombres / apellidos obligatorios
        if (esVacio(t.getPrimerNombre())) {
            throw new IllegalArgumentException("El primer nombre es obligatorio.");
        }
        if (esVacio(t.getPrimerApellido())) {
            throw new IllegalArgumentException("El primer apellido es obligatorio.");
        }
        if (esVacio(t.getSegundoApellido())) {
            throw new IllegalArgumentException("El segundo apellido es obligatorio.");
        }

        // Tipo de documento
        if (!esDeLista(t.getTipoDocumento(), "CC", "TI", "CE", "PP", "NIT")) {
            throw new IllegalArgumentException("Tipo de documento inválido. Debe ser CC, TI, CE, PP o NIT.");
        }

        // Documento: CC entre 6 y 11 dígitos; otros tipos 5 a 20 caracteres
        if (esVacio(t.getDocumento())) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        String doc = t.getDocumento().trim();
        String tipoDocT = t.getTipoDocumento() == null ? "" : t.getTipoDocumento().trim().toUpperCase();
        if ("CC".equals(tipoDocT)) {
            if (!doc.matches("^\\d{6,11}$")) {
                throw new IllegalArgumentException("La CC debe tener entre 6 y 11 dígitos, sin letras.");
            }
        } else {
            if (doc.length() < 5 || doc.length() > 20) {
                throw new IllegalArgumentException("El documento debe tener entre 5 y 20 caracteres.");
            }
        }

        // Prevalidación de unicidad por documento
        Trabajador porDoc = trabajadorDao.findByDocumento(doc);
        if (porDoc != null && (esCreacion || !porDoc.getIdTrabajador().equals(t.getIdTrabajador()))) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese documento.");
        }

        // Estado
        if (!esDeLista(t.getEstado(), "ACTIVO", "INACTIVO", "SUSPENDIDO")) {
            throw new IllegalArgumentException("Estado inválido. Debe ser ACTIVO, INACTIVO o SUSPENDIDO.");
        }

        // Email con regex estándar
        if (esVacio(t.getEmail())) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        String email = t.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }
        // Prevalidación de unicidad por email
        Trabajador porEmail = trabajadorDao.findByEmail(email);
        if (porEmail != null && (esCreacion || !porEmail.getIdTrabajador().equals(t.getIdTrabajador()))) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese email.");
        }

        // Teléfono: debe iniciar con 3 y tener 10 dígitos
        String telStr = String.valueOf(t.getTelefono());
        if (!telStr.matches("^3\\d{9}$")) {
            throw new IllegalArgumentException("El teléfono debe iniciar con 3 y tener 10 dígitos.");
        }
        // Prevalidación de unicidad por teléfono
        Trabajador porTel = trabajadorDao.findByTelefono(t.getTelefono());
        if (porTel != null && (esCreacion || !porTel.getIdTrabajador().equals(t.getIdTrabajador()))) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese teléfono.");
        }

        // Sueldo (si sigues la regla de sueldo mínimo/máximo)
        if (t.getSueldo() < 1_300_000 || t.getSueldo() > 50_000_000) {
            throw new IllegalArgumentException("El sueldo debe estar entre 1.300.000 y 50.000.000.");
        }

        // Credenciales para roles administrativos (ADMINISTRADOR, RECEPCIONISTA)
        String nombreRol = t.getRol() != null && t.getRol().getNombreRol() != null
                ? t.getRol().getNombreRol().trim().toUpperCase() : "";
        boolean rolAdmin = nombreRol.equals("ADMINISTRADOR") || nombreRol.equals("RECEPCIONISTA");

        if (rolAdmin) {
            // Para ADMINISTRADOR/RECEPCIONISTA, usuario y contraseña son obligatorios
            if (esVacio(t.getUsuario())) {
                throw new IllegalArgumentException("Para roles administrativos (ADMINISTRADOR/RECEPCIONISTA) el usuario es obligatorio.");
            }
            if (t.getUsuario().length() < 5) {
                throw new IllegalArgumentException("El usuario debe tener al menos 5 caracteres.");
            }
            Trabajador existente = trabajadorDao.findByUsuario(t.getUsuario());
            if (existente != null && (esCreacion || !existente.getIdTrabajador().equals(t.getIdTrabajador()))) {
                throw new IllegalArgumentException("Ya existe un trabajador con ese usuario.");
            }

            if (esVacio(t.getContrasena())) {
                throw new IllegalArgumentException("Para roles administrativos (ADMINISTRADOR/RECEPCIONISTA) la contraseña es obligatoria.");
            }
        } else {
            // Para roles no administrativos, no deben tener credenciales
            if (!esVacio(t.getUsuario()) || !esVacio(t.getContrasena())) {
                throw new IllegalArgumentException("Este rol no requiere credenciales. Deje usuario y contraseña vacíos.");
            }
        }

        // Rol obligatorio
        if (t.getRol() == null || t.getRol().getIdRol() == null || t.getRol().getIdRol() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un rol válido para el trabajador.");
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean esDeLista(String valor, String... permitidos) {
        if (valor == null) return false;
        for (String p : permitidos) {
            if (valor.equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }
}

