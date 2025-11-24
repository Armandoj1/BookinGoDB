package controller;

import ConexionBase.Conexion;
import dao.impl.HabitacionDaoImpl;
import dao.impl.ReservaDaoImpl;
import dao.impl.TrabajadorDaoImpl;
import service.DatabaseConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class AdminDashboardController {

    @FXML
    private Label lblFechaHora;
    @FXML
    private Label lblTotalHabitaciones;
    @FXML
    private Label lblOcupacion;
    @FXML
    private ProgressBar pbOcupacion;
    @FXML
    private Label lblIngresosMes;
    @FXML
    private Label lblReservasActivas;
    @FXML
    private Label lblTotalTrabajadores;
    @FXML
    private Label lblAdministradores;
    @FXML
    private Label lblRecepcionistas;
    @FXML
    private Label lblHabDisponibles;
    @FXML
    private Label lblHabOcupadas;
    @FXML
    private Label lblHabMantenimiento;

    // DAOs
    private HabitacionDaoImpl habitacionDao;
    private TrabajadorDaoImpl trabajadorDao;
    private ReservaDaoImpl reservaDao;

    @FXML
    private void initialize() {
        // Inicializar DAOs usando la configuración centralizada
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        habitacionDao = new HabitacionDaoImpl(conexion);
        trabajadorDao = new TrabajadorDaoImpl(conexion);
        reservaDao = new ReservaDaoImpl(conexion);

        actualizarFechaHora();
        cargarEstadisticas();
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        String diaSemana = ahora.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy - hh:mm a",
                new Locale("es", "ES"));
        String fechaFormateada = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1) + ", "
                + ahora.format(formatter);
        lblFechaHora.setText(fechaFormateada);
    }

    private void cargarEstadisticas() {
        try {
            // Contar habitaciones por estado
            int habDisponibles = habitacionDao.countByEstado("DISPONIBLE");
            int habOcupadas = habitacionDao.countByEstado("OCUPADO");
            int habMantenimiento = habitacionDao.countByEstado("MANTENIMIENTO");
            int totalHabitaciones = habDisponibles + habOcupadas + habMantenimiento;

            // Actualizar labels de habitaciones
            lblTotalHabitaciones.setText("De " + totalHabitaciones + " totales");
            lblHabDisponibles.setText(String.valueOf(habDisponibles));
            lblHabOcupadas.setText(String.valueOf(habOcupadas));
            lblHabMantenimiento.setText(String.valueOf(habMantenimiento));

            // Calcular ocupación
            double porcentajeOcupacion = totalHabitaciones > 0
                    ? (habOcupadas * 100.0) / totalHabitaciones
                    : 0;
            lblOcupacion.setText(String.format("Ocupación: %.0f%%", porcentajeOcupacion));
            pbOcupacion.setProgress(porcentajeOcupacion / 100.0);

            // Contar trabajadores por rol
            // Rol 1 = Administrador, Rol 2 = Recepcionista
            int administradores = trabajadorDao.countByRol(1L);
            int recepcionistas = trabajadorDao.countByRol(2L);
            int totalTrabajadores = administradores + recepcionistas;

            lblTotalTrabajadores.setText(String.valueOf(totalTrabajadores));
            lblAdministradores.setText(String.valueOf(administradores));
            lblRecepcionistas.setText(String.valueOf(recepcionistas));

            // Contar reservas activas (considerar variantes ACTIVO/ACTIVA)
            int reservasActivas = reservaDao.countByEstado("ACTIVO") + reservaDao.countByEstado("ACTIVA");
            lblReservasActivas.setText(String.valueOf(reservasActivas));

            // TODO: Calcular ingresos del mes desde la base de datos
            lblIngresosMes.setText("$ 0.00");

        } catch (SQLException e) {
            System.err.println("Error al cargar estadísticas: " + e.getMessage());
            e.printStackTrace();

            // Valores por defecto en caso de error
            lblTotalHabitaciones.setText("De 0 totales");
            lblHabDisponibles.setText("0");
            lblHabOcupadas.setText("0");
            lblHabMantenimiento.setText("0");
            lblOcupacion.setText("Ocupación: 0%");
            pbOcupacion.setProgress(0);
            lblTotalTrabajadores.setText("0");
            lblAdministradores.setText("0");
            lblRecepcionistas.setText("0");
            lblReservasActivas.setText("0");
            lblIngresosMes.setText("$ 0.00");
        }
    }

    public void actualizarDatos() {
        cargarEstadisticas();
        actualizarFechaHora();
    }
}
