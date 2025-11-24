package service.impl;

import dao.IReservaDao;
import model.Cliente;
import model.Habitacion;
import model.Reserva;
import model.Trabajador;
import service.IReservaService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservaServiceImpl implements IReservaService {

    private final IReservaDao reservaDao;

    public ReservaServiceImpl(IReservaDao reservaDao) {
        this.reservaDao = reservaDao;
    }

    @Override
    public Reserva crear(Reserva reserva) throws SQLException {
        validarReserva(reserva, true);
        reservaDao.create(reserva);
        return reserva;
    }

    @Override
    public Reserva actualizar(Reserva reserva) throws SQLException {
        if (reserva.getIdReserva() == null || reserva.getIdReserva() <= 0) {
            throw new IllegalArgumentException("La reserva debe tener un ID válido para actualizarse.");
        }
        validarReserva(reserva, false);
        reservaDao.update(reserva);
        return reserva;
    }

    @Override
    public void eliminar(Long idReserva) throws SQLException {
        if (idReserva == null || idReserva <= 0) {
            throw new IllegalArgumentException("ID de reserva inválido.");
        }
        reservaDao.delete(idReserva);
    }

    @Override
    public Reserva buscarPorId(Long idReserva) throws SQLException {
        if (idReserva == null || idReserva <= 0) {
            throw new IllegalArgumentException("ID de reserva inválido.");
        }
        return reservaDao.read(idReserva);
    }

    @Override
    public List<Reserva> listarTodas() throws SQLException {
        return reservaDao.findAll();
    }

    @Override
    public List<Reserva> listarPorCliente(Long idCliente) throws SQLException {
        if (idCliente == null || idCliente <= 0) {
            throw new IllegalArgumentException("ID de cliente inválido.");
        }
        return reservaDao.findByCliente(idCliente);
    }

    @Override
    public List<Reserva> listarPorEstado(String estado) throws SQLException {
        if (estado == null || estado.isBlank()) {
            return listarTodas();
        }
        return reservaDao.findByEstado(estado);
    }

    @Override
    public List<Reserva> listarPorFecha(LocalDate fechaReserva) throws SQLException {
        if (fechaReserva == null) {
            throw new IllegalArgumentException("La fecha de reserva no puede ser null.");
        }
        return reservaDao.findByFecha(fechaReserva);
    }

    // ================= VALIDACIÓN DE RESERVA =================

    private void validarReserva(Reserva r, boolean esCreacion) {
        if (r == null) {
            throw new IllegalArgumentException("La reserva no puede ser null.");
        }

        // Cliente obligatorio
        Cliente cliente = r.getCliente();
       if (cliente == null) {
       throw new IllegalArgumentException("Debe seleccionar un cliente válido.");
       }

        // Habitación obligatoria
        Habitacion habitacion = r.getHabitacion();
        if (habitacion == null || habitacion.getIdHabitacion() == null || habitacion.getIdHabitacion() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una habitación válida.");
        }

        // Trabajador: en tu tabla es NOT NULL (por el create usas getTrabajador())
        Trabajador trabajador = r.getTrabajador();
        if (trabajador == null || trabajador.getIdTrabajador() == null || trabajador.getIdTrabajador() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un trabajador válido.");
        }

        // Fechas (según tus CHECK de BD)
        LocalDate fReserva  = r.getFechaReserva();
        LocalDate fEntrada  = r.getFechaEntrada();
        LocalDate fSalida   = r.getFechaSalida();

        if (fReserva == null || fEntrada == null || fSalida == null) {
            throw new IllegalArgumentException("Las fechas de reserva, entrada y salida son obligatorias.");
        }

        // fecha_entrada >= fecha_reserva
        if (fEntrada.isBefore(fReserva)) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser anterior a la fecha de reserva.");
        }

        // fecha_salida > fecha_entrada
        if (!fSalida.isAfter(fEntrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la fecha de entrada.");
        }

        // Tipo de reserva (chk_tipo_reserva)
        if (!esDeLista(r.getTipoReserva(),
                "ONLINE", "PRESENCIAL", "TELEFONICA", "AGENCIA", "CORPORATIVA")) {
            throw new IllegalArgumentException(
                    "Tipo de reserva inválido. Debe ser ONLINE, PRESENCIAL, TELEFONICA, AGENCIA o CORPORATIVA."
            );
        }

        // Estado de reserva (chk_estado_reserva)
        if (!esDeLista(r.getEstado(),
                "PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA", "EN_PROCESO")) {
            throw new IllegalArgumentException(
                    "Estado de reserva inválido. Debe ser PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA o EN_PROCESO."
            );
        }

        // Total ≥ 0 (chk_total)
        if (r.getTotal() < 0) {
            throw new IllegalArgumentException("El total de la reserva no puede ser negativo.");
        }

        // (Opcional) puedes validar que el total sea coherente con precio * noches * personas, etc.
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

