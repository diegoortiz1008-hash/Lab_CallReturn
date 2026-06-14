# Lab_CallReturn
 
Este repositorio contiene la implementación progresiva de un mismo caso (gestión de salones / inventario / sistema de bienestar universitario) evolucionando a través de distintos estilos arquitectónicos: Sockets TCP, HTTP, RMI, gRPC, Microservicios y API Gateway.
 
---
 
## 2.3 — Sistema de Gestión de Salones (Sockets TCP)
 
*Carpeta: salon-TCP*
 
### Arquitectura
Cliente-servidor clásico sobre sockets TCP. El protocolo de comunicación es un formato de texto plano definido manualmente (`OPERACION,CODIGO`). El servidor escucha en el puerto **35000**, atiende una conexión a la vez y mantiene el estado de los salones en memoria.
 
### Qué se hizo
- `Salon.java`: modelo con código y estado (disponible/reservado).
- `SalonRepository.java`: repositorio en memoria con salones E301-E304.
- `SalonServer.java`: servidor TCP que procesa el protocolo `CONSULTAR_SALON`, `RESERVAR_SALON`, `LIBERAR_SALON`.
- `SalonClient.java`: cliente de consola con menú interactivo.
### Cómo probarlo
```bash
# Terminal 1
javac *.java
java SalonServer
 
# Terminal 2
java SalonClient
```
Flujo de prueba: consultar (disponible) → reservar (éxito) → consultar (reservado) → reservar de nuevo (error) → liberar (éxito) → consultar código inexistente (error).
 

 
---
 
## 3.3 — Gestión de Salones vía HTTP
 
*Carpeta: salon-http*
 
### Arquitectura
Se expone la misma funcionalidad mediante un servidor HTTP construido con `com.sun.net.httpserver.HttpServer` (sin frameworks). El contrato pasa de cadenas de texto arbitrarias a rutas HTTP estándar con métodos GET/POST y parámetros de query, permitiendo el acceso desde navegador o `curl`.
 
### Qué se hizo
- Reutilización de `Salon.java` y `SalonRepository.java`.
- `SalonHttpServer.java`: servidor en el puerto **8080** con tres rutas:
  - `GET /rooms` → lista todos los salones.
  - `GET /rooms?id=E301` → consulta un salón.
  - `POST /rooms/reserve?id=E301` → reserva.
  - `POST /rooms/release?id=E301` → libera.
### Cómo probarlo
```bash
javac *.java
java SalonHttpServer
```
- Navegador: `http://localhost:8080/rooms` y `http://localhost:8080/rooms?id=E301`
- PowerShell:
```bash
curl -X POST http://localhost:8080/rooms/reserve?id=E301
curl -X POST http://localhost:8080/rooms/release?id=E301
```
 

 
---
 
## 4.3 — Inventario de Laboratorios (RMI)
 
*Carpeta: lab-rmi*
 
### Arquitectura
Comunicación mediante Java RMI (Remote Method Invocation): el cliente invoca métodos de un objeto remoto como si fuera local. El contrato se define como una interfaz Java (`LabService extends Remote`), eliminando el diseño manual de mensajes. El registro RMI corre en el puerto **23000**.
 
### Qué se hizo
- `Equipment.java`: modelo serializable (código, nombre, laboratorio, estado).
- `LabService.java`: interfaz remota con `consultarEquipos`, `consultarEquipo`, `reservarEquipo`, `liberarEquipo`.
- `LabServiceImpl.java`: implementación con datos en memoria (PC01, PC02, AR01, AR02, OS01).
- `LabRmiServer.java`: publica el servicio en el registro RMI.
- `LabRmiClient.java`: cliente de consola con menú interactivo.
### Cómo probarlo
```bash
# Terminal 1
javac *.java
java LabRmiServer
 
# Terminal 2
java LabRmiClient
```
Flujo de prueba: ver todos los equipos → consultar uno → reservarlo → consultar (debe verse reservado) → liberarlo → consultar (debe verse disponible).
 

 
---
 
## 5.3 — Sistema de Citas de Bienestar Universitario (gRPC)
 
*Carpeta: wellness-grpc*
 
