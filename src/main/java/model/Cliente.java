
package model;


public class Cliente extends Persona {
    
    private Long idCliente;
    private String categoria;

    public Cliente() {}

    public Cliente(Long idCliente, String categoria) {
        this.idCliente = idCliente;
        this.categoria = categoria;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }
    
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    
}
