package com.proyecto.demo.catalogo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CatalogoController {

    private final CatalogoService servicio;

    public CatalogoController(CatalogoService servicio) {
        this.servicio = servicio;
    }

    // Listado con buscador
    @GetMapping("/catalogo")
    public String lista(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("cuentas", servicio.listar(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return "catalogo/lista";
    }

    // Formulario para crear
    @GetMapping("/catalogo/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cuenta", new Cuenta(0, "", "", ""));
        model.addAttribute("tipos", CatalogoService.TIPOS);
        return "catalogo/formulario";
    }

    // Formulario para editar, con los datos cargados
    @GetMapping("/catalogo/{id}/editar")
    public String editar(@PathVariable long id, Model model, RedirectAttributes redirect) {
        Cuenta cuenta = servicio.obtener(id);
        if (cuenta == null) {
            redirect.addFlashAttribute("error", "La cuenta ya no existe.");
            return "redirect:/catalogo";
        }
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("tipos", CatalogoService.TIPOS);
        return "catalogo/formulario";
    }

    // Crear (sin id) o actualizar (con id)
    @PostMapping("/catalogo/guardar")
    public String guardar(@RequestParam(required = false) Long id,
            @RequestParam(defaultValue = "") String codigo,
            @RequestParam(defaultValue = "") String nombre,
            @RequestParam(defaultValue = "") String tipo,
            Model model, RedirectAttributes redirect) {
        try {
            if (id == null) {
                servicio.crear(codigo, nombre, tipo);
                redirect.addFlashAttribute("mensaje", "Cuenta creada correctamente.");
            } else {
                servicio.actualizar(id, codigo, nombre, tipo);
                redirect.addFlashAttribute("mensaje", "Cuenta actualizada correctamente.");
            }
            return "redirect:/catalogo";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("cuenta", new Cuenta(id == null ? 0 : id, codigo, nombre, tipo));
        model.addAttribute("tipos", CatalogoService.TIPOS);
        return "catalogo/formulario";
    }

    // Pantalla de confirmación de borrado
    @GetMapping("/catalogo/{id}/eliminar")
    public String confirmarBorrado(@PathVariable long id, Model model, RedirectAttributes redirect) {
        Cuenta cuenta = servicio.obtener(id);
        if (cuenta == null) {
            redirect.addFlashAttribute("error", "La cuenta ya no existe.");
            return "redirect:/catalogo";
        }
        model.addAttribute("cuenta", cuenta);
        return "catalogo/eliminar";
    }

    // Borrar
    @PostMapping("/catalogo/{id}/borrar")
    public String borrar(@PathVariable long id, Model model, RedirectAttributes redirect) {
        try {
            servicio.borrar(id);
            redirect.addFlashAttribute("mensaje", "Cuenta eliminada correctamente.");
            return "redirect:/catalogo";
        } catch (IllegalArgumentException e) {
            Cuenta cuenta = servicio.obtener(id);
            if (cuenta == null) {
                redirect.addFlashAttribute("error", e.getMessage());
                return "redirect:/catalogo";
            }
            model.addAttribute("cuenta", cuenta);
            model.addAttribute("error", e.getMessage());
            return "catalogo/eliminar";
        }
    }
}
