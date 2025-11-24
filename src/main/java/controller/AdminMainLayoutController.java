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

public class AdminMainLayoutController {

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
        loadView("/hotel/view/AdminDashboardViewV2.fxml");
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
        loadView("/hotel/view/AdminDashboardViewV2.fxml");
    }

    @FXML
    private void handleReservas() {
        System.out.println("Navegando a Reservas...");
        loadView("/hotel/view/ReservasView.fxml");
    }

    @FXML
    private void handleHabitaciones() {
        System.out.println("Navegando a Habitaciones...");
        loadView("/hotel/view/HabitacionesView.fxml");
    }

    @FXML
    private void handleHuespedes() {
        System.out.println("Navegando a Huéspedes...");
        loadView("/hotel/view/HuespedesView.fxml");
    }

    @FXML
    private void handlePagos() {
        System.out.println("Navegando a Pagos...");
        loadView("/hotel/view/PagosView.fxml");
    }

    @FXML
    private void handleTrabajadores() {
        System.out.println("Navegando a Trabajadores...");
        loadView("/hotel/view/TrabajadoresView.fxml");
    }

    @FXML
    private void handleRoles() {
        System.out.println("Navegando a Roles...");
        loadView("/hotel/view/RolesView.fxml");
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
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar pantalla de login");
            e.printStackTrace();
        }
    }
}