### Arquitectura
Comunicación RPC moderna con gRPC. El contrato se define formalmente en `wellness.proto` (Protocol Buffers), generando automáticamente las clases Java (mensajes y stubs) vía Maven. A diferencia de RMI, el contrato es independiente del lenguaje. El servidor escucha en el puerto **50061**.
 
### Qué se hizo
- `src/main/proto/wellness.proto`: define el servicio `AppointmentService` con `RequestAppointment`, `CancelAppointment`, `GetAppointments`, y los tipos `ServiceType` (MEDICINE, PSYCHOLOGY, DENTISTRY) y `Status` (REQUESTED, CANCELLED, ATTENDED).
- `AppointmentServer.java`: implementación del servicio, citas en memoria.
- `AppointmentClient.java`: cliente de consola que solicita, cancela y consulta citas.
### Cómo probarlo
```bash
mvn clean compile
 
# Terminal 1
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AppointmentServer"
 
# Terminal 2
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AppointmentClient"
```
Flujo de prueba: solicitar cita (queda `REQUESTED`) → ver mis citas (aparece) → cancelar cita → ver mis citas (ya no aparece, pues una cita cancelada no se considera activa).
 

 
---
 
## 6.3 — Descomposición en Microservicios
 
*Carpeta: wellness-grpc* (extensión del anterior)
 
### Arquitectura
El sistema se separa en servicios pequeños y cohesivos, cada uno con su propio contrato `.proto` y su propio puerto, evitando que un solo servicio concentre toda la responsabilidad.
 
| Servicio | Responsabilidad | Puerto |
|---|---|---|
| `AppointmentService` | Gestionar citas | 50061 |
| `MedicalService` | Consultar especialidades médicas disponibles | 50062 |
 
### Qué se hizo
- `src/main/proto/medical.proto`: define `MedicalService` con `GetSpecialties` y `GetSpecialty` (especialidades MED, PSY, DEN con su disponibilidad).
- `MedicalServer.java`: servidor gRPC del nuevo servicio.
- `WellnessClient.java`: cliente que abre un canal hacia **cada** servicio (50061 y 50062) y permite consultar especialidades y solicitar citas desde un mismo menú.
### Cómo probarlo
```bash
# Terminal 1
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AppointmentServer"
 
# Terminal 2
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.MedicalServer"
 
# Terminal 3
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.WellnessClient"
```
Flujo de prueba: ver especialidades médicas (desde MedicalService) → solicitar una cita (desde AppointmentService), ambas desde el mismo cliente.
 
 
---
 
## 7.3 — WellnessGateway (API Gateway)
 
*Carpeta: wellness-grpc* (extensión del anterior)
 
### Arquitectura
Un API Gateway centraliza el acceso del cliente a múltiples microservicios. El cliente final ya no necesita conocer los puertos ni contratos individuales de `AppointmentService` y `MedicalService`: solo interactúa con el Gateway, que internamente actúa como cliente gRPC de ambos servicios y **unifica** las respuestas.
 
### Qué se hizo
- `WellnessGateway.java`: aplicación de consola que:
  - `requestAppointment`: solicita una cita a través de `AppointmentService`.
  - `getStudentWellnessSummary`: combina en una sola respuesta las citas activas del estudiante (`AppointmentService`) y las especialidades disponibles (`MedicalService`).
### Cómo probarlo
```bash
# Terminal 1
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AppointmentServer"
 
# Terminal 2
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.MedicalServer"
 
# Terminal 3
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.WellnessGateway"
```
Flujo de prueba: opción 1 (crear una cita) → opción 2 (`getStudentWellnessSummary`), debe mostrar la cita creada **y** las especialidades disponibles en una sola respuesta del Gateway.
 
 
---
 
## 8 — Ejercicio integrador final: Plataforma ECICIENCIA
 
*Carpeta: wellness-grpc (extensión del anterior)*
 
### Contexto
ECICIENCIA necesita una plataforma distribuida para gestionar el registro de asistentes, la agenda de actividades, la reserva de cupos en talleres y el control de aforo, durante el evento.
 
### Arquitectura propuesta
 
Siguiendo el patrón de microservicios + API Gateway ya aplicado en los puntos 6.3 y 7.3, se proponen tres servicios independientes, cada uno con su propio contrato `.proto` y puerto:
 
