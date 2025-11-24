package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase con datos de ejemplo (mock) para las vistas del sistema de recepción
 */
public class MockData {

    public static class Reserva {
        public int id;
        public String cliente;
        public String habitacion;
        public String entrada;
        public String salida;
        public String total;
        public String estado;

        public Reserva(int id, String cliente, String habitacion, String entrada, String salida, String total,
                String estado) {
            this.id = id;
            this.cliente = cliente;
            this.habitacion = habitacion;
            this.entrada = entrada;
            this.salida = salida;
            this.total = total;
            this.estado = estado;
        }
    }

    public static class Habitacion {
        public String numero;
        public String tipo;
        public int piso;
        public int capacidad;
        public String precio;
        public String estado;
        public String comodidades;

        public Habitacion(String numero, String tipo, int piso, int capacidad, String precio, String estado,
                String comodidades) {
            this.numero = numero;
            this.tipo = tipo;
            this.piso = piso;
            this.capacidad = capacidad;
            this.precio = precio;
            this.estado = estado;
            this.comodidades = comodidades;
        }
    }

    public static class Huesped {
        public int id;
        public String nombreCompleto;
        public String documento;
        public String email;
        public String telefono;
        public String categoria;

        public Huesped(int id, String nombreCompleto, String documento, String email, String telefono,
                String categoria) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.documento = documento;
            this.email = email;
            this.telefono = telefono;
            this.categoria = categoria;
        }
    }

    public static class Pago {
        public int id;
        public String tipo;
        public String fechaEmision;
        public String montoTotal;
        public String metodoPago;
        public String estado;

        public Pago(int id, String tipo, String fechaEmision, String montoTotal, String metodoPago, String estado) {
            this.id = id;
            this.tipo = tipo;
            this.fechaEmision = fechaEmision;
            this.montoTotal = montoTotal;
            this.metodoPago = metodoPago;
            this.estado = estado;
        }
    }

    // Datos de ejemplo para Reservas
    public static List<Reserva> getReservas() {
        List<Reserva> reservas = new ArrayList<>();
        reservas.add(new Reserva(1, "Juan Pérez", "Hab. 102", "20/9/2024", "22/9/2024", "S/ 360.00", "CONFIRMADA"));
        reservas.add(new Reserva(2, "Ana González", "Hab. 202", "20/9/2024", "21/9/2024", "S/ 220.00", "CONFIRMADA"));
        return reservas;
    }

    // Datos de ejemplo para Habitaciones
    public static List<Habitacion> getHabitaciones() {
        List<Habitacion> habitaciones = new ArrayList<>();
        habitaciones.add(new Habitacion("101", "SIMPLE", 1, 1, "COP $ 120.00/noche", "DISPONIBLE",
                "TV, WiFi, Baño privado, Aire acondicionado"));
        habitaciones.add(new Habitacion("102", "DOBLE", 1, 2, "COP $ 180.00/noche", "OCUPADA",
                "TV, WiFi, Baño privado, Aire acondicionado, Minibar"));
        habitaciones.add(new Habitacion("201", "SUITE", 2, 3, "COP $ 350.00/noche", "DISPONIBLE",
                "TV Smart, WiFi, Baño privado, Aire acondicionado, Jacuzzi, Aire, Vista panorámica"));
        habitaciones.add(new Habitacion("202", "MATRIMONIAL", 2, 2, "COP $ 220.00/noche", "OCUPADA",
                "TV, WiFi, Baño privado, Aire acondicionado, Minibar"));
        habitaciones.add(new Habitacion("301", "DOBLE", 3, 2, "COP $ 180.00/noche", "DISPONIBLE",
                "TV, WiFi, Baño privado, Aire acondicionado, Minibar"));
        habitaciones.add(new Habitacion("302", "SIMPLE", 3, 1, "COP $ 120.00/noche", "LIMPIEZA",
                "TV, WiFi, Baño privado, Aire acondicionado"));
        return habitaciones;
    }

    // Datos de ejemplo para Huéspedes
    public static List<Huesped> getHuespedes() {
        List<Huesped> huespedes = new ArrayList<>();
        huespedes.add(new Huesped(1, "Juan Pérez", "DNI 45678912", "juan.perez@email.com", "987654323", "REGULAR"));
        huespedes.add(new Huesped(2, "Ana González", "DNI 78912345", "ana.gonzalez@email.com", "987654324", "VIP"));
        return huespedes;
    }

    // Datos de ejemplo para Pagos
    public static List<Pago> getPagos() {
        List<Pago> pagos = new ArrayList<>();
        pagos.add(new Pago(1, "BOLETA", "20/9/2024", "S/ 360.00", "TARJETA", "PAGADO"));
        pagos.add(new Pago(2, "FACTURA", "20/9/2024", "S/ 220.00", "EFECTIVO", "PENDIENTE"));
        return pagos;
    }

    // Métricas para Dashboard
    public static int getCheckInsHoy() {
        return 0;
    }

    public static int getCheckOutsHoy() {
        return 0;
    }

    public static int getHabitacionesDisponibles() {
        return 3;
    }

    public static int getHabitacionesOcupadas() {
        return 2;
    }

    // Métricas para Reservas
    public static int getTotalReservas() {
        return 2;
    }

    public static int getReservasConfirmadas() {
        return 2;
    }

    public static int getReservasPendientesCheckIn() {
        return 0;
    }

    public static int getReservasActivas() {
        return 0;
    }

    // Métricas para Huéspedes
    public static int getTotalClientes() {
        return 2;
    }

    public static int getClientesVIP() {
        return 1;
    }

    public static int getClientesRegular() {
        return 1;
    }

    public static int getClientesCorporativo() {
        return 0;
    }

    // Obtener fecha y hora actual formateada
    public static String getFechaHoraActual() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy - HH:mm");
        return now.format(formatter);
    }
}
