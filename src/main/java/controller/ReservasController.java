package controller;

import ConexionBase.Conexion;
import dao.impl.ReservaDaoImpl;
import model.Reserva;
import model.Cliente;
import model.Habitacion;
import service.DatabaseConfig;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReservasController {

    @FXML
    private Label lblTotalReservas;
    @FXML
    private Label lblConfirmadas;
    @FXML
    private Label lblPendientesCheckIn;
    @FXML
    private Label lblActivas;

    @FXML
    private TableView<Reserva> tblReservas;
    @FXML
    private TableColumn<Reserva, String> colId;
    @FXML
    private TableColumn<Reserva, String> colIdCliente;
    @FXML
    private TableColumn<Reserva, String> colDocumento;
    @FXML
    private TableColumn<Reserva, String> colIdTrabajador;
    @FXML
    private TableColumn<Reserva, String> colIdHabitacion;
    @FXML
    private TableColumn<Reserva, String> colFechaReserva;
    @FXML
    private TableColumn<Reserva, String> colEntrada;
    @FXML
    private TableColumn<Reserva, String> colSalida;
    @FXML
    private TableColumn<Reserva, String> colTipoReserva;
    @FXML
    private TableColumn<Reserva, String> colObservacion;
    @FXML
    private TableColumn<Reserva, String> colTotal;
    @FXML
    private TableColumn<Reserva, String> colEstado;

    @FXML
    private Button btnNuevaReserva;
    @FXML
    private Button btnEditarReserva;

    private ReservaDaoImpl reservaDao;

    @FXML
    public void initialize() {
        // Inicializar DAO con la configuración centralizada
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        reservaDao = new ReservaDaoImpl(conexion);
        configurarTabla();
        System.out.println("[ReservasController] Inicializando vista de Reservas...");
        cargarReservas();
        cargarMetricas();
    }

    private void cargarMetricas() {
        try {
            int total = reservaDao.countAll();
            // Confirmadas deben mostrar EN_PROCESO (compatibilidad con EN_CURSO)
            int confirmadas = reservaDao.countByEstado("EN_PROCESO") + reservaDao.countByEstado("EN_CURSO");

            // Pendientes deben mostrar las reservas en ACTIVA/ACTIVO
            int pendientes = reservaDao.countByEstado("ACTIVO") + reservaDao.countByEstado("ACTIVA");

            // El otro card debe mostrar las FINALIZADAS
            int finalizadas = reservaDao.countByEstado("FINALIZADA");

            lblTotalReservas.setText(String.valueOf(total));
            lblConfirmadas.setText(String.valueOf(confirmadas));
            lblPendientesCheckIn.setText(String.valueOf(pendientes));
            lblActivas.setText(String.valueOf(finalizadas));
        } catch (SQLException e) {
            lblTotalReservas.setText("0");
            lblConfirmadas.setText("0");
            lblActivas.setText("0");
            lblPendientesCheckIn.setText("0");
        }
    }

    private void configurarTabla() {
        tblReservas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdReserva())));
        colIdCliente.setCellValueFactory(data -> {
            Cliente c = data.getValue().getCliente();
            String id = (c == null) ? "" : String.valueOf(c.getIdCliente());
            return new SimpleStringProperty(id);
        });
        colDocumento.setCellValueFactory(data -> {
            Cliente c = data.getValue().getCliente();
            String doc = (c == null || c.getDocumento() == null) ? "" : c.getDocumento();
            return new SimpleStringProperty(doc);
        });
        colIdTrabajador.setCellValueFactory(data -> {
            var t = data.getValue().getTrabajador();
            String id = (t == null) ? "" : String.valueOf(t.getIdTrabajador());
            return new SimpleStringProperty(id);
        });
        colIdHabitacion.setCellValueFactory(data -> {
            Habitacion h = data.getValue().getHabitacion();
            String id = (h == null) ? "" : String.valueOf(h.getIdHabitacion());
            return new SimpleStringProperty(id);
        });
        colFechaReserva.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaReserva() == null ? "" : fmt.format(data.getValue().getFechaReserva())));
        colEntrada.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaEntrada() == null ? "" : fmt.format(data.getValue().getFechaEntrada())));
        colSalida.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaSalida() == null ? "" : fmt.format(data.getValue().getFechaSalida())));
        colTipoReserva.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTipoReserva() == null ? "" : data.getValue().getTipoReserva()));
        colObservacion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getObservacion() == null ? "" : data.getValue().getObservacion()));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty("COP $ " + String.format("%,.2f", data.getValue().getTotal()).replace(',', 'X').replace('.', ',').replace('X', '.')));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEstado() == null ? "" : data.getValue().getEstado()));
    }

    private void cargarReservas() {
        try {
            System.out.println("[ReservasController] Cargando reservas...");
            List<Reserva> reservas = reservaDao.findAll();
            System.out.println("[ReservasController] Reservas obtenidas: " + (reservas == null ? 0 : reservas.size()));
            if (reservas != null) {
                for (Reserva r : reservas) {
                    System.out.println("  - Reserva ID=" + r.getIdReserva()
                            + ", Cliente=" + (r.getCliente() != null ? r.getCliente().getIdCliente() : "null")
                            + ", Documento=" + (r.getCliente() != null ? r.getCliente().getDocumento() : "null")
                            + ", Trabajador=" + (r.getTrabajador() != null ? r.getTrabajador().getIdTrabajador() : "null")
                            + ", Habitacion=" + (r.getHabitacion() != null ? r.getHabitacion().getIdHabitacion() : "null")
                            + ", FechaReserva=" + r.getFechaReserva()
                            + ", Estado=" + r.getEstado()
                            + ", Total=" + r.getTotal());
                }
                tblReservas.getItems().setAll(reservas);
            } else {
                tblReservas.getItems().clear();
            }
            if (tblReservas.getItems().isEmpty()) {
                System.out.println("[ReservasController] Tabla vacía tras cargar. Revisa el DAO o datos en BD.");
            }
        } catch (Exception e) {
            System.out.println("[ReservasController] Error cargando reservas: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            tblReservas.getItems().clear();
        }
    }

    @FXML
    private void handleNuevaReserva() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/NuevaReservaDialog.fxml"));
            Parent root = loader.load();
            NuevaReservaController controller = loader.getController();

            // Pasar DAO y callback para refrescar
            controller.initDependencies(reservaDao);
            controller.setOnReservaCreada(() -> {
                cargarReservas();
                cargarMetricas();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Nueva Reserva");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            // Podrías mostrar un Alert si quieres
        }
    }

    @FXML
    private void handleEditarReserva() {
        Reserva seleccionada = tblReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleccione una reserva en la tabla para editar.");
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/NuevaReservaDialog.fxml"));
            Parent root = loader.load();
            NuevaReservaController controller = loader.getController();

            controller.initDependencies(reservaDao);
            controller.setReservaParaEditar(seleccionada);
            controller.setOnReservaGuardada(() -> {
                cargarReservas();
                cargarMetricas();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Editar Reserva");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el editor: " + e.getMessage());
            a.setHeaderText(null);
            a.showAndWait();
        }
    }
}
