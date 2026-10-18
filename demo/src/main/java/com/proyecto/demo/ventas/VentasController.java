package com.proyecto.demo.ventas;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VentasController {
    @GetMapping("/ventas")
    public String lista() {
        return "ventas/lista";
    }
}
