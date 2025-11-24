package service.impl;

import dao.IComprobanteDao;
import model.Comprobante;
import service.IComprobanteService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ComprobanteServiceImpl implements IComprobanteService {

    private final IComprobanteDao comprobanteDao;

    public ComprobanteServiceImpl(IComprobanteDao comprobanteDao) {
        this.comprobanteDao = comprobanteDao;
    }

    @Override
    public Comprobante crear(Comprobante comprobante) throws SQLException {
        validarComprobante(comprobante);
        comprobanteDao.create(comprobante);
        return comprobante;
    }

    @Override
    public Comprobante actualizar(Comprobante comprobante) throws SQLException {
        if (comprobante.getIdComprobante() == null) {
            throw new IllegalArgumentException("El comprobante debe tener ID para actualizarse.");
        }
        validarComprobante(comprobante);
        comprobanteDao.update(comprobante);
        return comprobante;
    }

    @Override
    public void eliminar(Long idComprobante) throws SQLException {
        comprobanteDao.delete(idComprobante);
    }

    @Override
    public Comprobante buscarPorId(Long idComprobante) throws SQLException {
        return comprobanteDao.read(idComprobante);
    }

    @Override
    public List<Comprobante> listarTodos() throws SQLException {
        return comprobanteDao.findAll();
    }

    @Override
    public List<Comprobante> listarPorReserva(Long idReserva) throws SQLException {
        if (idReserva == null) {
            throw new IllegalArgumentException("El ID de reserva no puede ser null.");
        }
        return comprobanteDao.findByReserva(idReserva);
    }

    @Override
    public List<Comprobante> listarPorEstado(String estado) throws SQLException {
        if (estado == null || estado.isBlank()) {
            return listarTodos();
        }
        return comprobanteDao.findByEstado(estado);
    }

    // ================= VALIDACIONES =================

    private void validarComprobante(Comprobante c) {
        if (c == null) {
            throw new IllegalArgumentException("El comprobante no puede ser null.");
        }

        // Reserva asociada
        if (c.getReserva() == null || c.getReserva().getIdReserva() == null) {
            throw new IllegalArgumentException("Debe seleccionar una reserva válida.");
        }

        // Fecha de emisión
        LocalDate fecha = c.getFechaEmision();
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de emisión es obligatoria.");
        }

        // Monto total ≥ 0 (según CHECK chk_monto_total)
        if (c.getMontoTotal() < 0) {
            throw new IllegalArgumentException("El monto total no puede ser negativo.");
        }

        // Tipo de comprobante (chk_tipo_comprobante)
        if (!esDeLista(c.getTipoComprobante(),
                "FACTURA", "RECIBO", "NOTA_CREDITO", "NOTA_DEBITO")) {
            throw new IllegalArgumentException(
                    "Tipo de comprobante inválido. " +
                    "Debe ser FACTURA, RECIBO, NOTA_CREDITO o NOTA_DEBITO."
            );
        }

        // Método de pago (chk_metodo_pago)
        if (!esDeLista(c.getMetodoPago(),
                "EFECTIVO", "TARJETA", "TRANSFERENCIA", "PSE", "NEQUI", "DAVIPLATA")) {
            throw new IllegalArgumentException(
                    "Método de pago inválido. Debe ser EFECTIVO, TARJETA, TRANSFERENCIA, PSE, NEQUI o DAVIPLATA."
            );
        }

        // Estado (chk_estado_comprobante)
        if (!esDeLista(c.getEstado(),
                "EMITIDO", "PENDIENTE", "ENPROCESO", "PAGADO", "ANULADO")) {
            throw new IllegalArgumentException(
                    "Estado de comprobante inválido. " +
                    "Debe ser EMITIDO, PENDIENTE, ENPROCESO, PAGADO o ANULADO."
            );
        }

        // Descripción: NOT NULL y longitud acorde a tu esquema (VARCHAR2(10) según tu script)
        if (c.getDescripcion() == null || c.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        if (c.getDescripcion().length() > 10) {
            // Ajusta este límite si cambias el tamaño en la BD
            throw new IllegalArgumentException("La descripción no puede superar los 10 caracteres.");
        }
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

