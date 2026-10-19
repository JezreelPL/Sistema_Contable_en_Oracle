package com.proyecto.demo.asientos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsientosService {

    private final AsientosRepository repositorio;

    public AsientosService(AsientosRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public long crear(LocalDate fecha, String descripcion, long idVenta, List<LineaAsiento> lineas) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        validarLineas(lineas);

        long idAsiento = repositorio.crear(fecha, descripcion.trim(), idVenta);
        for (LineaAsiento linea : lineas) {
            repositorio.crearLinea(idAsiento, linea);
        }
        return idAsiento;
    }

    private void validarLineas(List<LineaAsiento> lineas) {
        if (lineas.size() < 2) {
            throw new IllegalArgumentException("El asiento necesita al menos dos líneas.");
        }
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        for (LineaAsiento l : lineas) {
            if (l.idCuenta() == null) {
                throw new IllegalArgumentException("Cada línea necesita una cuenta.");
            }
            if (l.debe().signum() < 0 || l.haber().signum() < 0) {
                throw new IllegalArgumentException("Los montos no pueden ser negativos.");
            }
            boolean enDebe = l.debe().signum() > 0;
            boolean enHaber = l.haber().signum() > 0;
            if (enDebe == enHaber) {
                throw new IllegalArgumentException("Cada línea debe tener monto en el debe o en el haber, pero no en ambos.");
            }
            totalDebe = totalDebe.add(l.debe());
            totalHaber = totalHaber.add(l.haber());
        }
        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new IllegalArgumentException("El asiento no cuadra: debe " + totalDebe + " y haber " + totalHaber + ".");
        }
    }
}