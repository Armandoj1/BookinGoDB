package service;

import model.Trabajador;

/**
 * Singleton para gestionar la sesión del usuario actualmente logueado
 */
public class SessionManager {

    private static SessionManager instance;
    private Trabajador usuarioActual;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Establece el usuario actual después de un login exitoso
     */
    public void login(Trabajador trabajador) {
        this.usuarioActual = trabajador;
        System.out.println("Sesión iniciada para: " + trabajador.getNombreCompleto());
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        if (usuarioActual != null) {
            System.out.println("Sesión cerrada para: " + usuarioActual.getNombreCompleto());
            this.usuarioActual = null;
        }
    }

    /**
     * Obtiene el usuario actualmente logueado
     * 
     * @return Usuario actual o null si no hay sesión activa
     */
    public Trabajador getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Verifica si hay una sesión activa
     */
    public boolean isLoggedIn() {
        return usuarioActual != null;
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    public boolean tieneRol(String nombreRol) {
        return usuarioActual != null &&
                usuarioActual.getRol() != null &&
                usuarioActual.getRol().getNombreRol().equalsIgnoreCase(nombreRol);
    }

    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean esAdministrador() {
        return tieneRol("ADMINISTRADOR");
    }

    /**
     * Verifica si el usuario actual es recepcionista
     */
    public boolean esRecepcionista() {
        return tieneRol("RECEPCIONISTA");
    }
}
