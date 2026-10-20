package com.proyecto.demo.ventas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VentasRepository {

    private final JdbcTemplate jdbc;

    public VentasRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_BASE = """
            SELECT v.id_Venta, v.id_Cliente, c.nombre AS nombre_cliente, v.fecha_venta,
                   v.numero_factura, v.forma_pago, v.descuento, v.total_venta
            FROM Ventas v
            JOIN Clientes c ON c.id_Cliente = v.id_Cliente
            """;

    private Venta mapear(java.sql.ResultSet rs, int fila) throws java.sql.SQLException {
        return new Venta(
                rs.getLong("id_Venta"),
                rs.getLong("id_Cliente"),
                rs.getString("nombre_cliente"),
                rs.getObject("fecha_venta", LocalDate.class),
                rs.getString("numero_factura"),
                rs.getString("forma_pago"),
                rs.getBigDecimal("descuento"),
                rs.getBigDecimal("total_venta"));
    }

    // Listar (con buscador por factura o nombre de cliente)
    public List<Venta> listar(String buscar) {
        if (buscar == null || buscar.isBlank()) {
            return jdbc.query(SELECT_BASE + " ORDER BY v.id_Venta DESC", this::mapear);
        }
        String patron = "%" + buscar.trim().toUpperCase() + "%";
        return jdbc.query(SELECT_BASE
                + " WHERE UPPER(v.numero_factura) LIKE ? OR UPPER(c.nombre) LIKE ?"
                + " ORDER BY v.id_Venta DESC", this::mapear, patron, patron);
    }

    // Buscar una venta por id (para editar)
    public Venta buscarPorId(long id) {
        List<Venta> r = jdbc.query(SELECT_BASE + " WHERE v.id_Venta = ?", this::mapear, id);
        return r.isEmpty() ? null : r.get(0);
    }

    // Lista desplegable de clientes (módulo de Clientes)
    public List<ClienteOpcion> listarClientes() {
        return jdbc.query("SELECT id_Cliente, nombre FROM Clientes ORDER BY nombre",
                (rs, fila) -> new ClienteOpcion(rs.getLong("id_Cliente"), rs.getString("nombre")));
    }

    // Crear
    public void crear(long idCliente, LocalDate fecha, String factura, String formaPago,
            BigDecimal descuento, BigDecimal total) {
        jdbc.update("""
                INSERT INTO Ventas (id_Cliente, fecha_venta, numero_factura, forma_pago, descuento, total_venta)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                idCliente, java.sql.Date.valueOf(fecha), factura, formaPago, descuento, total);
    }

    // Actualizar por id_Venta
    public void actualizar(long id, long idCliente, LocalDate fecha, String factura, String formaPago,
            BigDecimal descuento, BigDecimal total) {
        jdbc.update("""
                UPDATE Ventas SET id_Cliente = ?, fecha_venta = ?, numero_factura = ?,
                       forma_pago = ?, descuento = ?, total_venta = ?
                WHERE id_Venta = ?
                """,
                idCliente, java.sql.Date.valueOf(fecha), factura, formaPago, descuento, total, id);
    }

    // Borrar
    public void eliminar(long id) {
        jdbc.update("DELETE FROM Ventas WHERE id_Venta = ?", id);
    }
}
