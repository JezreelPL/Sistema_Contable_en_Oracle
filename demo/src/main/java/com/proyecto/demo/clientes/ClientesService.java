package com.proyecto.demo.clientes;

import java.time.LocalDate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientesService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ClientesRepository repositorio;

    public ClientesService(ClientesRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public void crear(Cliente cliente) {
        Cliente limpio = limpiar(cliente);
        validarCliente(limpio);
        repositorio.crear(limpio);
    }

    @Transactional
    public void actualizar(Cliente cliente) {
        if (cliente == null || cliente.idCliente() == null) {
            throw new IllegalArgumentException("Debe indicar el cliente a actualizar.");
        }
        Cliente limpio = limpiar(cliente);
        validarCliente(limpio);
        repositorio.actualizar(limpio);
    }

    @Transactional
    public void eliminar(Long idCliente) {
        if (idCliente == null) {
            throw new IllegalArgumentException("Debe indicar el cliente a eliminar.");
        }
        if (repositorio.tieneVentas(idCliente)) {
            throw new IllegalArgumentException("No se puede borrar, tiene ventas");
        }
        repositorio.eliminar(idCliente);
    }

    private Cliente limpiar(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        String nombre = cliente.nombre() == null ? null : cliente.nombre().trim();
        String direccion = cliente.direccion() == null ? null : cliente.direccion().trim();
        String nit = cliente.nit() == null ? null : cliente.nit().trim();
        String correo = cliente.correo() == null ? null : cliente.correo().trim();
        String telefono = cliente.telefono() == null ? null : cliente.telefono().trim();
        if (correo != null && correo.isBlank()) {
            correo = null;
        }
        if (telefono != null && telefono.isBlank()) {
            telefono = null;
        }
        return new Cliente(
                cliente.idCliente(),
                nombre,
                direccion,
                nit,
                correo,
                telefono,
                cliente.fechaRegistro() == null ? LocalDate.now() : cliente.fechaRegistro());
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Los datos del cliente son obligatorios.");
        }
        if (cliente.nombre() == null || cliente.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (cliente.correo() != null && !cliente.correo().isBlank() && !EMAIL_PATTERN.matcher(cliente.correo()).matches()) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
    }
}
