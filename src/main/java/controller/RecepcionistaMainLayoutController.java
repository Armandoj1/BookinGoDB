package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import service.SessionManager;

import java.io.IOException;

public class RecepcionistaMainLayoutController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblUsuarioActual;

    @FXML
    private Label lblRolActual;

    @FXML
    private Button btnDashboard;

    @FXML
    private void initialize() {
        // Cargar información del usuario actual
        if (SessionManager.getInstance().isLoggedIn()) {
            lblUsuarioActual.setText(SessionManager.getInstance().getUsuarioActual().getNombreCompleto());
            lblRolActual.setText(SessionManager.getInstance().getUsuarioActual().getRol().getNombreRol());
        }

        // Cargar Dashboard por defecto
        loadView("/hotel/view/RecepcionistaDashboardView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error al cargar vista: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Botones del menú de navegación
    @FXML
    private void handleDashboard() {
        loadView("/hotel/view/RecepcionistaDashboardView.fxml");
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
        loadView("/hotel/view/PagosView.fxml");
    }

    @FXML
    private void handleCerrarSesion() {
        // Cerrar sesión
        SessionManager.getInstance().logout();

        try {
            // Volver a la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/LoginView.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setWidth(450);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar pantalla de login");
            e.printStackTrace();
        }
    }
}
