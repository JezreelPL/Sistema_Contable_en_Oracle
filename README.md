# Sistema Contable GameZone: Interfaz de entrada de datos sobre Oracle

Proyecto 2 del curso de Base de Datos / Sistemas de Información. Aplicación web (Spring Boot + Thymeleaf + JdbcTemplate) que se conecta a Oracle Database y permite gestionar 5 tablas del sistema contable de GameZone, una tienda de videojuegos.

## Módulos

La aplicación tiene 4 utilitarios, todos con CRUD completo, validaciones y mensajes claros cuando Oracle rechaza una operación. El menú los muestra en este orden:

| Módulo | Ruta | Tabla(s) | Depende de |
|---|---|---|---|
| Catálogo de cuentas | `/catalogo` | `Catalogo_Cuentas` | Ninguno |
| Clientes | `/clientes` | `Clientes` | Ninguno |
| Ventas | `/ventas` | `Ventas` | Clientes |
| Asientos contables | `/asientos` | `Asientos_Contables` y `Asientos_Detalle` | Catálogo y Ventas |

Flujo: Ventas toma sus clientes de Clientes; Asientos toma sus cuentas del Catálogo y su venta de origen de Ventas. En el listado de ventas, el botón **Generar asiento** abre Asientos con esa venta ya elegida.

## Tecnologías

- Java 21
- Spring Boot 4.1.1 (Web MVC, Thymeleaf, JDBC)
- Oracle JDBC `ojdbc11` 21.9.0.0
- Oracle Database XE, PDB `XEPDB1`
- Maven (se incluye Maven Wrapper)

## Requisitos previos

1. **JDK 21** instalado (`java -version` debe mostrar la versión 21).
2. **Oracle Database XE** en ejecución, con la PDB `XEPDB1` abierta y el listener activo en el puerto `1521`.
3. **Conexión a internet** la primera vez que se ejecuta, para que Maven descargue las dependencias.
4. Una herramienta para ejecutar scripts SQL, por ejemplo Oracle SQL Developer.

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/JezreelPL/Sistema_Contable_en_Oracle.git
cd Sistema_Contable_en_Oracle
```

### 2. Preparar la base de datos

Conéctate a la PDB `XEPDB1` con un usuario administrador (por ejemplo `SYSTEM`) y crea el usuario de la aplicación, si todavía no existe:

```sql
CREATE USER presentacion IDENTIFIED BY presentacion;
GRANT CONNECT, RESOURCE TO presentacion;
ALTER USER presentacion QUOTA UNLIMITED ON USERS;
```

Luego conéctate como `presentacion` y ejecuta el script DDL del Proyecto 1 (`gamezone_ddl_completo.sql`), que crea las 22 tablas. La aplicación usa 5 de ellas: `Clientes`, `Ventas`, `Catalogo_Cuentas`, `Asientos_Contables` y `Asientos_Detalle`.

Para verificar que quedaron creadas:

```sql
SELECT table_name FROM user_tables ORDER BY table_name;
```

### 3. Configurar la conexión

La conexión se configura en `demo/src/main/resources/`, en dos archivos:

| Archivo | Contenido | ¿Está en el repositorio? |
|---|---|---|
| `application.properties` | Cadena de conexión y usuario | Sí |
| `application-local.properties` | Contraseña | **No** (está en `.gitignore`) |

`application.properties` ya trae estos valores:

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/XEPDB1
spring.datasource.username=presentacion
```

Cada persona debe **crear** el archivo `demo/src/main/resources/application-local.properties` con la contraseña del usuario. Para el usuario de demostración del proyecto:

```properties
spring.datasource.password=presentacion
```

Si tu base de datos usa otro host, puerto, servicio, usuario o contraseña, ajusta esos valores en los archivos anteriores.

## Ejecución

Desde la carpeta `demo` (la que contiene `pom.xml`):

```bash
# Linux / macOS (si hace falta: chmod +x mvnw)
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

También se puede ejecutar la clase `DemoApplication` desde VS Code (extensión Spring Boot Dashboard o Java).

Cuando la consola muestre que la aplicación inició, abre el navegador en:

**http://localhost:8080**

Desde la página de inicio se accede a los 4 módulos.

## Prueba rápida (historia de la demo)

La demo cuenta la venta de una PS5 a Andrea López:

1. **Catálogo:** crear la cuenta `1101` «Cuentas por Cobrar» (tipo Activo) y una cuenta de ingresos (tipo Ingreso).
2. **Clientes:** crear el cliente Andrea López.
3. **Ventas:** crear la venta con factura `F-0001`, forma de pago Crédito y total 4500.
4. **Asientos:** usar **Generar asiento** en la venta y registrar 4500 al debe en Cuentas por Cobrar y 4500 al haber en Ingresos.

Para ver los errores de integridad:

- Borrar el cliente que tiene ventas (ORA-02292).
- Guardar un asiento descuadrado (se rechaza y no se guarda nada).

## Estructura del proyecto

```
demo/
├── pom.xml
└── src/main/
    ├── java/com/proyecto/demo/
    │   ├── DemoApplication.java
    │   ├── InicioController.java
    │   ├── catalogo/      (Controller, Service, Repository, record)
    │   ├── clientes/
    │   ├── ventas/
    │   └── asientos/
    └── resources/
        ├── application.properties
        ├── application-local.properties   (no se sube al repositorio)
        └── templates/
            ├── fragments/ (head, menu)
            ├── inicio.html
            └── catalogo/, clientes/, ventas/, asientos/
```

Cada módulo sigue el mismo patrón: un **Controller** recibe la petición, un **Service** valida y controla la transacción, un **Repository** ejecuta el SQL con `JdbcTemplate`, y las plantillas de Thymeleaf muestran las pantallas.

## Solución de problemas

| Mensaje o síntoma | Causa probable | Solución |
|---|---|---|
| `ORA-01017: invalid username/password` | Usuario o contraseña incorrectos | Revisar `application-local.properties` y el usuario en `application.properties`. |
| `ORA-12541: no listener` | El listener de Oracle no está activo | Iniciar los servicios de Oracle XE. |
| `ORA-12514` | El servicio `XEPDB1` no está registrado o abierto | Verificar que la PDB esté abierta y que el nombre del servicio sea correcto. |
| `ORA-00942: table or view does not exist` | No se ejecutó el script DDL con ese usuario | Ejecutar `gamezone_ddl_completo.sql` conectado como `presentacion`. |
| `Port 8080 was already in use` | Otra aplicación usa el puerto | Cerrar la otra aplicación o agregar `server.port=8081` en `application.properties`. |
| Falla al descargar dependencias | Sin conexión a internet la primera vez | Conectarse a internet y volver a ejecutar. |
