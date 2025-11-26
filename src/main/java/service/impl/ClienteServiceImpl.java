package service.impl;

import dao.IClienteDao;
import model.Cliente;
import service.IClienteService;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class ClienteServiceImpl implements IClienteService {

    private final IClienteDao clienteDao;

    // Regex similar al CHECK de email que mostraste
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public ClienteServiceImpl(IClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    @Override
    public Cliente crear(Cliente cliente) throws SQLException {
        validarCliente(cliente, true);
        // Prevalidación de documento único
        Cliente existente = clienteDao.findByDocumento(cliente.getDocumento().trim());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
        }
        // Prevalidación de email único
        Cliente porEmail = clienteDao.findByEmail(cliente.getEmail().trim());
        if (porEmail != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email.");
        }
        // Prevalidación de teléfono único
        Cliente porTelefono = clienteDao.findByTelefono(cliente.getTelefono());
        if (porTelefono != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese teléfono.");
        }
        clienteDao.create(cliente);
        return cliente;
    }

    @Override
    public Cliente crearConProcedimiento(Cliente cliente) throws SQLException {
        validarCliente(cliente, true);
        // Prevalidaciones de unicidad para evitar duplicados antes de llamar al SP
        Cliente porDocumento = clienteDao.findByDocumento(cliente.getDocumento().trim());
        if (porDocumento != null) {
            throw new IllegalArgumentException("El documento ya está asociado a otro cliente.");
        }
        Cliente porEmail = clienteDao.findByEmail(cliente.getEmail().trim());
        if (porEmail != null) {
            throw new IllegalArgumentException("El email ya está asociado a otro cliente.");
        }
        Cliente porTelefono = clienteDao.findByTelefono(cliente.getTelefono());
        if (porTelefono != null) {
            throw new IllegalArgumentException("El teléfono ya está asociado a otro cliente.");
        }

        Long id = clienteDao.createUsingProcedure(cliente);
        // El DAO asigna el id al objeto; retornamos el cliente ya creado
        return cliente;
    }

    @Override
    public Cliente actualizar(Cliente cliente) throws SQLException {
        // Aquí estaba el error: getIdCliente() es long primitivo, no puede ser null
        if (cliente == null || cliente.getIdCliente() <= 0) {
            throw new IllegalArgumentException("El cliente debe tener un ID válido para poder actualizarse.");
        }
        validarCliente(cliente, false);
        // Prevalidación: documento no debe pertenecer a otro cliente
        Cliente existente = clienteDao.findByDocumento(cliente.getDocumento().trim());
        if (existente != null && existente.getIdCliente() != cliente.getIdCliente()) {
            throw new IllegalArgumentException("El documento ya está asociado a otro cliente.");
        }
        // Prevalidación: email no debe pertenecer a otro cliente
        Cliente porEmail = clienteDao.findByEmail(cliente.getEmail().trim());
        if (porEmail != null && porEmail.getIdCliente() != cliente.getIdCliente()) {
            throw new IllegalArgumentException("El email ya está asociado a otro cliente.");
        }
        // Prevalidación: teléfono no debe pertenecer a otro cliente
        Cliente porTelefono = clienteDao.findByTelefono(cliente.getTelefono());
        if (porTelefono != null && porTelefono.getIdCliente() != cliente.getIdCliente()) {
            throw new IllegalArgumentException("El teléfono ya está asociado a otro cliente.");
        }
        clienteDao.update(cliente);
        return cliente;
    }

    @Override
    public void eliminar(Long idCliente) throws SQLException {
        clienteDao.delete(idCliente);
    }

    @Override
    public void activar(Long idCliente) throws SQLException {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido.");
        }
        clienteDao.activate(idCliente);
    }

    @Override
    public Cliente buscarPorId(Long idCliente) throws SQLException {
        return clienteDao.read(idCliente);
    }

    @Override
    public Cliente buscarPorDocumento(String documento) throws SQLException {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacío.");
        }
        return clienteDao.findByDocumento(documento);
    }

    @Override
    public List<Cliente> listarTodos() throws SQLException {
        return clienteDao.findAll();
    }

    @Override
    public List<Cliente> listarPorEstado(String estado) throws SQLException {
        if (estado == null || estado.isBlank()) {
            return listarTodos();
        }
        return clienteDao.findByEstado(estado);
    }

    // ================= VALIDACIONES =================

    private void validarCliente(Cliente c, boolean esCreacion) {
        if (c == null) {
            throw new IllegalArgumentException("El cliente no puede ser null.");
        }

        // Nombres / Apellidos
        if (esVacio(c.getPrimerNombre())) {
            throw new IllegalArgumentException("El primer nombre es obligatorio.");
        }
        if (esVacio(c.getPrimerApellido())) {
            throw new IllegalArgumentException("El primer apellido es obligatorio.");
        }
        if (esVacio(c.getSegundoApellido())) {
            throw new IllegalArgumentException("El segundo apellido es obligatorio.");
        }

        // Tipo de documento
        if (!esDeLista(c.getTipoDocumento(), "CC", "TI", "CE", "PP", "NIT", "DNI")) {
            throw new IllegalArgumentException("Tipo de documento inválido.");
        }

        // Documento: CC entre 6 y 11 dígitos sin letras; otros tipos 5 a 20 caracteres
        if (esVacio(c.getDocumento())) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        String doc = c.getDocumento().trim();
        String tipoDoc = c.getTipoDocumento() == null ? "" : c.getTipoDocumento().trim().toUpperCase();
        if ("CC".equals(tipoDoc)) {
            if (!doc.matches("^\\d{6,11}$")) {
                throw new IllegalArgumentException("La CC debe tener entre 6 y 11 dígitos, sin letras.");
            }
        } else {
            if (doc.length() < 5 || doc.length() > 20) {
                throw new IllegalArgumentException("El documento debe tener entre 5 y 20 caracteres.");
            }
        }

        // Estado (puedes ajustar a tus valores reales)
        if (!esDeLista(c.getEstado(), "ACTIVO", "INACTIVO", "SUSPENDIDO")) {
            throw new IllegalArgumentException("Estado de cliente inválido.");
        }

        // Email
        if (esVacio(c.getEmail()) || !EMAIL_REGEX.matcher(c.getEmail()).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }

        // Teléfono: debe iniciar con 3 y tener 10 dígitos
        String telStr = String.valueOf(c.getTelefono());
        if (!telStr.matches("^3\\d{9}$")) {
            throw new IllegalArgumentException("El teléfono debe iniciar con 3 y tener 10 dígitos.");
        }

        // Categoría
        if (!esDeLista(c.getCategoria(),
                "NUEVO", "FRECUENTE", "VIP", "CORPORATIVO", "REGULAR")) {
            throw new IllegalArgumentException("Categoría de cliente inválida.");
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean esDeLista(String valor, String... permitidos) {
        if (valor == null) return false;
        for (String p : permitidos) {
            if (valor.equalsIgnoreCase(p)) return true;
        }
        return false;
    }
}
