package controller;

import ConexionBase.Conexion;
import dao.impl.HabitacionDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Reserva;
import service.AppContext;
import service.DatabaseConfig;
import service.IHabitacionService;
import service.SessionManager;
import service.impl.ReservaServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RecepcionistaDashboardController {

    @FXML
    private Label lblFechaHora;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblCheckInsHoy;

    @FXML
    private Label lblCheckOutsHoy;

    @FXML
    private Label lblHabDisponibles;

    @FXML
    private Label lblHabOcupadas;

    @FXML
    private Label lblBadgePendientes;

    // Opcionales (si la vista define estos fx:id)
    @FXML
    private Label lblTotalHabitacionesRecep;

    @FXML
    private Label lblOcupacionRecep;

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        // Fecha y usuario
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy");
        lblFechaHora.setText(fmt.format(hoy));

        var usuarioActual = SessionManager.getInstance().getUsuarioActual();
        if (usuarioActual != null) {
            String nombre = (usuarioActual.getPrimerNombre() == null ? "" : usuarioActual.getPrimerNombre()) +
                    " " + (usuarioActual.getPrimerApellido() == null ? "" : usuarioActual.getPrimerApellido());
            String rol = usuarioActual.getRol() != null ? usuarioActual.getRol().getNombreRol() : "";
            lblUsuario.setText(nombre.trim() + " - " + rol);
        } else {
            lblUsuario.setText("Sesión no iniciada");
        }

        // Servicios
        IHabitacionService habitacionService = AppContext.getHabitacionService();
        ReservaServiceImpl reservaService = new ReservaServiceImpl(new ReservaDaoImpl(new Conexion(DatabaseConfig.getDatabase())));

        try {
            System.out.println("[RecepcionistaDashboard] Iniciando carga de métricas generales. DB=" + DatabaseConfig.getDatabase());
            // Métricas generales: contar por estados reales y compatibles
            var reservaDao = new ReservaDaoImpl(new Conexion(DatabaseConfig.getDatabase()));
            int countConfirmadas = reservaDao.countByEstado("CONFIRMADA");
            int countPendientes = reservaDao.countByEstado("PENDIENTE");
            int countActivo = reservaDao.countByEstado("ACTIVO"); // compatibilidad
            int countActiva = reservaDao.countByEstado("ACTIVA"); // compatibilidad
            int reservasActivasTotal = countConfirmadas + countPendientes + countActivo + countActiva;
            System.out.println("[RecepcionistaDashboard] Activas totales: CONFIRMADA=" + countConfirmadas + ", PENDIENTE=" + countPendientes + ", ACTIVO=" + countActivo + ", ACTIVA=" + countActiva + "; TOTAL=" + reservasActivasTotal);

            // Reservas canceladas: totales
            int reservasCanceladasTotal = reservaDao.countByEstado("CANCELADA");
            System.out.println("[RecepcionistaDashboard] Canceladas totales: " + reservasCanceladasTotal);

            // Reservas en curso: totales (EN_PROCESO + EN_CURSO)
            int countEnProceso = reservaDao.countByEstado("EN_PROCESO");
            int countEnCurso = reservaDao.countByEstado("EN_CURSO"); // compatibilidad
            int reservasEnProcesoTotal = countEnProceso + countEnCurso;
            System.out.println("[RecepcionistaDashboard] En curso totales: EN_PROCESO=" + countEnProceso + ", EN_CURSO=" + countEnCurso + "; TOTAL=" + reservasEnProcesoTotal);

            // Actualizar tarjetas (reutilizamos los fx:id existentes)
            lblCheckInsHoy.setText(String.valueOf(reservasActivasTotal));
            lblCheckOutsHoy.setText(String.valueOf(reservasCanceladasTotal));
            lblBadgePendientes.setText(String.valueOf(reservasEnProcesoTotal));

            // Métricas de habitaciones (usar estado "OCUPADO" para coherencia)
            int disponibles = habitacionService.listarPorEstado("DISPONIBLE").size();
            // Aceptar ambas variantes de estado ocupado por consistencia con la base
            int ocupadas = habitacionService.listarPorEstado("OCUPADO").size()
                    + habitacionService.listarPorEstado("OCUPADA").size();
            System.out.println("[RecepcionistaDashboard] Habitaciones: disponibles=" + disponibles + ", ocupadas=" + ocupadas);
            lblHabDisponibles.setText(String.valueOf(disponibles));
            lblHabOcupadas.setText(String.valueOf(ocupadas));

            // Si existen labels opcionales para total y ocupación, actualízalos (evita NPE si no se asignan)
            try {
                int totalHabitaciones = habitacionService.listarTodas().size();
                double porcentajeOcupacion = totalHabitaciones > 0 ? (ocupadas * 100.0) / totalHabitaciones : 0;
                System.out.println("[RecepcionistaDashboard] Total habitaciones=" + totalHabitaciones + ", ocupación=" + String.format("%.0f%%", porcentajeOcupacion));
                // Estos ids podrían no existir en la vista; se setean solo si están inyectados
                Label lblTotalHabitaciones = (Label) this.getClass().getDeclaredField("lblTotalHabitacionesRecep").get(this);
                Label lblOcupacionRecep = (Label) this.getClass().getDeclaredField("lblOcupacionRecep").get(this);
                if (lblTotalHabitaciones != null) {
                    lblTotalHabitaciones.setText("De " + totalHabitaciones + " totales");
                }
                if (lblOcupacionRecep != null) {
                    lblOcupacionRecep.setText(String.format("Ocupación: %.0f%%", porcentajeOcupacion));
                }
            } catch (NoSuchFieldException | IllegalAccessException nsfe) {
                // La vista puede no tener estos labels; continuar sin error
            }
        } catch (Exception e) {
            System.out.println("[RecepcionistaDashboard] Error cargando métricas: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            lblCheckInsHoy.setText("0");
            lblCheckOutsHoy.setText("0");
            lblBadgePendientes.setText("0");
            lblHabDisponibles.setText("0");
            lblHabOcupadas.setText("0");
        }
    }
}
