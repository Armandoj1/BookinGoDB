
package model;

import java.time.LocalDate;


public class Comprobante {
    private Long idComprobante;
    private Reserva reserva;
    private String tipoComprobante;
    private LocalDate fechaEmision;
    private double montoTotal;
    private String metodoPago;
    private String descripcion;
    private String estado;

    public Comprobante() {}

    public Comprobante(Long idComprobante, Reserva reserva, String tipoComprobante, LocalDate fechaEmision,
                        double montoTotal, String metodoPago, String descripcion, String estado) {
        this.idComprobante = idComprobante;
        this.reserva = reserva;
        this.tipoComprobante = tipoComprobante;
        this.fechaEmision = fechaEmision;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public Long getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Long idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    
}
