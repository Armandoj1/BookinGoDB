package controller;

import ConexionBase.Conexion;
import dao.impl.HuespedDaoImpl;
import dao.impl.ClienteDaoImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import model.Huesped;
import model.Cliente;
import service.DatabaseConfig;
import service.impl.ClienteServiceImpl;
import service.SessionManager;

public class HuespedesController {

    @FXML
    private Label lblTotalClientes;
    @FXML
    private Label lblVIP;
    @FXML
    private Label lblRegular;
    @FXML
    private Label lblCorporativo;

    @FXML
    private TableView<Huesped> tblHuespedes;
    @FXML
    private TableColumn<Huesped, String> colId;
    @FXML
    private TableColumn<Huesped, String> colPrimerNombre;
    @FXML
    private TableColumn<Huesped, String> colSegundoNombre;
    @FXML
    private TableColumn<Huesped, String> colPrimerApellido;
    @FXML
    private TableColumn<Huesped, String> colSegundoApellido;
    @FXML
    private TableColumn<Huesped, String> colDocumento;
    @FXML
    private TableColumn<Huesped, String> colTipoDocumento;
    @FXML
    private TableColumn<Huesped, String> colEmail;
    @FXML
    private TableColumn<Huesped, String> colTelefono;
    @FXML
    private TableColumn<Huesped, String> colCategoria;
    @FXML
    private TableColumn<Huesped, String> colEstado;
    @FXML
    private Button btnEditarHuesped;
    @FXML
    private Button btnEliminarHuesped;
    @FXML
    private Button btnNuevoHuesped;

    private HuespedDaoImpl huespedDao;
    private ClienteServiceImpl clienteService;

    @FXML
    public void initialize() {
        // Inicializar DAO con configuración centralizada
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        huespedDao = new HuespedDaoImpl(conexion);
        clienteService = new ClienteServiceImpl(new ClienteDaoImpl(conexion));
        cargarHuespedes();
        cargarMetricas();
        configurarTabla();

        // Deshabilitar botones si no hay selección
        btnEditarHuesped.disableProperty().bind(tblHuespedes.getSelectionModel().selectedItemProperty().isNull());
        btnEliminarHuesped.disableProperty().bind(tblHuespedes.getSelectionModel().selectedItemProperty().isNull());

        // Handlers
        btnEditarHuesped.setOnAction(e -> editarSeleccionado());
        btnEliminarHuesped.setOnAction(e -> eliminarSeleccionado());
        if (btnNuevoHuesped != null) {
            btnNuevoHuesped.setOnAction(e -> crearNuevoHuesped());
        }

        // Restricción: recepcionista no puede eliminar huéspedes
        if (btnEliminarHuesped != null && SessionManager.getInstance().esRecepcionista()) {
            btnEliminarHuesped.setVisible(false);
            btnEliminarHuesped.setManaged(false);
        }
    }

