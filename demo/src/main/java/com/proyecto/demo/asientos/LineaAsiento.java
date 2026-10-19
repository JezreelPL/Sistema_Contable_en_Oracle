package com.proyecto.demo.asientos;

import java.math.BigDecimal;

public record LineaAsiento(Long idCuenta, BigDecimal debe, BigDecimal haber) {
}