package com.proyecto.demo.catalogo;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogoRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Cuenta> mapeo = (rs, fila) -> new Cuenta(
            rs.getLong("id_Cuenta_Contable"),
            rs.getString("codigo"),
            rs.getString("nombre"),
            rs.getString("tipo_cuenta"));

    public CatalogoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Listar todas las cuentas
    public List<Cuenta> listar() {
        String sql = """
                SELECT id_Cuenta_Contable, codigo, nombre, tipo_cuenta
                FROM Catalogo_Cuentas
                ORDER BY codigo
                """;
        return jdbc.query(sql, mapeo);
    }

    // Buscar por nombre o código (sin distinguir mayúsculas de minúsculas)
    public List<Cuenta> buscar(String texto) {
        String sql = """
                SELECT id_Cuenta_Contable, codigo, nombre, tipo_cuenta
                FROM Catalogo_Cuentas
                WHERE UPPER(codigo) LIKE ? OR UPPER(nombre) LIKE ?
                ORDER BY codigo
                """;
        String patron = "%" + texto.toUpperCase() + "%";
        return jdbc.query(sql, mapeo, patron, patron);
    }

    // Obtener una cuenta por su id (null si no existe)
    public Cuenta buscarPorId(long id) {
        String sql = """
                SELECT id_Cuenta_Contable, codigo, nombre, tipo_cuenta
                FROM Catalogo_Cuentas
                WHERE id_Cuenta_Contable = ?
                """;
        List<Cuenta> resultado = jdbc.query(sql, mapeo, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // Crear (el id es IDENTITY, no se envía)
    public void crear(String codigo, String nombre, String tipo) {
        jdbc.update(
                "INSERT INTO Catalogo_Cuentas (codigo, nombre, tipo_cuenta) VALUES (?, ?, ?)",
                codigo, nombre, tipo);
    }

    // Actualizar por id_Cuenta_Contable; devuelve filas afectadas
    public int actualizar(long id, String codigo, String nombre, String tipo) {
        return jdbc.update(
                "UPDATE Catalogo_Cuentas SET codigo = ?, nombre = ?, tipo_cuenta = ? WHERE id_Cuenta_Contable = ?",
                codigo, nombre, tipo, id);
    }

    // Borrar por id_Cuenta_Contable; devuelve filas afectadas
    public int borrar(long id) {
        return jdbc.update("DELETE FROM Catalogo_Cuentas WHERE id_Cuenta_Contable = ?", id);
    }
}
