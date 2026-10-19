package com.proyecto.demo.asientos;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class AsientosRepository {

    private final JdbcTemplate jdbc;

    public AsientosRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Listar los Asientos
    public List<Asiento> listar() {
        String sql = """
                SELECT a.id_Asiento, a.fecha_asiento, a.descripcion, v.numero_factura,
                       (SELECT NVL(SUM(d.debe), 0)
                        FROM Asientos_Detalle d
                        WHERE d.id_Asiento = a.id_Asiento) AS total
                FROM Asientos_Contables a
                LEFT JOIN Ventas v ON v.id_Venta = a.id_Venta
                ORDER BY a.id_Asiento DESC
                """;
        return jdbc.query(sql, (rs, fila) -> new Asiento(
                rs.getLong("id_Asiento"),
                rs.getObject("fecha_asiento", LocalDate.class),
                rs.getString("descripcion"),
                rs.getString("numero_factura"),
                rs.getBigDecimal("total")));
    }

    // Listar las ventas
    public List<VentaOpcion> listarVentas() {
        return jdbc.query(
                "SELECT id_Venta, numero_factura FROM Ventas ORDER BY id_Venta DESC",
                (rs, fila) -> new VentaOpcion(rs.getLong("id_Venta"), rs.getString("numero_factura")));
    }

    // Crear un asiento
    public long crear(LocalDate fecha, String descripcion, long idVenta) {
        String sql = "INSERT INTO Asientos_Contables (fecha_asiento, descripcion, id_Venta) VALUES (?, ?, ?)";
        KeyHolder llave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] { "ID_ASIENTO" });
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ps.setString(2, descripcion);
            ps.setLong(3, idVenta);
            return ps;
        }, llave);
        return llave.getKey().longValue();
    }
    
    //listar cuentas
    public List<CuentaOpcion> listarCuentas() {
        return jdbc.query(
                "SELECT id_Cuenta_Contable, codigo, nombre FROM Catalogo_Cuentas ORDER BY codigo",
                (rs, fila) -> new CuentaOpcion(
                        rs.getLong("id_Cuenta_Contable"),
                        rs.getString("codigo"),
                        rs.getString("nombre")));
    }
    //Crear linea 
    public void crearLinea(long idAsiento, LineaAsiento linea) {
        jdbc.update(
                "INSERT INTO Asientos_Detalle (id_Asiento, id_Cuenta_Contable, debe, haber) VALUES (?, ?, ?, ?)",
                idAsiento, linea.idCuenta(), linea.debe(), linea.haber());
    }

}