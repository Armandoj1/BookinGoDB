package service.impl;

import dao.IRolDao;
import model.Rol;
import service.IRolService;

import java.util.List;

public class RolServiceImpl implements IRolService {

    private final IRolDao rolDao;

    public RolServiceImpl(IRolDao rolDao) {
        this.rolDao = rolDao;
    }

    @Override
    public void crearRol(Rol rol) throws Exception {

        if (rol == null) {
            throw new Exception("El rol no puede ser null.");
        }
        if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
            throw new Exception("El nombre del rol es obligatorio.");
        }
        if (rol.getNombreRol().length() > 30) {
            throw new Exception("El nombre del rol no puede superar 30 caracteres.");
        }
        if (rol.getDescripcion() == null || rol.getDescripcion().trim().isEmpty()) {
            throw new Exception("La descripción del rol es obligatoria.");
        }
        if (rol.getDescripcion().length() > 100) {
            throw new Exception("La descripción es muy larga.");
        }

        // Validar que no exista un rol con ese nombre
        List<Rol> existentes = rolDao.findAll();
        for (Rol r : existentes) {
            if (r.getNombreRol().equalsIgnoreCase(rol.getNombreRol())) {
                throw new Exception("Ya existe un rol con este nombre.");
            }
        }

        rolDao.create(rol);
    }

    @Override
    public Rol obtenerPorId(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new Exception("ID inválido");
        }
        return rolDao.read(id);
    }

    @Override
    public void actualizarRol(Rol rol) throws Exception {

        if (rol.getIdRol() == null || rol.getIdRol() <= 0) {
            throw new Exception("ID del rol inválido.");
        }
        if (rol.getNombreRol() == null || rol.getNombreRol().trim().isEmpty()) {
            throw new Exception("El nombre del rol es obligatorio.");
        }
        if (rol.getNombreRol().length() > 30) {
            throw new Exception("El nombre del rol no puede superar 30 caracteres.");
        }
        if (rol.getDescripcion() == null || rol.getDescripcion().trim().isEmpty()) {
            throw new Exception("La descripción del rol es obligatoria.");
        }

        rolDao.update(rol);
    }

    @Override
    public void eliminarRol(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new Exception("ID inválido.");
        }
        rolDao.delete(id);
    }

    @Override
    public List<Rol> listarRoles() throws Exception {
        return rolDao.findAll();
    }
}

