package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Trabajador;
import service.AuthService;
import service.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    private AuthService authService;

    @FXML
    private void initialize() {
        authService = new AuthService();
        // Ocultar mensaje de error al inicio
        lblError.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText();

        // Validaciones básicas
        if (usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }

        // Deshabilitar botón mientras se procesa
        btnLogin.setDisable(true);
        lblError.setVisible(false);

        // Ejecutar login en un thread separado para no bloquear UI
        new Thread(() -> {
            try {
                Trabajador trabajador = authService.loginTrabajador(usuario, contrasena);

                Platform.runLater(() -> {
                    if (trabajador != null) {
                        // Login exitoso - guardar sesión
                        SessionManager.getInstance().login(trabajador);

                        // Cargar el layout correspondiente según el rol
                        cargarMainLayout(trabajador);
                    } else {
                        // Login fallido
                        mostrarError("Usuario o contraseña incorrectos");
                        btnLogin.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarError("Error al conectar con la base de datos: " + e.getMessage());
                    btnLogin.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }

    private void cargarMainLayout(Trabajador trabajador) {
        try {
            String fxmlPath;
            String titulo;

            // Determinar qué layout cargar según el rol
            if (SessionManager.getInstance().esAdministrador()) {
                fxmlPath = "/hotel/view/AdminMainLayoutV2.fxml";
                titulo = "BookinnGo - Administrador";
            } else if (SessionManager.getInstance().esRecepcionista()) {
                fxmlPath = "/hotel/view/RecepcionistaMainLayout.fxml";
                titulo = "BookinnGo - Recepcionista";
            } else {
                // Por defecto, cargar layout de recepcionista para otros roles
                fxmlPath = "/hotel/view/RecepcionistaMainLayout.fxml";
                titulo = "BookinnGo - " + trabajador.getRol().getNombreRol();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Obtener el stage actual
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.setMaximized(true); // Maximizar ventana
            stage.show();

        } catch (IOException e) {
            mostrarError("Error al cargar la interfaz principal");
            e.printStackTrace();
            btnLogin.setDisable(false);
        }
    }
}
