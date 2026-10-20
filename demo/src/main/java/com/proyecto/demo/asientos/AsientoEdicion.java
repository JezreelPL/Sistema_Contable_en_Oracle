package com.proyecto.demo.asientos;

import java.time.LocalDate;

public record AsientoEdicion(long id, LocalDate fecha, String descripcion, Long idVenta) {
}