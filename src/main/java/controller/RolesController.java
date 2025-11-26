package controller;

import ConexionBase.Conexion;
import dao.impl.RolDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.converter.DefaultStringConverter;
import model.Rol;
import service.DatabaseConfig;
import service.impl.RolServiceImpl;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.List;

public class RolesController {

    @FXML
    private TableView<Rol> tblRoles;
    @FXML
    private TableColumn<Rol, Long> colId;
    @FXML
    private TableColumn<Rol, String> colNombre;
    @FXML
    private TableColumn<Rol, String> colDescripcion;

    @FXML
    private Button btnNuevoRol;

    private RolServiceImpl rolService;
    private ObservableList<Rol> rolesData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Inicializar dependencias
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        RolDaoImpl rolDao = new RolDaoImpl(conexion);
        this.rolService = new RolServiceImpl(rolDao);

        configurarTablaEditable();

        // Evento para crear nuevo rol (diálogo simple)
        btnNuevoRol.setOnAction(e -> crearNuevoRolDialog());

        cargarRoles();
    }

    private void cargarRoles() {
        try {
            List<Rol> roles = rolService.listarRoles();
            rolesData.setAll(roles);
            tblRoles.setItems(rolesData);
        } catch (Exception e) {
            mostrarError("Error al cargar roles", e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarTablaEditable() {
        tblRoles.setEditable(true);
        // Fijar política de tamaño de columnas desde código para evitar errores de FXML
        tblRoles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("idRol"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreRol"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colNombre.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        colNombre.setOnEditCommit(event -> {
            Rol rol = event.getRowValue();
            String nuevo = event.getNewValue();
            if (nuevo == null || nuevo.trim().isEmpty()) {
                mostrarError("Validación", "El nombre del rol es obligatorio.");
                tblRoles.refresh();
                return;
            }
            rol.setNombreRol(nuevo.trim());
            actualizarRolPersistente(rol);
        });

        colDescripcion.setOnEditCommit(event -> {
            Rol rol = event.getRowValue();
            String nuevo = event.getNewValue();
            if (nuevo == null || nuevo.trim().isEmpty()) {
                mostrarError("Validación", "La descripción del rol es obligatoria.");
                tblRoles.refresh();
                return;
            }
            rol.setDescripcion(nuevo.trim());
            actualizarRolPersistente(rol);
        });
    }

    private void actualizarRolPersistente(Rol rol) {
        try {
            rolService.actualizarRol(rol);
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el rol", e.getMessage());
            e.printStackTrace();
            cargarRoles(); // revert to server state if failed
        }
    }

    // Solo edición: se retira la columna de acciones (eliminar)

    // Eliminación deshabilitada según requerimiento

    private void crearNuevoRolDialog() {
        Dialog<Rol> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Rol");
        dialog.setHeaderText("Crear un nuevo rol");

        Label lblNombre = new Label("Nombre:");
        TextField txtNombre = new TextField();
        Label lblDesc = new Label("Descripción:");
        TextField txtDesc = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(lblNombre, 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(lblDesc, 0, 1);
        grid.add(txtDesc, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType crearType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearType, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == crearType) {
                Rol r = new Rol();
                r.setNombreRol(txtNombre.getText());
                r.setDescripcion(txtDesc.getText());
                return r;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> {
            try {
                rolService.crearRol(r);
                cargarRoles();
            } catch (Exception e) {
                mostrarError("No se pudo crear el rol", e.getMessage());
            }
        });
    }

    private void mostrarError(String titulo, String detalle) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(detalle);
        alert.showAndWait();
    }
}