| Servicio | Responsabilidad | Puerto |
|---|---|---|
| `AttendeeService` | Registro de asistentes | 50071 |
| `AgendaService` | Agenda de actividades y consulta por franja horaria | 50072 |
| `WorkshopService` | Reserva de cupos en talleres y control de aforo | 50073 |
 
Un `ECICIENCIAGateway` centralizaría el acceso del cliente final, evitando que conozca los puertos y contratos individuales de cada servicio (mismo rol que `WellnessGateway` en 7.3).
 
 
### Microservicio implementado: AgendaService
 
De los tres servicios propuestos, se implementó `AgendaService` con código funcional.
 
- `src/main/proto/agenda.proto`: define el servicio `AgendaService` con:
  - `GetAgenda`: retorna todas las actividades.
  - `GetActivitiesByTimeSlot`: filtra actividades por franja horaria.
  - `GetActivity`: consulta una actividad por ID.
- `AgendaServer.java`: implementación con 4 actividades en memoria (charlas y talleres con ponente, horario y lugar).
- `AgendaClient.java`: cliente de consola con menú para las tres operaciones.
### Cómo probarlo
```bash
mvn clean compile
 
# Terminal 1
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AgendaServer"
 
# Terminal 2
mvn exec:java -Dexec.mainClass="edu.eci.arsw.wellness.AgendaClient"
```
Flujo de prueba: ver agenda completa → consultar por franja horaria `09:00-11:00` (deben aparecer dos actividades en paralelo) → consultar actividad por ID existente (`A01`) → consultar ID inexistente (`A99`, debe indicar no encontrada).
 
 
### Reflexión sobre la evolución arquitectónica
 
A lo largo del taller, el mismo problema (consultar y modificar el estado de un recurso) se resolvió con seis estilos distintos. Empezando por **sockets TCP**, donde el contrato es un acuerdo informal de cadenas de texto, cualquier cambio implica modificar cliente y servidor a mano y no hay forma de validar el formato salvo en tiempo de ejecución. **HTTP** introduce una estructura estándar (rutas, métodos, parámetros) que permite interoperar con herramientas externas (navegador, `curl`), pero sigue sin definir un esquema formal de datos.
 
**RMI** cambia el paradigma: el contrato es código (una interfaz Java), lo que da seguridad de tipos en tiempo de compilación, pero ata la solución al ecosistema Java. **gRPC** resuelve ambos problemas con un contrato `.proto` independiente del lenguaje, fuertemente tipado y que genera automáticamente el código cliente/servidor.
 
Al pasar a **microservicios**, el sistema deja de concentrar toda la lógica en un solo proceso: cada servicio (citas, especialidades médicas, agenda) es pequeño, cohesivo y se despliega/escala de forma independiente. El costo es que el cliente ahora debe conocer múltiples direcciones y contratos. El **API Gateway** resuelve justo eso, centralizando el acceso y unificando respuestas que provienen de varios servicios — al precio de convertirse en un posible punto único de fallo y, si no se controla, en un lugar donde se acumula lógica de negocio que debería vivir en los servicios.
 
No se optó por un monolito para ECICIENCIA porque las responsabilidades (asistentes, agenda, talleres) tienen ciclos de cambio y escalamiento distintos: la agenda se consulta masivamente durante todo el evento, mientras que el registro de asistentes tiene mayor carga al inicio. Separarlos permite escalar y desplegar cada uno según su propia demanda, sin afectar a los demás.
 
---
 
## Comparación general de los estilos trabajados
 
| Estilo | Contrato definido en | Puerto usado |
|---|---|---|
| Sockets TCP | Convención de texto manual | 35000 |
| HTTP | Rutas y parámetros | 8080 |
| RMI | Interfaz Java (`Remote`) | 23000 |
| gRPC | Archivo `.proto` | 50061 |
| Microservicios | Múltiples `.proto` | 50061 / 50062 |
| API Gateway | Reutiliza contratos gRPC internos | 50061 / 50062 (internos) |
| ECICIENCIA (integrador) | Múltiples `.proto` + Gateway | 50071 / 50072 / 50073 |
