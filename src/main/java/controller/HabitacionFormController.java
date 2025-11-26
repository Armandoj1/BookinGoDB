package controller;

import service.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Habitacion;
import service.IHabitacionService;

import java.sql.SQLException;

public class HabitacionFormController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNumero;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtCapacidad;
    @FXML private TextField txtPiso;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtCaracteristicas;
    @FXML private Button btnGuardar;

    private IHabitacionService habitacionService;
    private Habitacion habitacionEdicion;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        cbTipo.getItems().setAll("SENCILLA", "DOBLE", "SUITE", "FAMILIAR", "PRESIDENCIAL");
        // Estados válidos de HABITACION: DISPONIBLE, OCUPADO
        cbEstado.getItems().setAll("DISPONIBLE", "OCUPADO");

        // Dependencia por defecto desde AppContext (DIP)
        habitacionService = AppContext.getHabitacionService();
    }

    public void initDependencies(IHabitacionService service) {
        if (service != null) {
            this.habitacionService = service;
        }
    }

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    public void setHabitacion(Habitacion h) {
        this.habitacionEdicion = h;
        boolean edicion = h != null && h.getIdHabitacion() != null;
        lblTitulo.setText(edicion ? "Editar Habitación" : "Nueva Habitación");
        btnGuardar.setText(edicion ? "Guardar Cambios" : "Crear Habitación");

        if (h != null) {
            txtNumero.setText(h.getNumero());
            cbTipo.getSelectionModel().select(h.getTipoHabitacion());
            txtPrecio.setText(String.valueOf((long) h.getPrecio()));
            cbEstado.getSelectionModel().select(h.getEstado());
            txtCapacidad.setText(String.valueOf(h.getCapacidad()));
            txtPiso.setText(String.valueOf(h.getPiso()));
            txtDescripcion.setText(h.getDescripcion());
            txtCaracteristicas.setText(h.getCaracteristicas());
        } else {
            cbTipo.getSelectionModel().selectFirst();
            cbEstado.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleCancelar() {
        // Cerrar la ventana
        btnGuardar.getScene().getWindow().hide();
    }

    @FXML
    private void handleGuardar() {
        try {
            Habitacion datos = (habitacionEdicion != null) ? habitacionEdicion : new Habitacion();

            String numero = noVacio(txtNumero.getText(), "Número de habitación");
            String tipo = noVacio(cbTipo.getValue(), "Tipo de habitación");
            String estado = noVacio(cbEstado.getValue(), "Estado");
            int capacidad = parseEntero(txtCapacidad.getText(), "Capacidad");
            int piso = parseEntero(txtPiso.getText(), "Piso");
            double precio = parseDouble(txtPrecio.getText(), "Precio (COP)");
            String descripcion = noVacio(txtDescripcion.getText(), "Descripción");
            String caracteristicas = noVacio(txtCaracteristicas.getText(), "Características");

            datos.setNumero(numero);
            datos.setTipoHabitacion(tipo);
            datos.setPrecio(precio);
            datos.setEstado(estado);
            datos.setCapacidad(capacidad);
            datos.setPiso(piso);
            datos.setDescripcion(descripcion);
            datos.setCaracteristicas(caracteristicas);

            if (datos.getIdHabitacion() == null) {
                habitacionService.crear(datos);
                showInfo("Habitación creada", "La habitación se creó correctamente.");
            } else {
                habitacionService.actualizar(datos);
                showInfo("Cambios guardados", "La habitación se actualizó correctamente.");
            }

            if (onSaved != null) onSaved.run();
            handleCancelar();
        } catch (IllegalArgumentException e) {
            showError("Validación", e.getMessage());
        } catch (SQLException e) {
            showError("Error de BD", e.getMessage());
        } catch (Exception e) {
            showError("Error", "Ocurrió un error al guardar la habitación.");
        }
    }

    private String noVacio(String v, String campo) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(campo + " es obligatorio.");
        return v.trim();
    }

    private int parseEntero(String v, String campo) {
        try { return Integer.parseInt(v.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(campo + " debe ser un número entero."); }
    }

    private double parseDouble(String v, String campo) {
        try {
            String clean = v.replace("COP", "").replace("$", "").replace(",", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) { throw new IllegalArgumentException(campo + " debe ser un número."); }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}