package controller;

import ConexionBase.Conexion;
import dao.impl.ComprobanteDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Comprobante;
import model.Reserva;
import service.DatabaseConfig;
import service.impl.ComprobanteServiceImpl;
import service.impl.ReservaServiceImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class NuevoComprobanteController {

    @FXML
    private ComboBox<Reserva> cbReserva;
    @FXML
    private DatePicker dpFechaEmision;
    @FXML
    private ComboBox<String> cbTipo;
    @FXML
    private ComboBox<String> cbMetodoPago;
    @FXML
    private TextField txtMontoTotal;
    @FXML
    private ComboBox<String> cbEstado;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private Label lblTitulo;
    @FXML
    private Button btnGuardar;

    private ComprobanteDaoImpl comprobanteDao;
    private ReservaDaoImpl reservaDao;
    private ComprobanteServiceImpl comprobanteService;
    private ReservaServiceImpl reservaService;
    private Runnable onComprobanteCreado;
    private Runnable onComprobanteGuardado;
    private Comprobante comprobanteEditando;

    public void initDependencies(ComprobanteDaoImpl comprobanteDao, ReservaDaoImpl reservaDao) {
        this.comprobanteDao = comprobanteDao;
        this.reservaDao = reservaDao;
        this.comprobanteService = new ComprobanteServiceImpl(comprobanteDao);
        this.reservaService = new ReservaServiceImpl(reservaDao);
        initForm();
    }

    public void setOnComprobanteCreado(Runnable cb) {
        this.onComprobanteCreado = cb;
    }

    public void setOnComprobanteGuardado(Runnable cb) {
        this.onComprobanteGuardado = cb;
    }

    @FXML
    private void initialize() {
        // Defaults
        dpFechaEmision.setValue(LocalDate.now());
        cbTipo.setItems(FXCollections.observableArrayList("FACTURA", "RECIBO"));
        cbTipo.getSelectionModel().select("FACTURA");
        cbMetodoPago.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "TRANSFERENCIA"));
        cbMetodoPago.getSelectionModel().select("EFECTIVO");
        cbEstado.setItems(FXCollections.observableArrayList("PENDIENTE", "PAGADO", "ENPROCESO", "EMITIDO", "ANULADO"));
        cbEstado.getSelectionModel().select("PENDIENTE");

        // Actualiza monto cuando cambia la reserva
        cbReserva.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtMontoTotal.setText(String.format(java.util.Locale.US, "%.2f", newV.getTotal()));
            }
        });
    }

    private void initForm() {
        try {
            List<Reserva> reservas = reservaService.listarTodas();
            cbReserva.setItems(FXCollections.observableArrayList(reservas));
            cbReserva.setConverter(new StringConverter<>() {
                @Override
                public String toString(Reserva r) {
                    if (r == null) return "Seleccione una reserva";
                    return "Reserva #" + r.getIdReserva() + " - S/ " + String.format("% ,.2f", r.getTotal()).trim();
                }
                @Override
                public Reserva fromString(String s) { return null; }
            });
            cbReserva.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Reserva r, boolean empty) {
                    super.updateItem(r, empty);
                    setText(empty || r == null ? "Seleccione una reserva" : "Reserva #" + r.getIdReserva() + " - S/ " + String.format("% ,.2f", r.getTotal()).trim());
                }
            });
            cbReserva.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Reserva r, boolean empty) {
                    super.updateItem(r, empty);
                    setText(empty || r == null ? null : "Reserva #" + r.getIdReserva() + " - S/ " + String.format("% ,.2f", r.getTotal()).trim());
                }
            });
        } catch (SQLException e) {
            cbReserva.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Precarga datos para modo edición de comprobante.
     */
    public void setComprobanteParaEditar(Comprobante c) {
        this.comprobanteEditando = c;
        try {
            // Asegurar que las reservas estén cargadas
            if (cbReserva.getItems() == null || cbReserva.getItems().isEmpty()) {
                List<Reserva> reservas = reservaService.listarTodas();
                cbReserva.setItems(FXCollections.observableArrayList(reservas));
            }
            // Seleccionar la reserva correspondiente
            if (c.getReserva() != null) {
                Reserva match = cbReserva.getItems().stream()
                        .filter(r -> r.getIdReserva() != null && c.getReserva().getIdReserva() != null
                                && r.getIdReserva().equals(c.getReserva().getIdReserva()))
                        .findFirst().orElse(null);
                cbReserva.getSelectionModel().select(match);
            }

            dpFechaEmision.setValue(c.getFechaEmision());
            cbTipo.getSelectionModel().select(c.getTipoComprobante());
            cbMetodoPago.getSelectionModel().select(c.getMetodoPago());
            cbEstado.getSelectionModel().select(c.getEstado());
            txtMontoTotal.setText(String.format(java.util.Locale.US, "%.2f", c.getMontoTotal()));
            txtDescripcion.setText(c.getDescripcion());

            if (lblTitulo != null) lblTitulo.setText("Editar Comprobante");
            if (btnGuardar != null) btnGuardar.setText("Guardar Cambios");
        } catch (Exception ex) {
            // Silenciar errores para UX
        }
    }

    @FXML
    private void handleCancelar() {
        txtDescripcion.getScene().getWindow().hide();
    }

    @FXML
    private void handleCrear() {
        try {
            Reserva r = cbReserva.getValue();
            if (r == null) {
                showAlert("Reserva requerida", "Seleccione una reserva.");
                return;
            }

            String tipo = cbTipo.getValue();
            String metodo = cbMetodoPago.getValue();
            String estado = cbEstado.getValue();
            LocalDate fecha = dpFechaEmision.getValue();
            String descripcion = txtDescripcion.getText();
            double monto;
            try {
                String montoTexto = txtMontoTotal.getText() == null ? "" : txtMontoTotal.getText().trim();
                montoTexto = montoTexto.replace("S/", "").trim();
                monto = Double.parseDouble(montoTexto);
            } catch (Exception ex) {
                showAlert("Monto inválido", "Ingrese un monto numérico válido.");
                return;
            }

            if (comprobanteEditando != null && comprobanteEditando.getIdComprobante() != null) {
                // Actualizar
                comprobanteEditando.setReserva(r);
                comprobanteEditando.setTipoComprobante(tipo);
                comprobanteEditando.setFechaEmision(fecha);
                comprobanteEditando.setMontoTotal(monto);
                comprobanteEditando.setMetodoPago(metodo);
                comprobanteEditando.setDescripcion(descripcion);
                comprobanteEditando.setEstado(estado);

                comprobanteService.actualizar(comprobanteEditando);
                if (onComprobanteGuardado != null) onComprobanteGuardado.run();
            } else {
                // Crear
                Comprobante c = new Comprobante();
                c.setReserva(r);
                c.setTipoComprobante(tipo);
                c.setFechaEmision(fecha);
                c.setMontoTotal(monto);
                c.setMetodoPago(metodo);
                c.setDescripcion(descripcion);
                c.setEstado(estado);

                comprobanteService.crear(c);
                if (onComprobanteCreado != null) onComprobanteCreado.run();
            }

            txtDescripcion.getScene().getWindow().hide();
        } catch (SQLException ex) {
            showAlert("Error al crear", "No se pudo guardar el comprobante: " + ex.getMessage());
        } catch (IllegalArgumentException iae) {
            showAlert("Datos inválidos", iae.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}