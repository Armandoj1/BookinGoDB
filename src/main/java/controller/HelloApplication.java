package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.scheduler.ReservaScheduler;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 700);

            primaryStage.setTitle("BookinnGo - Sistema de Gestión Hotelera");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.setResizable(false);
            primaryStage.show();

            // Iniciar scheduler de mantenimiento de reservas
            ReservaScheduler.getInstance().start();

            // Detener scheduler al cerrar la aplicación
            primaryStage.setOnCloseRequest(e -> ReservaScheduler.getInstance().stop());
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación (carga de LoginView.fxml):");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
