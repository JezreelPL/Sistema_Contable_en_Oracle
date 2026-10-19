package com.proyecto.demo.asientos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Asiento(long id, LocalDate fecha, String descripcion, String numeroFactura, BigDecimal total) {
}