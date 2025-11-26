package controller;

import service.AppContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Habitacion;
import service.IHabitacionService;
import service.SessionManager;

public class HabitacionesController {

    @FXML
    private GridPane gridHabitaciones;
    @FXML
    private Button btnNuevaHabitacion;

    private IHabitacionService habitacionService;

    @FXML
    public void initialize() {
        habitacionService = AppContext.getHabitacionService();
        // Restricción: recepcionista no puede crear habitaciones
        if (btnNuevaHabitacion != null && SessionManager.getInstance().esRecepcionista()) {
            btnNuevaHabitacion.setVisible(false);
            btnNuevaHabitacion.setManaged(false);
        }
        cargarHabitaciones();
    }

    private void cargarHabitaciones() {
        gridHabitaciones.getChildren().clear();
        int row = 0;
        int col = 0;

        try {
            java.util.List<Habitacion> habitaciones = habitacionService.listarTodas();
            for (Habitacion hab : habitaciones) {
                VBox card = crearTarjetaHabitacion(hab);
                gridHabitaciones.add(card, col, row);

                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        } catch (Exception e) {
            // Silenciar errores de carga para no romper la UI
        }
    }

    private VBox crearTarjetaHabitacion(Habitacion hab) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-padding: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // Header con número y estado
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblNumero = new Label(hab.getNumero());
        lblNumero.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label lblEstado = new Label(hab.getEstado());
        String estadoColor = switch (hab.getEstado()) {
            case "DISPONIBLE" -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
            case "OCUPADO" -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            default -> "-fx-background-color: #e2e8f0; -fx-text-fill: #475569;";
        };
        lblEstado.setStyle(estadoColor + " -fx-padding: 3 10; -fx-background-radius: 10; " +
                "-fx-font-weight: bold; -fx-font-size: 9px;");

        header.getChildren().addAll(lblNumero, spacer, lblEstado);

        // Tipo y Piso en una línea
        HBox infoPrincipal = new HBox(15);
        infoPrincipal.setAlignment(Pos.CENTER_LEFT);

        Label lblTipo = new Label(hab.getTipoHabitacion());
        lblTipo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        Label lblPiso = new Label("Piso " + hab.getPiso());
        lblPiso.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        infoPrincipal.getChildren().addAll(lblTipo, lblPiso);

        // Detalles (Piso, Capacidad, Precio COP)
        VBox detalles = new VBox(6);
        detalles.setAlignment(Pos.CENTER_LEFT);

        Label lblPisoDet = new Label("Piso: " + hab.getPiso());
        lblPisoDet.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f2937;");

        Label lblCapacidad = new Label("Capacidad: " + hab.getCapacidad() + " personas");
        lblCapacidad.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f2937;");

        String precioCop = "COP $ " + String.format("% ,.2f", hab.getPrecio()).replace(',', 'X').replace('.', ',').replace('X', '.');
        Label lblPrecio = new Label("Precio: " + precioCop);
        lblPrecio.setStyle("-fx-font-size: 12px; -fx-text-fill: #1f2937; -fx-font-weight: bold;");

        // Comodidades
        Label lblComodidades = new Label(hab.getCaracteristicas());
        lblComodidades.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        detalles.getChildren().addAll(lblPisoDet, lblCapacidad, lblPrecio, lblComodidades);

        // Botones de acción compactos
        HBox acciones = new HBox(8);
        acciones.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("Editar");
        btnEditar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 10px; " +
                "-fx-padding: 6 12; -fx-background-radius: 6;");

        btnEditar.setOnAction(e -> abrirFormulario(hab));

        // Restricción: recepcionista no puede eliminar habitaciones
        if (SessionManager.getInstance().esRecepcionista()) {
            acciones.getChildren().addAll(btnEditar);
        } else {
            Button btnEliminar = new Button("Eliminar");
            btnEliminar.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 10px; " +
                    "-fx-padding: 6 12; -fx-background-radius: 6;");
            acciones.getChildren().addAll(btnEditar, btnEliminar);
        }

        card.getChildren().addAll(header, infoPrincipal, detalles, acciones);

        return card;
    }

    @FXML
    private void handleNuevaHabitacion() {
        abrirFormulario(null);
    }

    private void abrirFormulario(Habitacion h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/view/HabitacionFormView.fxml"));
            Parent root = loader.load();
            HabitacionFormController controller = loader.getController();
            controller.initDependencies(habitacionService);
            controller.setHabitacion(h);
            controller.setOnSaved(this::cargarHabitaciones);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(h != null ? "Editar Habitación" : "Nueva Habitación");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            // Silenciar para no romper UX
        }
    }
}
