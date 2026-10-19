package com.proyecto.demo.asientos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AsientosController {

    private final AsientosRepository repositorio;
    private final AsientosService servicio;

    public AsientosController(AsientosRepository repositorio, AsientosService servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    @GetMapping("/asientos")
    public String lista(Model model) {
        model.addAttribute("asientos", repositorio.listar());
        return "asientos/lista";
    }

    @GetMapping("/asientos/nuevo")
    public String nuevo(Model model) {
        cargarListas(model);
        return "asientos/formulario";
    }

    @PostMapping("/asientos/guardar")
    public String guardar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String descripcion,
            @RequestParam long idVenta,
            @RequestParam(required = false) List<Long> cuentaId,
            @RequestParam(required = false) List<BigDecimal> debe,
            @RequestParam(required = false) List<BigDecimal> haber,
            Model model) {
        try {
            List<LineaAsiento> lineas = new ArrayList<>();
            if (cuentaId != null) {
                for (int i = 0; i < cuentaId.size(); i++) {
                    BigDecimal d = (debe != null && debe.get(i) != null) ? debe.get(i) : BigDecimal.ZERO;
                    BigDecimal h = (haber != null && haber.get(i) != null) ? haber.get(i) : BigDecimal.ZERO;
                    lineas.add(new LineaAsiento(cuentaId.get(i), d, h));
                }
            }
            servicio.crear(fecha, descripcion, idVenta, lineas);
            return "redirect:/asientos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            model.addAttribute("error", "Oracle rechazó el asiento: " + e.getMostSpecificCause().getMessage());
        }
        cargarListas(model);
        return "asientos/formulario";
    }

    private void cargarListas(Model model) {
        model.addAttribute("ventas", repositorio.listarVentas());
        model.addAttribute("cuentas", repositorio.listarCuentas());
    }

}