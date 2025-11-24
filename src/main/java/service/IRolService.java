
package service;

import model.Rol;
import java.util.List;

public interface IRolService {

    void crearRol(Rol rol) throws Exception;

    Rol obtenerPorId(Long id) throws Exception;

    void actualizarRol(Rol rol) throws Exception;

    void eliminarRol(Long id) throws Exception;

    List<Rol> listarRoles() throws Exception;
}
