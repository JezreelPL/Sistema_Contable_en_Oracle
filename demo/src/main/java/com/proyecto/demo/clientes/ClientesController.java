package com.proyecto.demo.clientes;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller 
public class ClientesController {
        @GetMapping("/clientes")
    public String lista() {
        return "clientes/lista";
    }
}
