package com.proyecto.demo.clientes;

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

@Controller
public class ClientesController {

    private final ClientesRepository repositorio;
    private final ClientesService servicio;

    public ClientesController(ClientesRepository repositorio, ClientesService servicio) {
        this.repositorio = repositorio;
        this.servicio = servicio;
    }

    @GetMapping("/clientes")
    public String lista(@RequestParam(required = false) String filtro, Model model) {
        model.addAttribute("filtro", filtro);
        model.addAttribute("clientes", repositorio.listar(filtro));
        return "clientes/lista";
    }

    @GetMapping("/clientes/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente(null, "", "", "", "", "", LocalDate.now()));
        return "clientes/formulario";
    }

    @GetMapping("/clientes/editar/{idCliente}")
    public String editar(@PathVariable Long idCliente, Model model) {
        Cliente cliente = repositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado."));
        model.addAttribute("cliente", cliente);
        return "clientes/formulario";
    }

    @PostMapping("/clientes/guardar")
    public String guardar(
            @RequestParam(required = false) Long idCliente,
            @RequestParam String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String nit,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaRegistro,
            Model model) {
        try {
            Cliente cliente = new Cliente(
                    idCliente,
                    nombre,
                    direccion,
                    nit,
                    correo,
                    telefono,
                    fechaRegistro != null ? fechaRegistro : LocalDate.now());

            if (idCliente == null) {
                servicio.crear(cliente);
            } else {
                servicio.actualizar(cliente);
            }
            return "redirect:/clientes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cliente", new Cliente(idCliente, nombre, direccion, nit, correo, telefono,
                    fechaRegistro != null ? fechaRegistro : LocalDate.now()));
            return "clientes/formulario";
        } catch (DataAccessException e) {
            model.addAttribute("error", "No se pudo guardar el cliente: " + e.getMostSpecificCause().getMessage());
            model.addAttribute("cliente", new Cliente(idCliente, nombre, direccion, nit, correo, telefono,
                    fechaRegistro != null ? fechaRegistro : LocalDate.now()));
            return "clientes/formulario";
        }
    }

    @PostMapping("/clientes/eliminar/{idCliente}")
    public String eliminar(@PathVariable Long idCliente, @RequestParam(required = false) String filtro, Model model) {
        try {
            servicio.eliminar(idCliente);
            return "redirect:/clientes" + (filtro == null || filtro.isBlank() ? "" : "?filtro=" + filtro);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientes", repositorio.listar(filtro));
            return "clientes/lista";
        } catch (DataAccessException e) {
            String mensaje = e.getMostSpecificCause() != null && e.getMostSpecificCause().getMessage() != null
                    ? e.getMostSpecificCause().getMessage()
                    : "No se pudo eliminar el cliente.";
            if (mensaje.toUpperCase().contains("ORA-02292") || mensaje.toUpperCase().contains("INTEGRITY CONSTRAINT")) {
                model.addAttribute("error", "No se puede borrar, tiene ventas");
            } else {
                model.addAttribute("error", "No se pudo eliminar el cliente: " + mensaje);
            }
            model.addAttribute("clientes", repositorio.listar(filtro));
            return "clientes/lista";
        }
    }
}

