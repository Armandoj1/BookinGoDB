
package model;

public class Habitacion {
    private Long idHabitacion;
    private String numero;
    private String tipoHabitacion;
    private double precio;
    private String estado;
    private int capacidad;
    private int piso;
    private String descripcion;
    private String caracteristicas;

    public Habitacion() {}

    public Habitacion(Long idHabitacion, String numero, String tipoHabitacion, double precio,
                      String estado, int capacidad, int piso, String descripcion, String caracteristicas) {
        this.idHabitacion = idHabitacion;
        this.numero = numero;
        this.tipoHabitacion = tipoHabitacion;
        this.precio = precio;
        this.estado = estado;
        this.capacidad = capacidad;
        this.piso = piso;
        this.descripcion = descripcion;
        this.caracteristicas = caracteristicas;
    }

    public Long getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Long idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(String tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getPiso() {
        return piso;
    }

    public void setPiso(int piso) {
        this.piso = piso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

   
}
