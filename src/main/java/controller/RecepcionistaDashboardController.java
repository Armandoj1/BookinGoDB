package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.MockData;

public class RecepcionistaDashboardController {

    @FXML
    private Label lblFechaHora;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblCheckInsHoy;

    @FXML
    private Label lblCheckOutsHoy;

    @FXML
    private Label lblHabDisponibles;

    @FXML
    private Label lblHabOcupadas;

    @FXML
    private Label lblBadgePendientes;

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        // Cargar fecha y hora
        lblFechaHora.setText("Sábado, 22 De Noviembre De 2025");
        lblUsuario.setText("Maria Rodriguez - Turno (10:03)");

        // Cargar métricas
        lblCheckInsHoy.setText(String.valueOf(MockData.getCheckInsHoy()));
        lblCheckOutsHoy.setText(String.valueOf(MockData.getCheckOutsHoy()));
        lblHabDisponibles.setText(String.valueOf(MockData.getHabitacionesDisponibles()));
        lblHabOcupadas.setText(String.valueOf(MockData.getHabitacionesOcupadas()));
        lblBadgePendientes.setText(String.valueOf(MockData.getCheckInsHoy()));
    }
}
