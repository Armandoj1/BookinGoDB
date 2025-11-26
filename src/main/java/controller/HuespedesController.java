package controller;

import service.AppContext;
import java.util.function.UnaryOperator;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import model.Huesped;
import model.Cliente;
import service.IClienteService;
import service.IHuespedService;
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

    private IHuespedService huespedService;
    private IClienteService clienteService;

    @FXML
    public void initialize() {
        // Obtener dependencias vía AppContext (DIP)
        huespedService = AppContext.getHuespedService();
        clienteService = AppContext.getClienteService();
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
        ChoiceBox<String> cbCategoria = new ChoiceBox<>();
        cbCategoria.getItems().addAll("VIP", "REGULAR", "CORPORATIVO");
        cbCategoria.setValue("REGULAR");

        // Etiquetas de error bajo los campos
        Label lblEmailError = new Label("Debes poner un correo válido");
        lblEmailError.setStyle("-fx-text-fill: red;");
        lblEmailError.setVisible(false);

        Label lblTelError = new Label("El teléfono debe iniciar con 3 y tener 10 dígitos");
        lblTelError.setStyle("-fx-text-fill: red;");
        lblTelError.setVisible(false);

        Label lblDocError = new Label("Debes poner un documento válido según el tipo");
        lblDocError.setStyle("-fx-text-fill: red;");
        lblDocError.setVisible(false);

        // Restringir Teléfono: solo dígitos, máximo 10, debe iniciar con 3
        UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.length() > 10) return null;
            if (!newText.matches("\\d*")) return null;
            if (newText.length() >= 1 && newText.charAt(0) != '3') return null;
            return change;
        };
        txtTelefono.setTextFormatter(new TextFormatter<>(phoneFilter));
        txtTelefono.textProperty().addListener((obs, oldV, newV) -> {
            // Mostrar error si no tiene los 10 dígitos todavía
            lblTelError.setVisible(newV.length() != 10);
        });

        // Restringir Documento según tipo
        UnaryOperator<TextFormatter.Change> docFilter = change -> {
            String newText = change.getControlNewText();
            String tipo = cbTipoDocumento.getValue();
            int max = 10;
            String pattern = "[A-Za-z0-9]*"; // por defecto alfanumérico
            if ("CC".equals(tipo) || "TI".equals(tipo) || "DNI".equals(tipo)) {
                pattern = "\\d*"; max = 10;
            } else if ("NIT".equals(tipo)) {
                pattern = "[\\d-]*"; max = 12;
            } else if ("CE".equals(tipo) || "PP".equals(tipo)) {
                // forzar mayúsculas al escribir
                if (change.getText() != null) {
                    change.setText(change.getText().toUpperCase());
                }
                pattern = "[A-Z0-9]*"; max = 12;
            }
            if (newText.length() > max) return null;
            if (!newText.matches(pattern)) return null;
            return change;
        };
        txtDocumento.setTextFormatter(new TextFormatter<>(docFilter));
        Runnable docValidate = () -> {
            String tipo = cbTipoDocumento.getValue();
            String doc = txtDocumento.getText().trim();
            boolean ok;
            if ("CC".equals(tipo) || "TI".equals(tipo) || "DNI".equals(tipo)) {
                ok = doc.matches("\\d{10}");
            } else if ("NIT".equals(tipo)) {
                ok = doc.matches("[\\d-]{9,12}");
            } else { // CE/PP
                ok = doc.matches("[A-Z0-9]{8,12}");
            }
            lblDocError.setVisible(!ok);
        };
        txtDocumento.textProperty().addListener((o, ov, nv) -> docValidate.run());
        cbTipoDocumento.valueProperty().addListener((o, ov, nv) -> docValidate.run());

        // Restringir Email: solo dominios gmail.com u hotmail.com
        UnaryOperator<TextFormatter.Change> emailFilter = change -> {
            String newText = change.getControlNewText();
            // Permitir incremental: localpart y luego dominio, máximo un '@'
            int ats = newText.length() - newText.replace("@", "").length();
            if (ats > 1) return null;
            if (!newText.matches("[\\w.%+\\-]*(@.*)?")) return null;
            if (newText.contains("@")) {
                int at = newText.indexOf('@');
                String dom = at >= 0 ? newText.substring(at + 1) : "";
                if (!dom.isEmpty() && !("gmail.com".startsWith(dom) || "hotmail.com".startsWith(dom))) {
                    return null;
                }
            }
            return change;
        };
        txtEmail.setTextFormatter(new TextFormatter<>(emailFilter));
        txtEmail.textProperty().addListener((obs, o, n) -> {
            boolean valid = n.matches("^[\\w.%+\\-]+@(gmail|hotmail)\\.com$");
            lblEmailError.setVisible(!valid);
        });

        grid.addRow(0, new Label("Primer Nombre *"), txtPrimerNombre);
        grid.addRow(1, new Label("Segundo Nombre"), txtSegundoNombre);
        grid.addRow(2, new Label("Primer Apellido *"), txtPrimerApellido);
        grid.addRow(3, new Label("Segundo Apellido"), txtSegundoApellido);
        grid.addRow(4, new Label("Tipo Documento *"), cbTipoDocumento);
        grid.addRow(5, new Label("Número Documento *"), txtDocumento);
        grid.add(lblDocError, 1, 6);
        grid.addRow(7, new Label("Email *"), txtEmail);
        grid.add(lblEmailError, 1, 8);
        grid.addRow(9, new Label("Teléfono *"), txtTelefono);
        grid.add(lblTelError, 1, 10);
        grid.addRow(11, new Label("Categoría *"), cbCategoria);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Evitar que se cierre el formulario si la validación básica falla
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String pNom = txtPrimerNombre.getText().trim();
            String pApe = txtPrimerApellido.getText().trim();
            String tipoDoc = cbTipoDocumento.getValue();
            String doc = txtDocumento.getText().trim();
            String email = txtEmail.getText().trim();
            String tel = txtTelefono.getText().trim();
            String categoria = cbCategoria.getValue();

            if (pNom.isEmpty() || pApe.isEmpty() || tipoDoc == null || tipoDoc.isEmpty() || doc.isEmpty() || email.isEmpty() || tel.isEmpty()) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Validación");
                warn.setHeaderText(null);
                warn.setContentText("Campos obligatorios: Primer Nombre, Primer Apellido, Tipo Documento, Documento, Email y Teléfono.");
                warn.showAndWait();
                event.consume(); // NO cerrar el diálogo
                return;
            }

            // Validaciones adicionales para no cerrar el diálogo
            if (categoria == null || categoria.isEmpty()) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Validación");
                warn.setHeaderText(null);
                warn.setContentText("Seleccione una categoría: VIP, REGULAR o CORPORATIVO.");
                warn.showAndWait();
                event.consume();
                return;
            }

            if (!tel.matches("\\d{10}")) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Validación");
                warn.setHeaderText(null);
                warn.setContentText("El teléfono debe ser numérico de 10 dígitos.");
                warn.showAndWait();
                event.consume();
                return;
            }

            if (!email.matches("^[\\w-.]+@[\\w-]+\\.[A-Za-z]{2,}$")) {
                Alert warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("Validación");
                warn.setHeaderText(null);
                warn.setContentText("Ingrese un email válido.");
                warn.showAndWait();
                event.consume();
                return;
            }

            // Intentar crear el cliente aquí; si hay duplicados u otro error, NO cerrar el diálogo
            try {
                Cliente nuevo = new Cliente();
                nuevo.setPrimerNombre(pNom);
                nuevo.setSegundoNombre(txtSegundoNombre.getText().trim());
                nuevo.setPrimerApellido(pApe);
                nuevo.setSegundoApellido(txtSegundoApellido.getText().trim());
                nuevo.setTipoDocumento(tipoDoc);
                nuevo.setDocumento(doc);
                nuevo.setEmail(email);
                nuevo.setTelefono(Long.parseLong(tel));
                nuevo.setEstado("ACTIVO");
                nuevo.setCategoria(categoria);

                clienteService.crearConProcedimiento(nuevo);

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Nuevo Cliente");
                ok.setHeaderText(null);
                ok.setContentText("Cliente creado correctamente.");
                ok.showAndWait();
                cargarHuespedes();
                cargarMetricas();
                // No consumir el evento: permitirá que el diálogo se cierre tras crear
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Nuevo Cliente");
                err.setHeaderText(null);
                err.setContentText("Error: " + ex.getMessage());
                err.showAndWait();
                // Mantener el formulario abierto para que el usuario corrija
                event.consume();
            }
        });
        // Mostrar el diálogo; la lógica de creación y validación ya está en el filtro del botón OK
        dialog.showAndWait();
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
            var lista = huespedService.listarTodos();
            // Recepcionista: solo ver huéspedes habilitados (ACTIVO)
            if (SessionManager.getInstance().esRecepcionista()) {
                lista = lista.stream()
                        .filter(h -> h.getEstado() != null && h.getEstado().equalsIgnoreCase("ACTIVO"))
                        .toList();
            }
            tblHuespedes.getItems().setAll(lista);
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

            // Etiquetas de error
            Label lblEmailError = new Label("Debes poner un correo válido");
            lblEmailError.setStyle("-fx-text-fill: red;");
            lblEmailError.setVisible(false);
            Label lblTelError = new Label("El teléfono debe iniciar con 3 y tener 10 dígitos");
            lblTelError.setStyle("-fx-text-fill: red;");
            lblTelError.setVisible(false);

            // Filtros de entrada
            UnaryOperator<TextFormatter.Change> phoneFilter = change -> {
                String newText = change.getControlNewText();
                if (newText.length() > 10) return null;
                if (!newText.matches("\\d*")) return null;
                if (newText.length() >= 1 && newText.charAt(0) != '3') return null;
                return change;
            };
            txtTelefono.setTextFormatter(new TextFormatter<>(phoneFilter));
            txtTelefono.textProperty().addListener((o, ov, nv) -> lblTelError.setVisible(nv.length() != 10));

            UnaryOperator<TextFormatter.Change> emailFilter = change -> {
                String newText = change.getControlNewText();
                int ats = newText.length() - newText.replace("@", "").length();
                if (ats > 1) return null;
                if (!newText.matches("[\\w.%+\\-]*(@.*)?")) return null;
                if (newText.contains("@")) {
                    int at = newText.indexOf('@');
                    String dom = at >= 0 ? newText.substring(at + 1) : "";
                    if (!dom.isEmpty() && !("gmail.com".startsWith(dom) || "hotmail.com".startsWith(dom))) {
                        return null;
                    }
                }
                return change;
            };
            txtEmail.setTextFormatter(new TextFormatter<>(emailFilter));
            txtEmail.textProperty().addListener((o, ov, nv) -> {
                boolean valid = nv.matches("^[\\w.%+\\-]+@(gmail|hotmail)\\.com$");
                lblEmailError.setVisible(!valid);
            });

            grid.addRow(0, new Label("Primer nombre:"), txtPrimerNombre);
            grid.addRow(1, new Label("Segundo nombre:"), txtSegundoNombre);
            grid.addRow(2, new Label("Primer apellido:"), txtPrimerApellido);
            grid.addRow(3, new Label("Segundo apellido:"), txtSegundoApellido);
            grid.addRow(4, new Label("Email:"), txtEmail);
            grid.add(lblEmailError, 1, 5);
            grid.addRow(6, new Label("Teléfono:"), txtTelefono);
            grid.add(lblTelError, 1, 7);
            grid.addRow(8, new Label("Categoría:"), txtCategoria);
            grid.addRow(9, new Label("Estado:"), cbEstado);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Evitar cerrar el diálogo si teléfono o email son inválidos
            Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.addEventFilter(ActionEvent.ACTION, ev -> {
                String telText = txtTelefono.getText().trim();
                String emailText = txtEmail.getText().trim();
                boolean telInvalid = telText.length() != 10;
                boolean emailInvalid = !emailText.matches("^[\\w.%+\\-]+@(gmail|hotmail)\\.com$");
                lblTelError.setVisible(telInvalid);
                lblEmailError.setVisible(emailInvalid);
                if (telInvalid || emailInvalid) {
                    ev.consume();
                }
            });

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

                        // Validaciones de teléfono y email
                        String telText = txtTelefono.getText().trim();
                        String emailText = txtEmail.getText().trim();
                        if (telText.length() != 10) {
                            lblTelError.setVisible(true);
                            return;
                        }
                        if (!emailText.matches("^[\\w.%+\\-]+@(gmail|hotmail)\\.com$")) {
                            lblEmailError.setVisible(true);
                            return;
                        }

                        long telParsed = Long.parseLong(telText);

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
