package controller;

import ConexionBase.Conexion;
import dao.impl.ComprobanteDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import model.Comprobante;
import service.DatabaseConfig;
import service.impl.ComprobanteServiceImpl;
import service.impl.ReservaServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PagosController {

    @FXML
    private Button btnNuevoComprobante;
    @FXML
    private Button btnEditarComprobante;

    @FXML
    private TableView<Comprobante> tblPagos;
    @FXML
    private TableColumn<Comprobante, String> colIdComprobante;
    @FXML
    private TableColumn<Comprobante, String> colIdReserva;
    @FXML
    private TableColumn<Comprobante, String> colTipoComprobante;
    @FXML
    private TableColumn<Comprobante, String> colFechaEmision;
    @FXML
    private TableColumn<Comprobante, String> colMontoTotal;
    @FXML
    private TableColumn<Comprobante, String> colMetodoPago;
    @FXML
    private TableColumn<Comprobante, String> colDescripcion;
    @FXML
    private TableColumn<Comprobante, String> colEstado;

    private ComprobanteServiceImpl comprobanteService;
    private ReservaServiceImpl reservaService;
    private ComprobanteDaoImpl comprobanteDao;
    private ReservaDaoImpl reservaDao;

    @FXML
    public void initialize() {
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        this.comprobanteDao = new ComprobanteDaoImpl(conexion);
        this.reservaDao = new ReservaDaoImpl(conexion);
        this.comprobanteService = new ComprobanteServiceImpl(comprobanteDao);
        this.reservaService = new ReservaServiceImpl(reservaDao);

        configurarTabla();
        cargarComprobantes();
    }

    private void configurarTabla() {
        tblPagos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colIdComprobante.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdComprobante())));
        colIdReserva.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getReserva() != null ? String.valueOf(data.getValue().getReserva().getIdReserva()) : ""));
        colTipoComprobante.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoComprobante()));
        colFechaEmision.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaEmision() != null ? data.getValue().getFechaEmision().format(fmt) : ""));
        colMontoTotal.setCellValueFactory(data -> new SimpleStringProperty(String.format("S/ %,.2f", data.getValue().getMontoTotal())));
        colMetodoPago.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMetodoPago()));
        colDescripcion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescripcion()));

        colEstado.setCellFactory(column -> new TableCell<Comprobante, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Comprobante c = getTableRow().getItem();
                    Label badge = new Label(c.getEstado());
                    String style = c.getEstado() != null && c.getEstado().equalsIgnoreCase("PAGADO")
                            ? "-fx-background-color: #d4edda; -fx-text-fill: #155724;"
                            : "-fx-background-color: #fff3cd; -fx-text-fill: #856404;";
                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 12; " +
                            "-fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));
    }

    private void cargarComprobantes() {
        try {
            List<Comprobante> lista = comprobanteService.listarTodos();
            tblPagos.getItems().setAll(lista);
        } catch (SQLException e) {
            tblPagos.getItems().clear();
        }
    }

    @FXML
    private void handleNuevoComprobante() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/NuevoComprobanteDialog.fxml"));
            Parent root = loader.load();

            NuevoComprobanteController controller = loader.getController();
            controller.initDependencies(comprobanteDao, reservaDao);
            controller.setOnComprobanteCreado(this::cargarComprobantes);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Nuevo Comprobante");
            stage.initOwner(((Stage) tblPagos.getScene().getWindow()));
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el diálogo: " + ex.getMessage());
            a.setHeaderText(null);
            a.showAndWait();
        }
    }

    @FXML
    private void handleEditarComprobante() {
        Comprobante seleccionado = tblPagos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Seleccione un comprobante en la tabla para editar.");
            a.setHeaderText(null);
            a.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/NuevoComprobanteDialog.fxml"));
            Parent root = loader.load();

            NuevoComprobanteController controller = loader.getController();
            controller.initDependencies(comprobanteDao, reservaDao);
            controller.setComprobanteParaEditar(seleccionado);
            controller.setOnComprobanteGuardado(this::cargarComprobantes);

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Editar Comprobante");
            stage.initOwner(((Stage) tblPagos.getScene().getWindow()));
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el diálogo: " + ex.getMessage());
            a.setHeaderText(null);
            a.showAndWait();
        }
    }
}
