package controller;

import ConexionBase.Conexion;
import dao.impl.RolDaoImpl;
import dao.impl.TrabajadorDaoImpl;
import dao.impl.TrabajadorListadoDaoImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import model.Rol;
import model.Trabajador;
import service.DatabaseConfig;
import service.impl.TrabajadorServiceImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class TrabajadoresController {

    @FXML
    private TextField txtBuscar;
    @FXML
    private Button btnNuevoTrabajador;

    @FXML
    private TableView<Trabajador> tblTrabajadores;
    @FXML
    private TableColumn<Trabajador, String> colId;
    @FXML
    private TableColumn<Trabajador, String> colPrimerNombre;
    @FXML
    private TableColumn<Trabajador, String> colSegundoNombre;
    @FXML
    private TableColumn<Trabajador, String> colPrimerApellido;
    @FXML
    private TableColumn<Trabajador, String> colSegundoApellido;
    @FXML
    private TableColumn<Trabajador, String> colUsuario;
    @FXML
    private TableColumn<Trabajador, String> colRol;
    @FXML
    private TableColumn<Trabajador, String> colSueldo;
    @FXML
    private TableColumn<Trabajador, String> colEstado;
    @FXML
    private Button btnEditarTrabajador;
    @FXML
    private Button btnEliminarTrabajador;

    private TrabajadorListadoDaoImpl trabajadorDao;
    private final ObservableList<Trabajador> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        trabajadorDao = new TrabajadorListadoDaoImpl(conexion);
        configurarTabla();
        cargarTrabajadores();
        configurarBusqueda();

        // Deshabilitar botones si no hay selección
        btnEditarTrabajador.disableProperty().bind(tblTrabajadores.getSelectionModel().selectedItemProperty().isNull());
        btnEliminarTrabajador.disableProperty().bind(tblTrabajadores.getSelectionModel().selectedItemProperty().isNull());

        // Handlers
        btnEditarTrabajador.setOnAction(e -> editarSeleccionado());
        btnEliminarTrabajador.setOnAction(e -> eliminarSeleccionado());
        if (btnNuevoTrabajador != null) {
            btnNuevoTrabajador.setOnAction(e -> crearNuevoTrabajador());
        }
    }

    private void crearNuevoTrabajador() {
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        TrabajadorServiceImpl service = new TrabajadorServiceImpl(new dao.impl.TrabajadorDaoImpl(conexion));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Trabajador");
        dialog.setHeaderText("Completa los datos del nuevo trabajador.");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        TextField txtPrimerNombre = new TextField();
        TextField txtSegundoNombre = new TextField();
        TextField txtPrimerApellido = new TextField();
        TextField txtSegundoApellido = new TextField();
        txtPrimerNombre.setPromptText("Primer Nombre");
        txtSegundoNombre.setPromptText("Segundo Nombre");
        txtPrimerApellido.setPromptText("Primer Apellido");
        txtSegundoApellido.setPromptText("Segundo Apellido");
        ChoiceBox<String> cbTipoDocumento = new ChoiceBox<>();
        cbTipoDocumento.getItems().addAll("CC", "TI", "CE", "PP", "NIT", "DNI");
        cbTipoDocumento.setValue("CC");
        TextField txtDocumento = new TextField();
        TextField txtTelefono = new TextField();
        TextField txtEmail = new TextField();
        txtDocumento.setPromptText("Número Documento");
        txtTelefono.setPromptText("Teléfono");
        txtEmail.setPromptText("Email");
        ChoiceBox<String> cbEstado = new ChoiceBox<>();
        cbEstado.getItems().addAll("ACTIVO", "INACTIVO", "SUSPENDIDO");
        cbEstado.setValue("ACTIVO");

        // Etiquetas de error
        Label lblTelError = new Label("El teléfono debe iniciar con 3 y tener 10 dígitos");
        lblTelError.setStyle("-fx-text-fill: red;");
        lblTelError.setVisible(false);
        Label lblEmailError = new Label("Email debe terminar en gmail.com u hotmail.com");
        lblEmailError.setStyle("-fx-text-fill: red;");
        lblEmailError.setVisible(false);
        Label lblDocError = new Label("Debes poner un documento válido según el tipo");
        lblDocError.setStyle("-fx-text-fill: red;");
        lblDocError.setVisible(false);

        // Filtro Teléfono
        java.util.function.UnaryOperator<javafx.scene.control.TextFormatter.Change> phoneFilter = change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("\\d*")) return null;
            if (newText.length() > 10) return null;
            if (!newText.isEmpty() && newText.charAt(0) != '3') return null;
            return change;
        };
        txtTelefono.setTextFormatter(new TextFormatter<>(phoneFilter));
        txtTelefono.textProperty().addListener((o, ov, nv) -> lblTelError.setVisible(nv.length() != 10));

        // Filtro Email
        java.util.function.UnaryOperator<javafx.scene.control.TextFormatter.Change> emailFilter = change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("[A-Za-z0-9._%+\\-@]*")) return null;
            int atIndex = newText.indexOf('@');
            if (atIndex >= 0) {
                String dom = newText.substring(atIndex + 1).toLowerCase();
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

        // Filtro Documento y validación
        java.util.function.UnaryOperator<javafx.scene.control.TextFormatter.Change> docFilter = change -> {
            String newText = change.getControlNewText();
            String tipo = cbTipoDocumento.getValue();
            int max = 10;
            String pattern = "[A-Za-z0-9]*";
            if ("CC".equals(tipo) || "TI".equals(tipo) || "DNI".equals(tipo)) {
                pattern = "\\d*"; max = 10;
            } else if ("NIT".equals(tipo)) {
                pattern = "[\\d-]*"; max = 12;
            } else if ("CE".equals(tipo) || "PP".equals(tipo)) {
                if (change.getText() != null) { change.setText(change.getText().toUpperCase()); }
                pattern = "[A-Z0-9]*"; max = 12;
            }
            if (newText.length() > max) return null;
            if (!newText.matches(pattern)) return null;
            return change;
        };
        txtDocumento.setTextFormatter(new TextFormatter<>(docFilter));
        final Runnable docValidate = () -> {
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

        ComboBox<Rol> cbRol = new ComboBox<>();
        cbRol.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreRol());
            }
        });
        cbRol.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreRol());
            }
        });
        try {
            List<Rol> roles = new RolDaoImpl(conexion).findAll();
            cbRol.getItems().setAll(roles);
            // Seleccionar Recepcionista si id 2 según tu ejemplo
            for (Rol r : roles) { if (r.getIdRol() != null && r.getIdRol() == 2L) { cbRol.setValue(r); break; } }
        } catch (Exception ex) {
            mostrarError("Error al cargar roles: " + ex.getMessage());
            return;
        }

        TextField txtUsuario = new TextField();
        PasswordField txtContrasena = new PasswordField();
        TextField txtSueldo = new TextField();
        txtUsuario.setPromptText("Usuario");
        txtContrasena.setPromptText("Contraseña");
        txtSueldo.setPromptText("Sueldo");

        // Bloquear usuario/contraseña si el rol NO es administrativo
        java.util.function.Consumer<Rol> applyRoleFields = r -> {
            String nombreRol = r != null && r.getNombreRol() != null ? r.getNombreRol().trim().toUpperCase() : "";
            boolean admin = nombreRol.equals("ADMINISTRADOR") || nombreRol.equals("RECEPCIONISTA");
            txtUsuario.setDisable(!admin);
            txtContrasena.setDisable(!admin);
            if (!admin) {
                txtUsuario.clear();
                txtContrasena.clear();
            }
        };
        cbRol.valueProperty().addListener((o, ov, nv) -> applyRoleFields.accept(nv));

        grid.addRow(0, new Label("Primer Nombre *"), txtPrimerNombre);
        grid.addRow(1, new Label("Segundo Nombre"), txtSegundoNombre);
        grid.addRow(2, new Label("Primer Apellido *"), txtPrimerApellido);
        grid.addRow(3, new Label("Segundo Apellido"), txtSegundoApellido);
        grid.addRow(4, new Label("Tipo Documento *"), cbTipoDocumento);
        grid.addRow(5, new Label("Número Documento *"), txtDocumento);
        grid.add(lblDocError, 1, 6);
        grid.addRow(7, new Label("Teléfono *"), txtTelefono);
        grid.add(lblTelError, 1, 8);
        grid.addRow(9, new Label("Email *"), txtEmail);
        grid.add(lblEmailError, 1, 10);
        grid.addRow(11, new Label("Estado"), cbEstado);
        grid.addRow(12, new Label("Rol *"), cbRol);
        grid.addRow(13, new Label("Usuario *"), txtUsuario);
        Label lblUserError = new Label("Para ADMINISTRADOR/RECEPCIONISTA el usuario es obligatorio (≥5)");
        lblUserError.setStyle("-fx-text-fill: red;");
        lblUserError.setVisible(false);
        grid.add(lblUserError, 1, 14);
        grid.addRow(15, new Label("Contraseña *"), txtContrasena);
        Label lblPwdError = new Label("Para ADMINISTRADOR/RECEPCIONISTA la contraseña es obligatoria");
        lblPwdError.setStyle("-fx-text-fill: red;");
        lblPwdError.setVisible(false);
        grid.add(lblPwdError, 1, 16);
        grid.addRow(17, new Label("Sueldo *"), txtSueldo);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Evitar cierre si teléfono/email/documento no cumplen
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String tel = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            docValidate.run();
            if (lblDocError.isVisible()) { event.consume(); return; }
            if (tel.length() != 10) { lblTelError.setVisible(true); event.consume(); return; }
            if (!email.matches("^[\\w.%+\\-]+@(gmail|hotmail)\\.com$")) { lblEmailError.setVisible(true); event.consume(); return; }

            // Validación: si el rol es administrativo, exigir contraseña, y no cerrar el diálogo
            Rol rolSel = cbRol.getValue();
            String nombreRol = rolSel != null && rolSel.getNombreRol() != null ? rolSel.getNombreRol().trim().toUpperCase() : "";
            boolean rolAdmin = nombreRol.equals("ADMINISTRADOR") || nombreRol.equals("RECEPCIONISTA");
            if (rolAdmin) {
                String usr = txtUsuario.getText();
                if (usr == null || usr.trim().isEmpty() || usr.trim().length() < 5) {
                    lblUserError.setVisible(true);
                    event.consume();
                    return;
                } else {
                    lblUserError.setVisible(false);
                }
                String pwd = txtContrasena.getText();
                if (pwd == null || pwd.trim().isEmpty()) {
                    lblPwdError.setVisible(true);
                    event.consume();
                    return;
                } else {
                    lblPwdError.setVisible(false);
                }
            } else {
                lblUserError.setVisible(false);
                lblPwdError.setVisible(false);
            }
        });

        // Estado inicial de bloqueo según rol seleccionado al abrir el diálogo
        applyRoleFields.accept(cbRol.getValue());

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    String pNom = txtPrimerNombre.getText().trim();
                    String pApe = txtPrimerApellido.getText().trim();
                    String tipoDoc = cbTipoDocumento.getValue();
                    String doc = txtDocumento.getText().trim();
                    String email = txtEmail.getText().trim();
                    String telS = txtTelefono.getText().trim();
                    String estado = cbEstado.getValue();
                    Rol rolSel = cbRol.getValue();
                    String usuario = txtUsuario.getText().trim();
                    String contrasena = txtContrasena.getText();
                    String sueldoS = txtSueldo.getText().trim();

                    if (pNom.isEmpty() || pApe.isEmpty() || tipoDoc == null || tipoDoc.isEmpty() || doc.isEmpty() ||
                        email.isEmpty() || telS.isEmpty() || sueldoS.isEmpty() || rolSel == null) {
                        Alert warn = new Alert(Alert.AlertType.WARNING);
                        warn.setTitle("Validación");
                        warn.setHeaderText(null);
                        warn.setContentText("Completa todos los campos obligatorios (usuario/contraseña según rol).");
                        warn.showAndWait();
                        return;
                    }

                    long tel = Long.parseLong(telS);
                    double sueldo = Double.parseDouble(sueldoS);

                    Trabajador t = new Trabajador();
                    t.setPrimerNombre(pNom);
                    t.setSegundoNombre(txtSegundoNombre.getText().trim());
                    t.setPrimerApellido(pApe);
                    t.setSegundoApellido(txtSegundoApellido.getText().trim());
                    t.setTipoDocumento(tipoDoc);
                    t.setDocumento(doc);
                    t.setTelefono(tel);
                    t.setEmail(email);
                    t.setEstado(estado);
                    t.setRol(rolSel);
                    t.setUsuario(usuario);
                    t.setContrasena(contrasena);
                    t.setSueldo(sueldo);

                    service.crearConProcedimiento(t);

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Nuevo Trabajador");
                    ok.setHeaderText(null);
                    ok.setContentText("Trabajador creado. ID: " + t.getIdTrabajador());
                    ok.showAndWait();
                    cargarTrabajadores();
                } catch (NumberFormatException nfe) {
                    mostrarError("Teléfono o sueldo no válidos.");
                } catch (SQLException ex) {
                    mostrarError("No se pudo crear el trabajador: " + ex.getMessage());
                } catch (IllegalArgumentException iae) {
                    mostrarError(iae.getMessage());
                }
            }
        });
    }

    private void configurarTabla() {
        tblTrabajadores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdTrabajador())));
        colPrimerNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrimerNombre()));
        colSegundoNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSegundoNombre()));
        colPrimerApellido.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrimerApellido()));
        colSegundoApellido.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSegundoApellido()));
        colUsuario.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        colRol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRol() != null ? d.getValue().getRol().getNombreRol() : "-"));
        colSueldo.setCellValueFactory(d -> new SimpleStringProperty(String.format(Locale.US, "%.2f", d.getValue().getSueldo())));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colEstado.setCellFactory(column -> new TableCell<Trabajador, String>() {
            @Override
            protected void updateItem(String ignored, boolean empty) {
                super.updateItem(ignored, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Trabajador t = getTableRow().getItem();
                    String estado = t.getEstado() != null ? t.getEstado().toUpperCase(Locale.ROOT) : "-";
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

        // Badge para el Rol
        colRol.setCellFactory(column -> new TableCell<Trabajador, String>() {
            @Override
            protected void updateItem(String ignored, boolean empty) {
                super.updateItem(ignored, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Trabajador t = getTableRow().getItem();
                    String rol = (t.getRol() != null && t.getRol().getNombreRol() != null) ? t.getRol().getNombreRol() : "-";
                    Label badge = new Label(rol.toUpperCase(Locale.ROOT));
                    String style = "-fx-background-color: #1e293b; -fx-text-fill: white;";
                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

    }

    private void cargarTrabajadores() {
        try {
            data.setAll(trabajadorDao.findAll());
            tblTrabajadores.setItems(data);
        } catch (Exception e) {
            data.clear();
            tblTrabajadores.setItems(data);
        }
    }

    private void configurarBusqueda() {
        FilteredList<Trabajador> filtered = new FilteredList<>(data, t -> true);
        txtBuscar.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.toLowerCase(Locale.ROOT).trim();
            filtered.setPredicate(t -> {
                if (q.isEmpty()) return true;
                String nombre = t.getNombreCompleto() != null ? t.getNombreCompleto().toLowerCase(Locale.ROOT) : "";
                String usuario = t.getUsuario() != null ? t.getUsuario().toLowerCase(Locale.ROOT) : "";
                String rol = (t.getRol() != null && t.getRol().getNombreRol() != null) ? t.getRol().getNombreRol().toLowerCase(Locale.ROOT) : "";
                String estado = t.getEstado() != null ? t.getEstado().toLowerCase(Locale.ROOT) : "";
                return nombre.contains(q) || usuario.contains(q) || rol.contains(q) || estado.contains(q);
            });
        });
        SortedList<Trabajador> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblTrabajadores.comparatorProperty());
        tblTrabajadores.setItems(sorted);
    }

    private void editarSeleccionado() {
        Trabajador seleccionado = tblTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        TrabajadorServiceImpl service = new TrabajadorServiceImpl(new TrabajadorDaoImpl(conexion));

        Trabajador t;
        try {
            t = service.buscarPorId(seleccionado.getIdTrabajador());
            if (t == null) {
                mostrarError("No se encontró el trabajador seleccionado.");
                return;
            }
        } catch (SQLException ex) {
            mostrarError("Error al cargar el trabajador: " + ex.getMessage());
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Trabajador");
        dialog.setHeaderText("Modifica nombres, estado, sueldo y rol");

        // Campos permitidos: Nombres y apellidos, Estado, Sueldo, Rol
        TextField txtPrimerNombre = new TextField(t.getPrimerNombre());
        TextField txtSegundoNombre = new TextField(t.getSegundoNombre());
        TextField txtPrimerApellido = new TextField(t.getPrimerApellido());
        TextField txtSegundoApellido = new TextField(t.getSegundoApellido());
        TextField txtSueldo = new TextField(String.format(Locale.US, "%.2f", t.getSueldo()));
        ChoiceBox<String> cbEstado = new ChoiceBox<>();
        cbEstado.getItems().addAll("ACTIVO", "INACTIVO", "SUSPENDIDO");
        cbEstado.setValue(t.getEstado() != null ? t.getEstado().toUpperCase(Locale.ROOT) : "ACTIVO");

        ComboBox<Rol> cbRol = new ComboBox<>();
        cbRol.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreRol());
            }
        });
        cbRol.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Rol item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNombreRol());
            }
        });

        try {
            List<Rol> roles = new RolDaoImpl(conexion).findAll();
            cbRol.getItems().setAll(roles);
            if (t.getRol() != null) {
                for (Rol r : roles) {
                    if (r.getIdRol() != null && r.getIdRol().equals(t.getRol().getIdRol())) {
                        cbRol.setValue(r);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            mostrarError("Error al cargar roles: " + ex.getMessage());
            return;
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Primer nombre"), 0, 0);
        grid.add(txtPrimerNombre, 1, 0);
        grid.add(new Label("Segundo nombre"), 0, 1);
        grid.add(txtSegundoNombre, 1, 1);
        grid.add(new Label("Primer apellido"), 0, 2);
        grid.add(txtPrimerApellido, 1, 2);
        grid.add(new Label("Segundo apellido"), 0, 3);
        grid.add(txtSegundoApellido, 1, 3);
        grid.add(new Label("Estado"), 0, 4);
        grid.add(cbEstado, 1, 4);
        grid.add(new Label("Sueldo"), 0, 5);
        grid.add(txtSueldo, 1, 5);
        grid.add(new Label("Rol"), 0, 6);
        grid.add(cbRol, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> bt);
        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) return;

        try {
            // Validaciones simples antes del servicio
            String pNom = txtPrimerNombre.getText() == null ? "" : txtPrimerNombre.getText().trim();
            String sNom = txtSegundoNombre.getText() == null ? "" : txtSegundoNombre.getText().trim();
            String pApe = txtPrimerApellido.getText() == null ? "" : txtPrimerApellido.getText().trim();
            String sApe = txtSegundoApellido.getText() == null ? "" : txtSegundoApellido.getText().trim();

            if (pNom.isEmpty()) { mostrarError("El primer nombre es obligatorio."); return; }
            if (pApe.isEmpty()) { mostrarError("El primer apellido es obligatorio."); return; }
            if (sApe.isEmpty()) { mostrarError("El segundo apellido es obligatorio."); return; }

            double sueldo = Double.parseDouble(txtSueldo.getText().trim());
            String estado = cbEstado.getValue();
            Rol rolSel = cbRol.getValue();

            if (rolSel == null) {
                mostrarError("Debe seleccionar un rol válido.");
                return;
            }

            // Asignar cambios
            t.setPrimerNombre(pNom);
            t.setSegundoNombre(sNom);
            t.setPrimerApellido(pApe);
            t.setSegundoApellido(sApe);
            t.setSueldo(sueldo);
            t.setEstado(estado);
            t.setRol(rolSel);

            service.actualizar(t);
            cargarTrabajadores();
        } catch (NumberFormatException nfe) {
            mostrarError("El sueldo debe ser numérico.");
        } catch (Exception ex) {
            mostrarError("No se pudo actualizar: " + ex.getMessage());
        }
    }

    private void eliminarSeleccionado() {
        Trabajador seleccionado = tblTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        TrabajadorServiceImpl service = new TrabajadorServiceImpl(new TrabajadorDaoImpl(conexion));
        try {
            Trabajador t = service.buscarPorId(seleccionado.getIdTrabajador());
            if (t == null) { mostrarError("No se encontró el trabajador."); return; }
            boolean inactivo = "INACTIVO".equalsIgnoreCase(t.getEstado());

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(inactivo ? "Activar Trabajador" : "Desactivar Trabajador");
            confirm.setHeaderText(null);
            confirm.setContentText(inactivo ?
                    "Esto cambiará el estado a ACTIVO. ¿Continuar?" :
                    "Esto cambiará el estado a INACTIVO. ¿Continuar?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) { return; }

            if (inactivo) {
                service.activar(seleccionado.getIdTrabajador());
            } else {
                service.eliminar(seleccionado.getIdTrabajador());
            }
            cargarTrabajadores();
        } catch (Exception ex) {
            mostrarError("No se pudo desactivar: " + ex.getMessage());
        }
    }

    /**
     * Activa explícitamente al trabajador seleccionado (PERSONA.estado = 'ACTIVO').
     */
    public void activarSeleccionado() {
        Trabajador seleccionado = tblTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        TrabajadorServiceImpl service = new TrabajadorServiceImpl(new TrabajadorDaoImpl(conexion));
        try {
            Trabajador t = service.buscarPorId(seleccionado.getIdTrabajador());
            if (t == null) { mostrarError("No se encontró el trabajador."); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Activar Trabajador");
            confirm.setHeaderText(null);
            confirm.setContentText("Esto cambiará el estado a ACTIVO. ¿Continuar?");
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) { return; }

            service.activar(seleccionado.getIdTrabajador());
            cargarTrabajadores();
        } catch (Exception ex) {
            mostrarError("No se pudo activar: " + ex.getMessage());
        }
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(msg);
    a.showAndWait();
    }

}