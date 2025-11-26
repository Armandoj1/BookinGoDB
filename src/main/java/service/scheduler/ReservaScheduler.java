package service.scheduler;

import ConexionBase.Conexion;
import dao.impl.ReservaDaoImpl;
import service.DatabaseConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ReservaScheduler {

    private static ReservaScheduler instance;
    private final ScheduledExecutorService scheduler;

    private ReservaScheduler() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public static synchronized ReservaScheduler getInstance() {
        if (instance == null) {
            instance = new ReservaScheduler();
        }
        return instance;
    }

    /**
     * Inicia la ejecución periódica cada 5 minutos.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
                ReservaDaoImpl reservaDao = new ReservaDaoImpl(conexion);

                // 1) Finalizar reservas vencidas
                reservaDao.finalizarReservasVencidas();

                // 2) Liberar habitaciones de reservas finalizadas
                reservaDao.liberarHabitacionesFinalizadas();

                // Cerrar la conexión si fue abierta en los métodos
                conexion.disconnect();
            } catch (Exception ex) {
                System.err.println("[ReservaScheduler] Error en ejecución periódica: " + ex.getMessage());
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * Detiene el scheduler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }
}