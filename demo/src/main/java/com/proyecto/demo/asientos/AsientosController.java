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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AsientosController {

    private final AsientosRepository repositorio;
    private final AsientosService servicio;

    public AsientosController(AsientosRepository repositorio, AsientosService servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    @GetMapping("/asientos")
    public String lista(@RequestParam(defaultValue = "") String texto, Model model) {
        model.addAttribute("asientos", repositorio.listar(texto.trim()));
        model.addAttribute("texto", texto);
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
        List<LineaAsiento> lineas = armarLineas(cuentaId, debe, haber);
        try {
            servicio.crear(fecha, descripcion, idVenta, lineas);
            return "redirect:/asientos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            model.addAttribute("error", "Oracle rechazó el asiento: " + e.getMostSpecificCause().getMessage());
        }
        model.addAttribute("asiento", new AsientoEdicion(0, fecha, descripcion, idVenta));
        model.addAttribute("lineas", lineas);
        cargarListas(model);
        return "asientos/formulario";
    }

    @PostMapping("/asientos/{id}/actualizar")
    public String actualizar(@PathVariable long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String descripcion,
            @RequestParam long idVenta,
            @RequestParam(required = false) List<Long> cuentaId,
            @RequestParam(required = false) List<BigDecimal> debe,
            @RequestParam(required = false) List<BigDecimal> haber,
            Model model) {
        List<LineaAsiento> lineas = armarLineas(cuentaId, debe, haber);
        try {
            servicio.actualizar(id, fecha, descripcion, idVenta, lineas);
            return "redirect:/asientos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            model.addAttribute("error", "Oracle rechazó el asiento: " + e.getMostSpecificCause().getMessage());
        }
        model.addAttribute("editando", true);
        model.addAttribute("asiento", new AsientoEdicion(id, fecha, descripcion, idVenta));
        model.addAttribute("lineas", lineas);
        cargarListas(model);
        return "asientos/formulario";
    }

    private void cargarListas(Model model) {
        model.addAttribute("ventas", repositorio.listarVentas());
        model.addAttribute("cuentas", repositorio.listarCuentas());
    }

    private List<LineaAsiento> armarLineas(List<Long> cuentaId, List<BigDecimal> debe, List<BigDecimal> haber) {
        List<LineaAsiento> lineas = new ArrayList<>();
        if (cuentaId != null) {
            for (int i = 0; i < cuentaId.size(); i++) {
                BigDecimal d = (debe != null && debe.get(i) != null) ? debe.get(i) : BigDecimal.ZERO;
                BigDecimal h = (haber != null && haber.get(i) != null) ? haber.get(i) : BigDecimal.ZERO;
                lineas.add(new LineaAsiento(cuentaId.get(i), d, h));
            }
        }
        return lineas;
    }

    @GetMapping("/asientos/{id}/editar")
    public String editar(@PathVariable long id, Model model) {
        try {
            model.addAttribute("asiento", repositorio.buscarPorId(id));
            model.addAttribute("lineas", repositorio.listarLineas(id));
            model.addAttribute("editando", true);
        } catch (DataAccessException e) {
            return "redirect:/asientos";
        }
        cargarListas(model);
        return "asientos/formulario";
    }

    @PostMapping("/asientos/{id}/eliminar")
    public String eliminar(@PathVariable long id, RedirectAttributes redirect) {
        try {
            servicio.eliminar(id);
            redirect.addFlashAttribute("mensaje", "Asiento eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            redirect.addFlashAttribute("error",
                    "Oracle no permitió eliminar el asiento: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/asientos";
    }

}