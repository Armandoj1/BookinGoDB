package controller;

import ConexionBase.Conexion;
import dao.impl.ClienteDaoImpl;
import dao.impl.HabitacionDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Cliente;
import model.Habitacion;
import model.Reserva;
import model.Trabajador;
import service.DatabaseConfig;
import service.SessionManager;
import service.impl.ReservaServiceImpl;
import service.impl.TrabajadorServiceImpl;
import dao.impl.TrabajadorDaoImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class NuevaReservaController {

    @FXML
    private TextField txtDocumento;
    @FXML
    private ComboBox<Habitacion> cbHabitacion;
    @FXML
    private DatePicker dpEntrada;
    @FXML
    private DatePicker dpSalida;
    @FXML
    private ComboBox<String> cbTipoReserva;
    @FXML
    private TextArea txtObservacion;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblTitulo;
    @FXML
    private Button btnGuardar;

    private ReservaDaoImpl reservaDao;
    private HabitacionDaoImpl habitacionDao;
    private ClienteDaoImpl clienteDao;
    private Runnable onReservaCreada;
    private Runnable onReservaGuardada;
    private Reserva reservaEditando;

    public void initDependencies(ReservaDaoImpl reservaDao) {
        this.reservaDao = reservaDao;
        // Inicializamos otros DAOs con la configuración centralizada
        Conexion conexion = new Conexion(DatabaseConfig.getDatabase());
        this.habitacionDao = new HabitacionDaoImpl(conexion);
        this.clienteDao = new ClienteDaoImpl(conexion);
        initForm();
    }

    public void setOnReservaCreada(Runnable callback) {
        this.onReservaCreada = callback;
    }

    public void setOnReservaGuardada(Runnable callback) {
        this.onReservaGuardada = callback;
    }

    @FXML
    private void initialize() {
        // En caso de que initDependencies no haya sido llamado aún, la UI se prepara aquí
        cbTipoReserva.setItems(FXCollections.observableArrayList("ONLINE", "PRESENCIAL", "TELEFONICA", "AGENCIA", "CORPORATIVA"));
        cbTipoReserva.getSelectionModel().select("PRESENCIAL");

        dpEntrada.setValue(LocalDate.now());
        dpSalida.setValue(LocalDate.now().plusDays(1));

        // Listeners para recalcular total
        cbHabitacion.valueProperty().addListener((obs, oldV, newV) -> actualizarTotal());
        dpEntrada.valueProperty().addListener((obs, o, n) -> actualizarTotal());
        dpSalida.valueProperty().addListener((obs, o, n) -> actualizarTotal());
    }

    private void initForm() {
        try {
            // Solo listar habitaciones disponibles para evitar reservas en estados inválidos
            List<Habitacion> habitaciones = habitacionDao.findByEstado("DISPONIBLE");
            cbHabitacion.setItems(FXCollections.observableArrayList(habitaciones));
            cbHabitacion.setConverter(new StringConverter<>() {
                @Override
                public String toString(Habitacion h) {
                    if (h == null) return "Seleccione una habitación";
                    return "Hab. " + h.getNumero() + " - " + h.getTipoHabitacion() + " - COP $ " + String.format("%,.2f", h.getPrecio()).replace(',', 'X').replace('.', ',').replace('X', '.');
                }

                @Override
                public Habitacion fromString(String s) { return null; }
            });

            cbHabitacion.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Habitacion h, boolean empty) {
                    super.updateItem(h, empty);
                    setText(empty || h == null ? "Seleccione una habitación" :
                            "Hab. " + h.getNumero() + " - " + h.getTipoHabitacion() + " - COP $ " + String.format("%,.2f", h.getPrecio()).replace(',', 'X').replace('.', ',').replace('X', '.'));
                }
            });
            cbHabitacion.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(Habitacion h, boolean empty) {
                    super.updateItem(h, empty);
                    setText(empty || h == null ? null :
                            "Hab. " + h.getNumero() + " - " + h.getTipoHabitacion() + " - COP $ " + String.format("%,.2f", h.getPrecio()).replace(',', 'X').replace('.', ',').replace('X', '.'));
                }
            });

            // Tipos de reserva permitidos
            cbTipoReserva.setItems(FXCollections.observableArrayList("ONLINE", "PRESENCIAL", "TELEFONICA", "AGENCIA", "CORPORATIVA"));
            cbTipoReserva.getSelectionModel().select("PRESENCIAL");

        } catch (SQLException e) {
            cbHabitacion.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Configura el diálogo en modo edición precargando los datos de la reserva.
     */
    public void setReservaParaEditar(Reserva r) {
        this.reservaEditando = r;
        try {
            // Documento del cliente
            if (r.getCliente() != null && r.getCliente().getDocumento() != null) {
                txtDocumento.setText(r.getCliente().getDocumento());
            }

            // Habitaciones ya inicializadas en initForm(); seleccionar la actual
            if (r.getHabitacion() != null) {
                // Asegurar que la lista esté cargada
                if (cbHabitacion.getItems() == null || cbHabitacion.getItems().isEmpty()) {
                    List<Habitacion> habitaciones = habitacionDao.findAll();
                    cbHabitacion.setItems(FXCollections.observableArrayList(habitaciones));
                }
                // Buscar por id
                Habitacion match = cbHabitacion.getItems().stream()
                        .filter(h -> h.getIdHabitacion() != null && r.getHabitacion().getIdHabitacion() != null
                                && h.getIdHabitacion().equals(r.getHabitacion().getIdHabitacion()))
                        .findFirst().orElse(null);
                cbHabitacion.getSelectionModel().select(match);
            }

            // Fechas
            dpEntrada.setValue(r.getFechaEntrada());
            dpSalida.setValue(r.getFechaSalida());

            // Tipo de reserva
            if (r.getTipoReserva() != null) {
                cbTipoReserva.getSelectionModel().select(r.getTipoReserva());
            }

            // Observación
            txtObservacion.setText(r.getObservacion());

            // Recalcular total y ajustar títulos
            actualizarTotal();
            if (lblTitulo != null) lblTitulo.setText("Editar Reserva");
            if (btnGuardar != null) btnGuardar.setText("Guardar Cambios");
        } catch (Exception ex) {
            // Si algo falla, dejamos al usuario corregir manualmente
        }
    }

    private void actualizarTotal() {
        Habitacion h = cbHabitacion.getValue();
        LocalDate entrada = dpEntrada.getValue();
        LocalDate salida = dpSalida.getValue();
        if (h == null || entrada == null || salida == null || !salida.isAfter(entrada)) {
            lblTotal.setText("Total: COP $ 0,00");
            return;
        }
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        double total = noches * h.getPrecio();
        lblTotal.setText("Total: COP $ " + String.format("%,.2f", total).replace(',', 'X').replace('.', ',').replace('X', '.'));
    }

    @FXML
    private void handleCancelar() {
        // Cerrar la ventana
        lblTotal.getScene().getWindow().hide();
    }

    @FXML
    private void handleCrear() {
        try {
            String documento = txtDocumento.getText();
            if (documento == null || documento.isBlank()) {
                showAlert("Documento requerido", "Ingrese el número de documento del cliente.");
                return;
            }

            Cliente cliente = clienteDao.findByDocumento(documento);
            if (cliente == null) {
                showAlert("Cliente no encontrado", "No existe un cliente con el documento ingresado.");
                return;
            }

            Habitacion habitacion = cbHabitacion.getValue();
            if (habitacion == null) {
                showAlert("Habitación requerida", "Seleccione una habitación.");
                return;
            }
            if (habitacion.getEstado() == null || !"DISPONIBLE".equalsIgnoreCase(habitacion.getEstado())) {
                showAlert("Habitación no disponible", "No se puede crear una reserva para una habitación cuyo estado no sea DISPONIBLE.");
                return;
            }

            LocalDate fechaEntrada = dpEntrada.getValue();
            LocalDate fechaSalida = dpSalida.getValue();
            if (fechaEntrada == null || fechaSalida == null || !fechaSalida.isAfter(fechaEntrada)) {
                showAlert("Fechas inválidas", "La fecha de salida debe ser posterior a la entrada.");
                return;
            }

            Trabajador trabajadorActual = SessionManager.getInstance().getUsuarioActual();
            if (trabajadorActual == null || trabajadorActual.getIdTrabajador() == null) {
                showAlert("Sesión requerida", "No hay un trabajador con sesión activa.");
                return;
            }

            // Validar que el trabajador de sesión exista en BD para evitar choques con FK
            try {
                TrabajadorServiceImpl tService = new TrabajadorServiceImpl(new TrabajadorDaoImpl(new Conexion(DatabaseConfig.getDatabase())));
                Trabajador tDb = tService.buscarPorId(trabajadorActual.getIdTrabajador());
                if (tDb == null) {
                    // Fallback: intentar localizarlo por usuario de sesión
                    Trabajador tByUser = null;
                    try { tByUser = tService.buscarPorUsuario(trabajadorActual.getUsuario()); } catch (Exception ignored) {}
                    if (tByUser != null) {
                        trabajadorActual = tByUser; // refrescar datos desde BD
                    } else {
                        showAlert("Trabajador inválido", "El trabajador en sesión no existe en el sistema. Cierre sesión e inicie nuevamente.");
                        return;
                    }
                } else {
                    trabajadorActual = tDb; // usar la versión fresca desde BD
                }
            } catch (SQLException e) {
                showAlert("Error de validación", "No se pudo validar el trabajador en sesión: " + e.getMessage());
                return;
            } catch (IllegalArgumentException iae) {
                showAlert("Datos inválidos", iae.getMessage());
                return;
            }

            long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
            double total = noches * habitacion.getPrecio();

            ReservaServiceImpl service = new ReservaServiceImpl(reservaDao);

            if (reservaEditando != null && reservaEditando.getIdReserva() != null) {
                // Modo edición: actualizar la reserva existente
                reservaEditando.setCliente(cliente);
                reservaEditando.setTrabajador(trabajadorActual);
                reservaEditando.setHabitacion(habitacion);
                // Mantener la fecha de reserva original si existe
                if (reservaEditando.getFechaReserva() == null) {
                    reservaEditando.setFechaReserva(LocalDate.now());
                }
                reservaEditando.setFechaEntrada(fechaEntrada);
                reservaEditando.setFechaSalida(fechaSalida);
                reservaEditando.setTipoReserva(cbTipoReserva.getValue());
                reservaEditando.setObservacion(txtObservacion.getText());
                // Ajustar estado a un valor permitido si es inválido o falta
                String estadoActual = reservaEditando.getEstado();
                if (!esEstadoPermitido(estadoActual)) {
                    reservaEditando.setEstado("ACTIVA");
                }
                reservaEditando.setTotal(total);

                service.actualizar(reservaEditando);
                if (onReservaGuardada != null) onReservaGuardada.run();
            } else {
                // Modo creación
                Reserva reserva = new Reserva();
                reserva.setCliente(cliente);
                reserva.setTrabajador(trabajadorActual);
                reserva.setHabitacion(habitacion);
                reserva.setFechaReserva(LocalDate.now());
                reserva.setFechaEntrada(fechaEntrada);
                reserva.setFechaSalida(fechaSalida);
                reserva.setTipoReserva(cbTipoReserva.getValue());
                reserva.setObservacion(txtObservacion.getText());
                // Estado inicial válido según las opciones indicadas
                reserva.setEstado("ACTIVA");
                reserva.setTotal(total);

                service.crear(reserva);
                if (onReservaCreada != null) onReservaCreada.run();
            }
            lblTotal.getScene().getWindow().hide();
        } catch (SQLException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            if (msg.toUpperCase().contains("FOREIGN KEY") && msg.toLowerCase().contains("id_trabajador")) {
                showAlert("Trabajador inválido", "El id del trabajador en sesión no existe en TRABAJADOR. Cierre sesión e intente nuevamente.");
            } else {
                showAlert("Error al crear", "Ocurrió un error al guardar la reserva: " + msg);
            }
        } catch (IllegalArgumentException iae) {
            showAlert("Datos inválidos", iae.getMessage());
        }
    }

    private boolean esEstadoPermitido(String estado) {
        if (estado == null || estado.isBlank()) return false;
        String e = estado.trim().toUpperCase();
        // Estados válidos de RESERVA: ACTIVA, EN_CURSO, FINALIZADA, CANCELADA
        return e.equals("ACTIVA") || e.equals("EN_CURSO") || e.equals("FINALIZADA") || e.equals("CANCELADA");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}