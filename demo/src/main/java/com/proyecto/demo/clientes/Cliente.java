package com.proyecto.demo.clientes;

import java.time.LocalDate;

public record Cliente(
        Long idCliente,
        String nombre,
        String direccion,
        String nit,
        String correo,
        String telefono,
        LocalDate fechaRegistro
) {
}
