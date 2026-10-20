package com.proyecto.demo.ventas;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentasService {

    private final VentasRepository repositorio;

    public VentasService(VentasRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void guardar(Long id, Long idCliente, LocalDate fecha, String factura, String formaPago,
            BigDecimal descuento, BigDecimal total) {
        if (idCliente == null) {
            throw new IllegalArgumentException("Selecciona un cliente.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (factura == null || factura.isBlank()) {
            throw new IllegalArgumentException("El número de factura es obligatorio.");
        }
        if (formaPago == null || formaPago.isBlank()) {
            throw new IllegalArgumentException("Selecciona la forma de pago.");
        }
        if (total == null || total.signum() <= 0) {
            throw new IllegalArgumentException("El total es obligatorio y debe ser mayor que cero.");
        }
        BigDecimal desc = (descuento == null) ? BigDecimal.ZERO : descuento;
        if (desc.signum() < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo.");
        }

        if (id == null) {
            repositorio.crear(idCliente, fecha, factura.trim(), formaPago, desc, total);
        } else {
            repositorio.actualizar(id, idCliente, fecha, factura.trim(), formaPago, desc, total);
        }
    }
}
