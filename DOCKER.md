
## Infraestructura de Persistencia (Docker Compose)

Esta configuración levanta dos motores de base de datos relacionales en paralelo, aislados en contenedores, para soportar la persistencia del `data-service`.

### Arquitectura de Contenedores y Puertos

Se utilizan puertos no estándar en el **Host** para evitar conflictos con instalaciones locales de MySQL (3306) o PostgreSQL (5432) en la máquina de desarrollo.

| Servicio | Motor / Imagen | Puerto Host (Externo) | Puerto Contenedor (Interno) | Base de Datos (Schema) | Password Root/User |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **mysql** | `mysql:8.0` | **3307** | 3306 | `data_db` | `password` |
| **postgres** | `postgres:15` | **5433** | 5432 | `data_db` | `password` |

### Detalles de Configuración

#### 1\. MySQL Service

* **Versión:** 8.0 (LTS).
* **Variables de Entorno:**
    * `MYSQL_ROOT_PASSWORD`: Contraseña para el usuario `root`.
    * `MYSQL_DATABASE`: Crea automáticamente el esquema `data_db` al iniciar si no existe.
* **Healthcheck:**
    * Ejecuta `mysqladmin ping` cada 5 segundos.
    * Garantiza que el contenedor solo se marque como "Healthy" cuando el socket de MySQL esté aceptando conexiones, previniendo errores de conexión en el arranque de los microservicios dependientes.

#### 2\. PostgreSQL Service

* **Versión:** 15.
* **Variables de Entorno:**
    * `POSTGRES_PASSWORD`: Contraseña para el superusuario `postgres`.
    * `POSTGRES_DB`: Crea automáticamente la base de datos `data_db`.
* **Healthcheck:**
    * Ejecuta `pg_isready` (herramienta nativa de Postgres) cada 5 segundos.
    * Verifica que el servidor acepte conexiones TCP/IP.

### Comandos de Ejecución

Ejecutar desde la raíz del proyecto donde se encuentra el `docker-compose.yml`:

```
# 1. Iniciar los servicios en segundo plano (Detached mode)
docker compose up -d

# 2. Verificar estado y salud (Esperar a que STATUS sea 'healthy')
docker compose ps

# 3. Ver logs en tiempo real (útil para depurar inicialización)
docker compose logs -f

# 4. Detener y eliminar contenedores
docker compose down
```
