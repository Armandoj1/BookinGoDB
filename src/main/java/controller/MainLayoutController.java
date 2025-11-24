package controller;



import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblUsuarioActual;

    @FXML
    private Label lblRolActual;

    @FXML
    private void initialize() {
        // Cargar Dashboard por defecto
        loadView("/hotel/view/DashboardView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Botones del menú
    @FXML
    private void handleDashboard() {
        loadView("/hotel/view/DashboardView.fxml");
    }

    @FXML
    private void handleReservas() {
        loadView("/hotel/view/ReservasView.fxml");
    }

    @FXML
    private void handleHabitaciones() {
        loadView("/hotel/view/HabitacionesView.fxml");
    }

    @FXML
    private void handleHuespedes() {
        loadView("/hotel/view/HuespedesView.fxml");
    }

    @FXML
    private void handlePagos() {
        // loadView("/hotel/view/PagosView.fxml");
    }

    @FXML
    private void handleTrabajadores() {
        loadView("/hotel/view/TrabajadoresView.fxml");
    }

    @FXML
    private void handleRoles() {
        // loadView("/hotel/view/RolesView.fxml");
    }

    @FXML
    private void handleCerrarSesion() {
        // aquí luego vuelves a la pantalla de login
    }

    // Esto luego lo puedes usar según el trabajador logueado
    public void setUsuarioActual(String nombre, String rol) {
        lblUsuarioActual.setText(nombre);
        lblRolActual.setText(rol);
    }
}