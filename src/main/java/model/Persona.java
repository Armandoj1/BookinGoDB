
package model;

public class Persona {
    private Long idPersona;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String tipoDocumento;
    private String documento;
    private String estado;
    private String email;
    private long telefono;

    // Constructores
    public Persona() {
    }

    public Persona(Long idPersona, String primerNombre, String segundoNombre, String primerApellido,
            String segundoApellido, String tipoDocumento, String documento,
            String estado, String email, long telefono) {
        this.idPersona = idPersona;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.tipoDocumento = tipoDocumento;
        this.documento = documento;
        this.estado = estado;
        this.email = email;
        this.telefono = telefono;

    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getTelefono() {
        return telefono;
    }

    public void setTelefono(long telefono) {
        this.telefono = telefono;
    }

    // Métodos auxiliares
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();
        if (primerNombre != null)
            nombreCompleto.append(primerNombre);
        if (segundoNombre != null && !segundoNombre.isEmpty())
            nombreCompleto.append(" ").append(segundoNombre);
        if (primerApellido != null)
            nombreCompleto.append(" ").append(primerApellido);
        if (segundoApellido != null && !segundoApellido.isEmpty())
            nombreCompleto.append(" ").append(segundoApellido);
        return nombreCompleto.toString().trim();
    }

    // Setters convenientes para compatibilidad
    public void setNombre(String nombre) {
        this.primerNombre = nombre;
    }

    public String getNombre() {
        return primerNombre;
    }

}