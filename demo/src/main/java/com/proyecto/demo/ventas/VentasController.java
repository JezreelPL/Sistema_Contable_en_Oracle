package com.proyecto.demo.ventas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VentasController {

    private static final List<String> FORMAS_PAGO = List.of("Efectivo", "Tarjeta", "Transferencia", "Cheque", "Crédito");

    private final VentasRepository repositorio;
    private final VentasService servicio;

    public VentasController(VentasRepository repositorio, VentasService servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    // LEER: listado + buscador por factura o cliente
    @GetMapping("/ventas")
    public String lista(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("ventas", repositorio.listar(buscar));
        model.addAttribute("buscar", buscar);
        return "ventas/lista";
    }

    // CREAR: formulario vacío
    @GetMapping("/ventas/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("venta", new Venta(null, null, null, LocalDate.now(), "", "", BigDecimal.ZERO, null));
        cargarListas(model);
        return "ventas/formulario";
    }

    // ACTUALIZAR: formulario con los datos cargados
    @GetMapping("/ventas/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes flash) {
        Venta venta = repositorio.buscarPorId(id);
        if (venta == null) {
            flash.addFlashAttribute("error", "La venta no existe.");
            return "redirect:/ventas";
        }
        model.addAttribute("venta", venta);
        cargarListas(model);
        return "ventas/formulario";
    }

    // Guarda: INSERT si no viene id, UPDATE por id_Venta si viene
    @PostMapping("/ventas/guardar")
    public String guardar(@RequestParam(required = false) Long id,
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) String numeroFactura,
            @RequestParam(required = false) String formaPago,
            @RequestParam(required = false) BigDecimal descuento,
            @RequestParam(required = false) BigDecimal total,
            Model model, RedirectAttributes flash) {
        try {
            servicio.guardar(id, idCliente, fecha, numeroFactura, formaPago, descuento, total);
            flash.addFlashAttribute("ok", id == null ? "Venta creada." : "Venta actualizada.");
            return "redirect:/ventas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (DataAccessException e) {
            model.addAttribute("error", traducirError(e));
        }
        model.addAttribute("venta", new Venta(id, idCliente, null, fecha, numeroFactura, formaPago, descuento, total));
        cargarListas(model);
        return "ventas/formulario";
    }

    // BORRAR (la confirmación se hace en la pantalla con JS)
    @PostMapping("/ventas/eliminar")
    public String eliminar(@RequestParam long id, RedirectAttributes flash) {
        try {
            repositorio.eliminar(id);
            flash.addFlashAttribute("ok", "Venta eliminada.");
        } catch (DataAccessException e) {
            flash.addFlashAttribute("error", traducirError(e));
        }
        return "redirect:/ventas";
    }

    private void cargarListas(Model model) {
        model.addAttribute("clientes", repositorio.listarClientes());
        model.addAttribute("formasPago", FORMAS_PAGO);
    }

    // Traduce los errores de Oracle a mensajes entendibles
    private String traducirError(DataAccessException e) {
        String msg = String.valueOf(e.getMostSpecificCause().getMessage());
        if (msg.contains("ORA-00001")) {
            return "Ya existe una venta con ese número de factura.";
        }
        if (msg.contains("ORA-02291")) {
            return "El cliente seleccionado no existe.";
        }
        if (msg.contains("ORA-02292")) {
            return "No se puede eliminar la venta porque ya tiene asientos contables. Elimina primero sus asientos.";
        }
        return "Oracle rechazó la operación: " + msg;
    }
}
