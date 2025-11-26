package service.impl;

import dao.IHabitacionDao;
import model.Habitacion;
import service.IHabitacionService;

import java.sql.SQLException;
import java.util.List;

public class HabitacionServiceImpl implements IHabitacionService {

    private final IHabitacionDao habitacionDao;

    public HabitacionServiceImpl(IHabitacionDao habitacionDao) {
        this.habitacionDao = habitacionDao;
    }

    @Override
    public Habitacion crear(Habitacion habitacion) throws SQLException {
        validarHabitacion(habitacion);
        habitacionDao.create(habitacion);
        return habitacion;
    }

    @Override
    public Habitacion actualizar(Habitacion habitacion) throws SQLException {
        if (habitacion.getIdHabitacion() == null) {
            throw new IllegalArgumentException("La habitación debe tener ID para actualizarse.");
        }
        validarHabitacion(habitacion);
        habitacionDao.update(habitacion);
        return habitacion;
    }

    @Override
    public void eliminar(Long idHabitacion) throws SQLException {
        habitacionDao.delete(idHabitacion);
    }

    @Override
    public Habitacion buscarPorId(Long idHabitacion) throws SQLException {
        return habitacionDao.read(idHabitacion);
    }

    @Override
    public Habitacion buscarPorNumero(String numero) throws SQLException {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número de habitación no puede estar vacío.");
        }
        return habitacionDao.findByNumero(numero);
    }

    @Override
    public List<Habitacion> listarTodas() throws SQLException {
        return habitacionDao.findAll();
    }

    @Override
    public List<Habitacion> listarPorEstado(String estado) throws SQLException {
        if (estado == null || estado.isBlank()) {
            return listarTodas();
        }
        return habitacionDao.findByEstado(estado);
    }

    @Override
    public List<Habitacion> listarPorTipo(String tipo) throws SQLException {
        if (tipo == null || tipo.isBlank()) {
            return listarTodas();
        }
        return habitacionDao.findByTipo(tipo);
    }

    @Override
    public void cambiarEstado(Long idHabitacion, String nuevoEstado) throws SQLException {
        // Estados válidos: DISPONIBLE, OCUPADO
        if (!esDeLista(nuevoEstado, "DISPONIBLE", "OCUPADO")) {
            throw new IllegalArgumentException(
                    "Estado de habitación inválido. Debe ser DISPONIBLE u OCUPADO."
            );
        }

        Habitacion h = habitacionDao.read(idHabitacion);
        if (h == null) {
            throw new IllegalArgumentException("No se encontró la habitación con ID " + idHabitacion);
        }

        h.setEstado(nuevoEstado);
        habitacionDao.update(h);
    }

    // ================= VALIDACIONES =================

    private void validarHabitacion(Habitacion h) {
        if (h == null) {
            throw new IllegalArgumentException("La habitación no puede ser null.");
        }

        // Número
        if (esVacio(h.getNumero())) {
            throw new IllegalArgumentException("El número de habitación es obligatorio.");
        }

        // Tipo de habitación (chk_tipo_habitacion)
        if (!esDeLista(h.getTipoHabitacion(),
                "SENCILLA", "DOBLE", "SUITE", "FAMILIAR", "PRESIDENCIAL")) {
            throw new IllegalArgumentException(
                    "Tipo de habitación inválido. Debe ser SENCILLA, DOBLE, SUITE, FAMILIAR o PRESIDENCIAL."
            );
        }

        // Precio (chk_precio)
        if (h.getPrecio() < 50_000 || h.getPrecio() > 5_000_000) {
            throw new IllegalArgumentException(
                    "El precio debe estar entre 50.000 y 5.000.000."
            );
        }

        // Estado (chk_estado_habitacion)
        if (!esDeLista(h.getEstado(),
                "DISPONIBLE", "OCUPADA", "LIMPIEZA", "MANTENIMIENTO", "FUERA_SERVICIO")) {
            throw new IllegalArgumentException(
                    "Estado de habitación inválido. Debe ser DISPONIBLE, OCUPADA, LIMPIEZA, MANTENIMIENTO o FUERA_SERVICIO."
            );
        }

        // Capacidad (chk_capacidad)
        if (h.getCapacidad() < 1 || h.getCapacidad() > 10) {
            throw new IllegalArgumentException("La capacidad debe estar entre 1 y 10 personas.");
        }

        // Piso (chk_piso: puede ser null, o entre 1 y 30)
        Integer piso = h.getPiso();
        if (piso != null && (piso < 1 || piso > 30)) {
            throw new IllegalArgumentException("El piso debe estar entre 1 y 30.");
        }
    } // <<--- ESTA LLAVE FALTABA

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
