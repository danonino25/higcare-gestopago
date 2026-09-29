# 🛒 HIGCARE - Módulo de Integración GestoPago & Gestión Bancaria

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.x-blue?style=flat&logo=postgresql)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=java)

## 📖 Descripción del Proyecto

Este sistema gestiona la sincronización de catálogos de productos con la API externa de **GestoPago** y la simulación de transacciones bancarias. Está diseñado bajo una arquitectura tolerante a fallos (*Failover*) que garantiza respuestas de baja latencia, servidas directamente desde una base de datos PostgreSQL local.

---

## 🛠️ Tecnologías Utilizadas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3 |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL |
| Cliente HTTP | RestTemplate / WebClient |
| Pruebas de carga | Apache JMeter |

---

## 🏗️ Arquitectura y Estrategia de Failover

El servicio consulta los productos utilizando una jerarquía de redundancia:

1. **PostgreSQL principal**: consulta primaria local (< 50 ms).
2. **Caché / BD de respaldo**: se consulta en caso de degradación o indisponibilidad de la BD principal.
3. **API directa de GestoPago**: consumo directo como último recurso cuando las bases de datos locales no tienen datos.

---

## ⚙️ Configuración e Instalación

### 1. Requisitos previos

- Java JDK 17 o superior
- PostgreSQL en ejecución en el puerto `5432`
- Maven o Gradle

### 2. Configuración de la base de datos

Crear la base de datos en PostgreSQL:

```sql
CREATE DATABASE higcare_db;
```

Configurar las credenciales en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/higcare_db
spring.datasource.username=postgres
spring.datasource.password=TU_CONTRASEÑA
spring.jpa.hibernate.ddl-auto=update

gestopago.api.url=https://api.gestopago.com/v1/products
```

> ⚠️ **Importante:** no subas tus credenciales reales al repositorio. Usa variables de entorno o un archivo de configuración local ignorado por Git.

### 3. Ejecución del proyecto

Con Gradle:

```bash
./gradlew bootRun
```

Con Maven:

```bash
./mvnw spring-boot:run
```

---

## 📡 Endpoints del Sistema

### 1. Consultar productos

- **URL:** `GET /api/v1/productos`
- **Descripción:** retorna el catálogo desde la BD local.

**Respuesta `200 OK`:**

```json
[
  {
    "id": "PROD-01",
    "nombre": "Servicio Recarga",
    "precio": 100.0,
    "imageUrl": "https://ejemplo.com/img.png",
    "fechaActualizacion": "2026-09-21T12:00:00"
  }
]
```

### 2. Control de errores

Ejemplo de fallo de red:

- **Código HTTP:** `503 Service Unavailable`

```json
{
  "codigo": "ERR_NET_03",
  "descripcion": "Sin conexión con el servicio externo de GestoPago.",
  "path": "/api/v1/productos",
  "timestamp": "2026-09-21T17:55:00"
}
```

---

## ⏱️ Proceso Programado (CronJob)

El sistema ejecuta una tarea en segundo plano todos los días a las 12:00 PM para sincronizar el catálogo local con la API de GestoPago.

| Parámetro | Valor |
|---|---|
| Expresión cron | `0 0 12 * * *` |
| Frecuencia | Diaria, 12:00 PM |

---

## 🧪 Pruebas de Carga

Las pruebas de rendimiento se realizan con **Apache JMeter** sobre el endpoint `GET /api/v1/productos`, para validar los tiempos de respuesta de la BD local.
