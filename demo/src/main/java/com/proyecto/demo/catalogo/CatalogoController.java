package com.proyecto.demo.catalogo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CatalogoController {
    @GetMapping("/catalogo")
    public String lista() {
        return "catalogo/lista";
    }
}
