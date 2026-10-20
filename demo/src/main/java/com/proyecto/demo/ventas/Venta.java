package com.proyecto.demo.ventas;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Venta(Long id, Long idCliente, String nombreCliente, LocalDate fecha,
        String numeroFactura, String formaPago, BigDecimal descuento, BigDecimal total) {
}
