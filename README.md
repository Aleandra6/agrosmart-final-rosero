# 🌾 Universidad de las Fuerzas Armadas ESPE

## Examen Final Práctico — Programación Avanzada

### Caso integrador: **AgroSmart** 


## Datos del estudiante

- **Nombre:** Ana Alejandra Rosero Chiluisa
- **Cédula:** 1753348620
- **Categoría asignada:** Cacao
- **Semilla:** 20

---

# Descripción del proyecto

AgroSmart es una aplicación desarrollada con Spring Boot que integra persistencia con PostgreSQL mediante JPA/Hibernate, programación reactiva con Spring WebFlux y Project Reactor, además de un módulo de inteligencia artificial utilizando LangChain4j para generar publicidad de productos.

El proyecto implementa un modelo de dominio inmutable separado de la entidad de persistencia, siguiendo buenas prácticas de arquitectura y programación funcional.

---

# Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.16
- Spring WebFlux
- Spring Data JPA (Hibernate)
- PostgreSQL 16
- Docker
- LangChain4j
- Gradle

---

# Configuración

## Base de datos

La base de datos PostgreSQL se ejecuta mediante Docker.

**Base de datos**

```
agrosmart_db
```

**Usuario**

```
agrosmart
```

**Puerto PostgreSQL (Host)**

```
55432
```

**Puerto de la aplicación**

```
8120
```

---

# Tabla creada

La aplicación trabaja con la siguiente tabla:

```
tbl_productos_base_20
```

---

# Ejecución del proyecto

## Levantar PostgreSQL

```bash
docker compose up -d
```

## Ejecutar la aplicación

```bash
gradlew bootRun --args="--spring.profiles.active=prod"
```

---

# Endpoints implementados

## Obtener todos los productos

```
GET /api/productos
```

---

## Buscar producto por ID

```
GET /api/productos/{id}
```

---

## Generar publicidad con IA

```
GET /api/agrosmart/publicidad
```

---

# Arquitectura

El proyecto está compuesto por las siguientes capas:

- Controller
- Service
- Repository
- Entity
- Domain
- Mapper

La entidad `ProductoEntity` representa la persistencia en PostgreSQL, mientras que `Producto` corresponde al modelo de dominio inmutable.

Las consultas realizadas mediante JPA son operaciones bloqueantes; por ello se ejecutan utilizando:

```
Schedulers.boundedElastic()
```

De esta forma se evita bloquear el event loop de Netty y la aplicación mantiene un comportamiento reactivo.

---

# Características implementadas

- Configuración mediante perfiles de Spring Boot.
- Persistencia con PostgreSQL utilizando Hibernate.
- Modelo de dominio inmutable.
- Copias defensivas de listas.
- Transformación Entity → Domain mediante Mapper.
- Programación reactiva con Reactor.
- API REST desarrollada con Spring WebFlux.
- Integración con LangChain4j para generación de publicidad.
- Manejo de errores mediante `switchIfEmpty()` y `onErrorResume()`.
- Pruebas unitarias con JUnit 5, Mockito y StepVerifier.

---

# Evidencias

Las capturas de pantalla utilizadas durante el desarrollo se encuentran en:

```
docs/evidencias/
```

---

# Repositorio

Repositorio GitHub:

https://github.com/Aleandra6/agrosmart-final-rosero
