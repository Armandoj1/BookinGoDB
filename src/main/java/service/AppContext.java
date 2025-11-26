package service;

import ConexionBase.Conexion;
import dao.impl.ClienteDaoImpl;
import dao.impl.HabitacionDaoImpl;
import dao.impl.HuespedDaoImpl;
import dao.IHuespedDao;
import service.impl.ClienteServiceImpl;
import service.impl.HabitacionServiceImpl;
import service.impl.HuespedServiceImpl;

/**
 * Proveedor simple de dependencias a nivel de aplicación.
 * Centraliza la creación de DAOs y Services para aplicar Inversión de Dependencias.
 */
public final class AppContext {

    private static Conexion conexion;
    private static IClienteService clienteService;
    private static IHabitacionService habitacionService;
    private static IHuespedService huespedService;

    private AppContext() {}

    private static Conexion getConexion() {
        if (conexion == null) {
            conexion = new Conexion(DatabaseConfig.getDatabase());
        }
        return conexion;
    }

    public static IClienteService getClienteService() {
        if (clienteService == null) {
            clienteService = new ClienteServiceImpl(new ClienteDaoImpl(getConexion()));
        }
        return clienteService;
    }

    public static IHabitacionService getHabitacionService() {
        if (habitacionService == null) {
            habitacionService = new HabitacionServiceImpl(new HabitacionDaoImpl(getConexion()));
        }
        return habitacionService;
    }

    public static IHuespedService getHuespedService() {
        if (huespedService == null) {
            IHuespedDao dao = new HuespedDaoImpl(getConexion());
            huespedService = new HuespedServiceImpl(dao);
        }
        return huespedService;
    }
}