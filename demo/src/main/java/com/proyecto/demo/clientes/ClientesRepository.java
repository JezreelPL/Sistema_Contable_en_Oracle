package com.proyecto.demo.clientes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ClientesRepository {

    private final JdbcTemplate jdbc;

    public ClientesRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Cliente> rowMapper = new RowMapper<>() {
        @Override
        public Cliente mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Cliente(
                    rs.getLong("id_Cliente"),
                    rs.getString("nombre"),
                    rs.getString("direccion"),
                    rs.getString("nit"),
                    rs.getString("correo"),
                    rs.getString("telefono"),
                    rs.getObject("fecha_registro", LocalDate.class)
            );
        }
    };

    public List<Cliente> listar(String filtro) {
        String valor = filtro == null ? "" : filtro.trim();
        if (valor.isBlank()) {
            return jdbc.query(
                    "SELECT id_Cliente, nombre, direccion, nit, correo, telefono, fecha_registro FROM Clientes ORDER BY id_Cliente DESC",
                    rowMapper);
        }

        String sql = """
                SELECT id_Cliente, nombre, direccion, nit, correo, telefono, fecha_registro
                FROM Clientes
                WHERE LOWER(nombre) LIKE LOWER(?) OR LOWER(nit) LIKE LOWER(?)
                ORDER BY id_Cliente DESC
                """;
        String busqueda = "%" + valor + "%";
        return jdbc.query(sql, rowMapper, busqueda, busqueda);
    }

    public Optional<Cliente> buscarPorId(Long idCliente) {
        String sql = """
                SELECT id_Cliente, nombre, direccion, nit, correo, telefono, fecha_registro
                FROM Clientes
                WHERE id_Cliente = ?
                """;
        List<Cliente> resultados = jdbc.query(sql, rowMapper, idCliente);
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    public void crear(Cliente cliente) {
        String sql = """
                INSERT INTO Clientes (nombre, direccion, nit, correo, telefono, fecha_registro)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql,
                cliente.nombre(),
                cliente.direccion(),
                cliente.nit(),
                cliente.correo(),
                cliente.telefono(),
                java.sql.Date.valueOf(cliente.fechaRegistro()));
    }

    public void actualizar(Cliente cliente) {
        String sql = """
                UPDATE Clientes
                SET nombre = ?,
                    direccion = ?,
                    nit = ?,
                    correo = ?,
                    telefono = ?,
                    fecha_registro = ?
                WHERE id_Cliente = ?
                """;
        jdbc.update(sql,
                cliente.nombre(),
                cliente.direccion(),
                cliente.nit(),
                cliente.correo(),
                cliente.telefono(),
                java.sql.Date.valueOf(cliente.fechaRegistro()),
                cliente.idCliente());
    }

    public void eliminar(Long idCliente) {
        jdbc.update("DELETE FROM Clientes WHERE id_Cliente = ?", idCliente);
    }

    public boolean tieneVentas(Long idCliente) {
        Integer conteo = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Ventas WHERE id_Cliente = ?",
                Integer.class,
                idCliente);
        return conteo != null && conteo > 0;
    }
}
