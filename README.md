# APLICATIVOWEBSPA — Spa Serenity

Aplicación web tipo SPA para la gestión y agendamiento de citas de un spa. Construida con Spring Boot 2.7.18 (Java 8), JPA/Hibernate, MySQL 8, JWT y un frontend estático (HTML + JS + CSS) servido desde el mismo backend.

## Stack

- **Backend:** Spring Boot 2.7.18, Spring Security (JWT), Spring Data JPA, Hibernate.
- **Base de datos:** MySQL 8 (schema `spa_db`).
- **Frontend:** `src/main/resources/static/` (HTML5 + ES6 + CSS, sin framework).
- **Build:** Maven 3.x.
- **JDK:** 8 (configurado en `pom.xml`).

## Cómo correr en local

```sql
-- En MySQL
CREATE DATABASE spa_db;
USE spa_db;
SOURCE database_backup.sql;
```

Editar `src/main/resources/application.properties` con tu usuario y contraseña de MySQL si difieren del default, y luego:

```bash
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

### Usuarios de prueba (incluidos en el backup)

| Usuario       | Contraseña            | Rol        |
|---------------|-----------------------|------------|
| `CUANTICU_`   | `JDESGOD123`          | CLIENTE    |
| `dagonch21`   | `12345678910`         | TERAPEUTA  |
| `juanda2206`  | `Juandavidpalomo12`   | ADMIN      |

## Roles del sistema

| Rol         | Permisos                                                   |
|-------------|------------------------------------------------------------|
| CLIENTE     | Ver servicios + agendar citas                              |
| TERAPEUTA   | Revisar las citas asignadas a su perfil                    |
| ADMIN       | Todo lo anterior + CRUD de servicios (crear, editar, borrar)|

---

## Validación de agendamiento de citas

El sistema impide que un mismo terapeuta tenga dos citas que se pisen entre sí. La validación se ejecuta cada vez que un cliente pulsa **Confirmar Cita** y se apoya en tres elementos:

1. **Duración del servicio** (`services.duration_minutes`): cada servicio del catálogo declara cuántos minutos dura. El backend calcula la hora de fin como `inicio + duration_minutes`.
2. **Hora de fin almacenada por cita** (`appointments.appointment_end_time`): se persiste junto con la cita para que la consulta de solapamiento sea O(1) por candidato.
3. **Detección de solapamiento por overlap clásico de intervalos**: dos bloques `[A.inicio, A.fin)` y `[B.inicio, B.fin)` se solapan si y solo si `A.inicio < B.fin` y `A.fin > B.inicio`. La query JPQL vive en [`AppointmentRepository.java`](src/main/java/com/spa/bookingapp/repository/AppointmentRepository.java) y excluye estados cancelados (`CANCELLED`, `CANCELED`, `CANCELADA`).

### Reglas implementadas

- Si el terapeuta X ya tiene una cita confirmada a las **14:00 con duración 60 min**, queda bloqueado el rango **[14:00, 15:00)** para él.
  - ❌ Otra cita a las 14:00 con el mismo terapeuta → **rechazada**.
  - ❌ Otra cita a las 14:30 con el mismo terapeuta → **rechazada** (overlap).
  - ❌ Otra cita a las 13:30 con servicio de 60 min → **rechazada** (termina a las 14:30, overlap).
  - ✅ Otra cita a las 15:00 con el mismo terapeuta → **aceptada** (sin overlap).
  - ✅ Otra cita a las 14:00 con un terapeuta distinto → **aceptada**.
- No se permite agendar en el pasado: si `appointmentTime <= NOW()` se devuelve mensaje específico.
- El mensaje de error de overlap incluye la **próxima franja libre** calculada a partir del fin de la última cita en conflicto, para que el cliente sepa qué hora pedir.

### Mensajes de error que devuelve el endpoint `POST /api/appointments`

| Caso                          | Mensaje                                                                                          |
|-------------------------------|--------------------------------------------------------------------------------------------------|
| Formato de fecha inválido     | `Formato de fecha inválido.`                                                                     |
| Cita en el pasado             | `No se puede agendar una cita en el pasado. Elegí una fecha y hora futura.`                      |
| Conflicto de overlap          | `Esa franja horaria ya está reservada para este terapeuta. Disponibilidad más cercana: <hora>`   |

## Estructura relevante

```
src/main/java/com/spa/bookingapp/
├── controller/AppointmentController.java   # endpoint POST /api/appointments
├── model/Appointment.java                  # entity con appointmentTime + appointmentEndTime
├── model/SpaService.java                   # entity con durationMinutes
├── repository/AppointmentRepository.java   # query overlap + query lista citas en conflicto
└── DataSeeder.java                         # limpia citas con appointmentEndTime=NULL al arrancar
```

## Endpoint de diagnóstico

`GET /api/appointments/debug` devuelve la lista completa de citas con `start`, `end`, `therapistId` y `status` — útil cuando un cliente reporta un "no disponible" para verificar qué cita está bloqueando el slot.
