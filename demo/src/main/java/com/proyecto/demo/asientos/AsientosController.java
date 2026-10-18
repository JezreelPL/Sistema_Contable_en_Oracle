package com.proyecto.demo.asientos;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AsientosController {
    @GetMapping("/asientos")
    public String lista() {
        return "asientos/lista";
    }
}
