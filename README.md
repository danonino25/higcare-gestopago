# 🛒 HIGCARE - Módulo de Integración GestoPago & Gestión Bancaria

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15.x-blue?style=flat&logo=postgresql)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=java)

## 📖 Descripción del Proyecto
Este sistema gestiona la sincronización de catálogos de productos con la API externa de **GestoPago** y la simulación de transacciones bancarias. Está diseñado bajo una arquitectura tolerante a fallos (*Failover*) que garantiza respuestas de baja latencia servidas directamente desde una base de datos PostgreSQL local.

---

## 🛠️ Tecnologías Utilizadas
- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3
- **Persistencia**: Spring Data JPA / Hibernate
- **Base de Datos**: PostgreSQL
- **Cliente HTTP**: RestTemplate / WebClient
- **Pruebas de Carga**: Apache JMeter

---

## 🏗️ Arquitectura y Estrategia de Failover
El servicio consulta los productos utilizando una jerarquía de redundancia:
1. **PostgreSQL Principal**: Consulta primaria local (< 50 ms).
2. **Caché / BD Repuesto**: Consultada en caso de degradación o indisponibilidad de la BD principal.
3. **API Directo GestoPago**: Consumo directo como último recurso en caso de vacíos en las bases de datos locales.

---

## ⚙️ Configuración e Instalación

### 1. Requisitos Previos
- Java JDK 17 o superior.
- PostgreSQL en ejecución en el puerto `5432`.
- Maven o Gradle.

### 2. Configuración de Base de Datos
Crear la base de datos en PostgreSQL:
```sql
CREATE DATABASE higcare_db;