    private void crearNuevoHuesped() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Cliente");
        dialog.setHeaderText("Completa los datos del nuevo cliente.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        TextField txtPrimerNombre = new TextField();
        TextField txtSegundoNombre = new TextField();
        TextField txtPrimerApellido = new TextField();
        TextField txtSegundoApellido = new TextField();
        ChoiceBox<String> cbTipoDocumento = new ChoiceBox<>();
        cbTipoDocumento.getItems().addAll("CC", "TI", "CE", "PP", "NIT", "DNI");
        cbTipoDocumento.setValue("DNI");
        TextField txtDocumento = new TextField();
        TextField txtEmail = new TextField();
        TextField txtTelefono = new TextField();

        grid.addRow(0, new Label("Primer Nombre *"), txtPrimerNombre);
        grid.addRow(1, new Label("Segundo Nombre"), txtSegundoNombre);
        grid.addRow(2, new Label("Primer Apellido *"), txtPrimerApellido);
        grid.addRow(3, new Label("Segundo Apellido"), txtSegundoApellido);
        grid.addRow(4, new Label("Tipo Documento *"), cbTipoDocumento);
        grid.addRow(5, new Label("Número Documento *"), txtDocumento);
        grid.addRow(6, new Label("Email *"), txtEmail);
        grid.addRow(7, new Label("Teléfono *"), txtTelefono);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    String pNom = txtPrimerNombre.getText().trim();
                    String pApe = txtPrimerApellido.getText().trim();
                    String tipoDoc = cbTipoDocumento.getValue();
                    String doc = txtDocumento.getText().trim();
                    String email = txtEmail.getText().trim();
                    String tel = txtTelefono.getText().trim();
                    if (pNom.isEmpty() || pApe.isEmpty() || tipoDoc == null || tipoDoc.isEmpty() || doc.isEmpty()) {
                        Alert warn = new Alert(Alert.AlertType.WARNING);
                        warn.setTitle("Validación");
                        warn.setHeaderText(null);
                        warn.setContentText("Campos obligatorios: Primer Nombre, Primer Apellido, Tipo Documento y Documento.");
                        warn.showAndWait();
                        return;
                    }

                    Huesped h = new Huesped();
                    h.setPrimerNombre(pNom);
                    h.setSegundoNombre(txtSegundoNombre.getText().trim());
                    h.setPrimerApellido(pApe);
                    h.setSegundoApellido(txtSegundoApellido.getText().trim());
                    h.setTipoDocumento(tipoDoc);
                    h.setDocumento(doc);
                    h.setEmail(email);
                    h.setTelefono(tel);
                    h.setEstado("ACTIVO");

                    huespedDao.createPersona(h);

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Nuevo Huésped");
                    ok.setHeaderText(null);
                    ok.setContentText("Huésped creado correctamente.");
                    ok.showAndWait();
                    cargarHuespedes();
                    cargarMetricas();
                } catch (Exception ex) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Nuevo Huésped");
                    err.setHeaderText(null);
                    err.setContentText("Error al crear: " + ex.getMessage());
                    err.showAndWait();
                }
            }
        });
    }

    private void cargarMetricas() {
        int total = tblHuespedes.getItems().size();
        long vip = tblHuespedes.getItems().stream().filter(h -> "VIP".equalsIgnoreCase(h.getCategoria())).count();
        long regular = tblHuespedes.getItems().stream().filter(h -> "REGULAR".equalsIgnoreCase(h.getCategoria())).count();
        long corporativo = tblHuespedes.getItems().stream().filter(h -> "CORPORATIVO".equalsIgnoreCase(h.getCategoria())).count();

        lblTotalClientes.setText(String.valueOf(total));
        lblVIP.setText(String.valueOf(vip));
        lblRegular.setText(String.valueOf(regular));
        lblCorporativo.setText(String.valueOf(corporativo));
    }

    private void configurarTabla() {
        tblHuespedes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colPrimerNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrimerNombre()));
        colSegundoNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSegundoNombre()));
        colPrimerApellido.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrimerApellido()));
        colSegundoApellido.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSegundoApellido()));
        colDocumento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumento()));
        colTipoDocumento.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoDocumento()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono()));
        colCategoria.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoria()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));

        // Columna de categoría con badge
        colCategoria.setCellFactory(column -> new TableCell<Huesped, String>() {
            @Override
            protected void updateItem(String categoria, boolean empty) {
                super.updateItem(categoria, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Huesped huesped = getTableRow().getItem();
                    String categoriaVal = huesped.getCategoria();
                    String display = categoriaVal != null ? categoriaVal : "SIN CATEGORÍA";
                    Label badge = new Label(display);

                    String style;
                    if ("VIP".equalsIgnoreCase(display)) {
                        style = "-fx-background-color: #1e293b; -fx-text-fill: white;";
                    } else if ("REGULAR".equalsIgnoreCase(display)) {
                        style = "-fx-background-color: #e2e8f0; -fx-text-fill: #475569;";
                    } else if ("CORPORATIVO".equalsIgnoreCase(display)) {
                        style = "-fx-background-color: #ddd6fe; -fx-text-fill: #5b21b6;";
                    } else {
                        style = "-fx-background-color: #e5e7eb; -fx-text-fill: #374151;";
                    }

                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 12; " +
                            "-fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Columna de estado con badge
        colEstado.setCellFactory(column -> new TableCell<Huesped, String>() {
            @Override
            protected void updateItem(String ignored, boolean empty) {
                super.updateItem(ignored, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Huesped h = getTableRow().getItem();
                    String estado = h.getEstado() != null ? h.getEstado().toUpperCase(java.util.Locale.ROOT) : "-";
                    Label badge = new Label(estado);
                    String style;
                    if ("ACTIVO".equals(estado)) {
                        style = "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46;";
                    } else if ("INACTIVO".equals(estado)) {
                        style = "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;";
                    } else {
                        style = "-fx-background-color: #E5E7EB; -fx-text-fill: #374151;";
                    }
                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

    }

    private void cargarHuespedes() {
        try {
            tblHuespedes.getItems().setAll(huespedDao.findAll());
        } catch (Exception e) {
            tblHuespedes.getItems().clear();
        }
    }

    private void editarSeleccionado() {
        Huesped seleccionado = tblHuespedes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        try {
            Cliente cliente = clienteService.buscarPorDocumento(seleccionado.getDocumento());
            if (cliente == null) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Editar Huésped");
                error.setHeaderText(null);
                error.setContentText("No se encontró el cliente asociado al documento: " + seleccionado.getDocumento());
                error.showAndWait();
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Editar Huésped");
            dialog.setHeaderText("Modifique los campos necesarios");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(8);

            TextField txtPrimerNombre = new TextField(cliente.getPrimerNombre());
            TextField txtSegundoNombre = new TextField(cliente.getSegundoNombre());
            TextField txtPrimerApellido = new TextField(cliente.getPrimerApellido());
            TextField txtSegundoApellido = new TextField(cliente.getSegundoApellido());
            TextField txtEmail = new TextField(cliente.getEmail());
            TextField txtTelefono = new TextField(String.valueOf(cliente.getTelefono()));
            TextField txtCategoria = new TextField(cliente.getCategoria());
            ChoiceBox<String> cbEstado = new ChoiceBox<>();
            cbEstado.getItems().addAll("ACTIVO", "INACTIVO");
            cbEstado.setValue(cliente.getEstado() != null ? cliente.getEstado() : "ACTIVO");

            grid.addRow(0, new Label("Primer nombre:"), txtPrimerNombre);
            grid.addRow(1, new Label("Segundo nombre:"), txtSegundoNombre);
            grid.addRow(2, new Label("Primer apellido:"), txtPrimerApellido);
            grid.addRow(3, new Label("Segundo apellido:"), txtSegundoApellido);
            grid.addRow(4, new Label("Email:"), txtEmail);
            grid.addRow(5, new Label("Teléfono:"), txtTelefono);
            grid.addRow(6, new Label("Categoría:"), txtCategoria);
            grid.addRow(7, new Label("Estado:"), cbEstado);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        // Validaciones básicas
                        String pNom = txtPrimerNombre.getText().trim();
                        String pApe = txtPrimerApellido.getText().trim();
                        if (pNom.isEmpty() || pApe.isEmpty()) {
                            Alert warn = new Alert(Alert.AlertType.WARNING);
                            warn.setTitle("Validación");
                            warn.setHeaderText(null);
                            warn.setContentText("Primer nombre y primer apellido son obligatorios.");
                            warn.showAndWait();
                            return;
                        }

                        long telParsed = 0;
                        try { telParsed = Long.parseLong(txtTelefono.getText().trim()); } catch (NumberFormatException ignored) {}

                        cliente.setPrimerNombre(pNom);
                        cliente.setSegundoNombre(txtSegundoNombre.getText().trim());
                        cliente.setPrimerApellido(pApe);
                        cliente.setSegundoApellido(txtSegundoApellido.getText().trim());
                        cliente.setEmail(txtEmail.getText().trim());
                        cliente.setTelefono(telParsed);
                        cliente.setCategoria(txtCategoria.getText().trim());
                        cliente.setEstado(cbEstado.getValue());

                        clienteService.actualizar(cliente);
                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("Editar Huésped");
                        ok.setHeaderText(null);
                        ok.setContentText("Huésped actualizado correctamente.");
                        ok.showAndWait();
                        cargarHuespedes();
                        cargarMetricas();
                    } catch (Exception ex) {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Editar Huésped");
                        err.setHeaderText(null);
                        err.setContentText("Error al actualizar: " + ex.getMessage());
                        err.showAndWait();
                    }
                }
            });
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Editar Huésped");
            err.setHeaderText(null);
            err.setContentText("Error al preparar la edición: " + ex.getMessage());
            err.showAndWait();
        }
    }

    private void eliminarSeleccionado() {
        Huesped seleccionado = tblHuespedes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        try {
            Cliente cliente = clienteService.buscarPorDocumento(seleccionado.getDocumento());
            if (cliente == null) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Estado Huésped");
                error.setHeaderText(null);
                error.setContentText("No se encontró el cliente asociado al documento: " + seleccionado.getDocumento());
                error.showAndWait();
                return;
            }

            boolean inactivo = "INACTIVO".equalsIgnoreCase(cliente.getEstado());
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(inactivo ? "Activar Huésped" : "Desactivar Huésped");
            confirm.setHeaderText(null);
            confirm.setContentText(inactivo ?
                    "¿Desea cambiar el estado del huésped a ACTIVO?" :
                    "¿Desea cambiar el estado del huésped a INACTIVO?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            if (inactivo) {
                clienteService.activar(cliente.getIdCliente());
            } else {
                clienteService.eliminar(cliente.getIdCliente());
            }
            cargarHuespedes();
            cargarMetricas();
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Estado Huésped");
            err.setHeaderText(null);
            err.setContentText("Error al actualizar estado: " + ex.getMessage());
            err.showAndWait();
        }
    }

    /**
     * Activa explícitamente al huésped seleccionado (PERSONA.estado = 'ACTIVO').
     */
    public void activarSeleccionado() {
        Huesped seleccionado = tblHuespedes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        try {
            Cliente cliente = clienteService.buscarPorDocumento(seleccionado.getDocumento());
            if (cliente == null) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Activar Huésped");
                error.setHeaderText(null);
                error.setContentText("No se encontró el cliente asociado al documento: " + seleccionado.getDocumento());
                error.showAndWait();
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Activar Huésped");
            confirm.setHeaderText(null);
            confirm.setContentText("¿Desea cambiar el estado del huésped a ACTIVO?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            clienteService.activar(cliente.getIdCliente());
            cargarHuespedes();
            cargarMetricas();
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Activar Huésped");
            err.setHeaderText(null);
            err.setContentText("Error al activar: " + ex.getMessage());
            err.showAndWait();
        }
    }
}
