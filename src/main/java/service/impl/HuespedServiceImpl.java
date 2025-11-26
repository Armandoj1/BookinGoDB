package service.impl;

import dao.IHuespedDao;
import model.Huesped;
import service.IHuespedService;

import java.sql.SQLException;
import java.util.List;

public class HuespedServiceImpl implements IHuespedService {

    private final IHuespedDao huespedDao;

    public HuespedServiceImpl(IHuespedDao huespedDao) {
        this.huespedDao = huespedDao;
    }

    @Override
    public void crearPersona(Huesped huesped) throws SQLException {
        validar(huesped);
        // Prevalidación de unicidad por documento
        if (huespedDao.existsDocumento(huesped.getDocumento().trim())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese documento.");
        }
        // Prevalidación de unicidad por email
        if (huespedDao.existsEmail(huesped.getEmail().trim())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese email.");
        }
        // Prevalidación de unicidad por teléfono
        long tel = Long.parseLong(huesped.getTelefono().trim());
        if (huespedDao.existsTelefono(tel)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con ese teléfono.");
        }
        huespedDao.createPersona(huesped);
    }

    @Override
    public List<Huesped> listarTodos() throws SQLException {
        return huespedDao.findAll();
    }

    private void validar(Huesped h) {
        if (h == null) throw new IllegalArgumentException("El huésped no puede ser null.");
        if (vacio(h.getPrimerNombre())) throw new IllegalArgumentException("Primer nombre es obligatorio.");
        if (vacio(h.getPrimerApellido())) throw new IllegalArgumentException("Primer apellido es obligatorio.");

        // Tipo de documento y documento
        if (vacio(h.getTipoDocumento())) throw new IllegalArgumentException("Tipo de documento es obligatorio.");
        if (vacio(h.getDocumento())) throw new IllegalArgumentException("Documento es obligatorio.");
        String tipoDoc = h.getTipoDocumento().trim().toUpperCase();
        String doc = h.getDocumento().trim();
        if ("CC".equals(tipoDoc)) {
            if (!doc.matches("^\\d{6,11}$")) {
                throw new IllegalArgumentException("La CC debe tener entre 6 y 11 dígitos, sin letras.");
            }
        }

        // Email
        if (vacio(h.getEmail())) throw new IllegalArgumentException("Email es obligatorio.");
        String email = h.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }

        // Teléfono: debe iniciar con 3 y tener 10 dígitos
        if (vacio(h.getTelefono())) throw new IllegalArgumentException("Teléfono es obligatorio.");
        String tel = h.getTelefono().trim();
        if (!tel.matches("^3\\d{9}$")) {
            throw new IllegalArgumentException("El teléfono debe iniciar con 3 y tener 10 dígitos.");
        }

        // Estado por defecto
        if (vacio(h.getEstado())) h.setEstado("ACTIVO");
    }

    private boolean vacio(String s) { return s == null || s.trim().isEmpty(); }
}