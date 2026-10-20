package com.proyecto.demo.catalogo;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class CatalogoService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoService.class);

    // Tipos de cuenta acordados por el grupo. Para agregar más, basta con añadirlos aquí.
    public static final List<String> TIPOS = List.of("Activo", "Pasivo", "Ingreso");

    private static final int MAX_CODIGO = 20;
    private static final int MAX_NOMBRE = 100;
    private static final Pattern FORMATO_CODIGO = Pattern.compile("[A-Za-z0-9.\\-]+");

    private final CatalogoRepository repositorio;

    public CatalogoService(CatalogoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Cuenta> listar(String buscar) {
        if (buscar == null || buscar.isBlank()) {
            return repositorio.listar();
        }
        return repositorio.buscar(buscar.trim());
    }

    public Cuenta obtener(long id) {
        return repositorio.buscarPorId(id);
    }

    public void crear(String codigo, String nombre, String tipo) {
        validar(codigo, nombre, tipo);
        try {
            repositorio.crear(codigo.trim(), nombre.trim(), tipo);
        } catch (DataAccessException e) {
            throw new IllegalArgumentException(traducir(e));
        }
    }

    public void actualizar(long id, String codigo, String nombre, String tipo) {
        validar(codigo, nombre, tipo);
        try {
            int filas = repositorio.actualizar(id, codigo.trim(), nombre.trim(), tipo);
            if (filas == 0) {
                throw new IllegalArgumentException("La cuenta ya no existe.");
            }
        } catch (DataAccessException e) {
            throw new IllegalArgumentException(traducir(e));
        }
    }

    public void borrar(long id) {
        try {
            int filas = repositorio.borrar(id);
            if (filas == 0) {
                throw new IllegalArgumentException("La cuenta ya no existe.");
            }
        } catch (DataAccessException e) {
            throw new IllegalArgumentException(traducir(e));
        }
    }

    // Validaciones previas al envío a Oracle
    private void validar(String codigo, String nombre, String tipo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (codigo.trim().length() > MAX_CODIGO) {
            throw new IllegalArgumentException("El código no puede superar los " + MAX_CODIGO + " caracteres.");
        }
        if (!FORMATO_CODIGO.matcher(codigo.trim()).matches()) {
            throw new IllegalArgumentException("El código solo puede contener letras, números, punto y guion.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (nombre.trim().length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede superar los " + MAX_NOMBRE + " caracteres.");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio.");
        }
        if (!TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("El tipo de cuenta no es válido.");
        }
    }

    // Convierte el error de Oracle en un mensaje claro, sin mostrar el error técnico
    private String traducir(DataAccessException e) {
        int codigoOra = codigoOracle(e);
        log.warn("Oracle rechazó la operación del catálogo (ORA-{}): {}", codigoOra, e.getMostSpecificCause().getMessage());
        return switch (codigoOra) {
            case 1 -> "Ese código ya existe.";
            case 2291 -> "El registro relacionado no existe.";
            case 2292 -> "No se puede borrar, tiene registros relacionados: la cuenta ya se usa en asientos contables.";
            case 12899 -> "Algún dato excede la longitud máxima permitida.";
            default -> "No se pudo completar la operación. Intenta de nuevo.";
        };
    }

    private int codigoOracle(Throwable e) {
        Throwable actual = e;
        while (actual != null) {
            if (actual instanceof SQLException sql) {
                return sql.getErrorCode();
            }
            actual = actual.getCause();
        }
        return 0;
    }
}
