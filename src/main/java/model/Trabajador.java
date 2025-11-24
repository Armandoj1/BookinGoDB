
package model;


public class Trabajador extends Persona {
    private Long idTrabajador;
    private Rol rol;
    private String usuario;
    private String contrasena;
    private double sueldo;

    public Trabajador() {}

    public Trabajador(Long idTrabajador, Rol rol, String usuario, String contrasena, double sueldo) {
        this.idTrabajador = idTrabajador;
        this.rol = rol;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.sueldo = sueldo;
    }

    public Long getIdTrabajador() {
        return idTrabajador;
    }

    public void setIdTrabajador(Long idTrabajador) {
        this.idTrabajador = idTrabajador;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }
    
}

