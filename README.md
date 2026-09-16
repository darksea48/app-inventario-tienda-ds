# app-inventario-tienda

Aplicación Java (Spring Boot) que gestiona el inventario de una tienda,
usada como caso de estudio del Examen Final de Automatización de Pruebas
(ADP1323). Implementa control de versiones con Trunk-Based Development,
un pipeline de CI con pruebas unitarias, de integración y análisis de
seguridad estático (SAST/SCA), y un pipeline de CD con acceptance tests
en BDD, análisis de seguridad dinámico (DAST), despliegue Blue-Green y
rollback automático con registro de auditoría.

> Examen Final · Automatización de Pruebas (ADP1323) · Douglas Suárez Z.
> El detalle completo (diagramas, justificación de cada decisión, mapeo
> a la pauta de evaluación) está en el informe `Douglas_Suarez.docx`.

## Estrategia de pruebas

| Nivel | Herramienta | Alcance |
|---|---|---|
| Unitaria | JUnit 5 (Surefire) | Reglas de negocio de `InventarioService`, aisladas. |
| Integración | Selenium + Failsafe | Flujo completo login → inventario, contra la app real. |
| Aceptación (BDD) | Cucumber + Gherkin | Los mismos criterios, en lenguaje de negocio, contra la versión candidata en CD. |
| Seguridad estática | CodeQL + OWASP Dependency-Check | Código y dependencias, antes de fusionar a `master`. |
| Seguridad dinámica | OWASP ZAP | La aplicación ya desplegada, antes de recibir tráfico real. |

## Cómo ejecutar

```bash
# Pruebas unitarias
mvn test

# Pruebas unitarias + de integración (igual que en CI) -- requiere Chrome/Chromium instalado
mvn verify

# Levantar la app localmente
mvn spring-boot:run   # http://localhost:8080/login  (usuario: qa.tester / clave: Test1234)
```

## Despliegue local Blue-Green (requiere Docker)

```bash
docker compose -f deploy/docker-compose.yml up -d --build
# La app queda disponible en http://localhost:8080 (a través del proxy),
# con BLUE activo por defecto, además de http://localhost:8081 (blue) y
# http://localhost:8082 (green) directos, para comparar.

# Conmutar el tráfico hacia GREEN (tras validarlo)
./deploy/blue-green-switch.sh --to green

# Revertir (rollback) al color anterior
./deploy/blue-green-switch.sh --rollback

# Correr los acceptance tests BDD contra un color específico
mvn test -Dtest=BddAcceptanceRunner -Dacceptance.target.url=http://localhost:8082
```

## Prueba de performance (k6)

```bash
# En una terminal: levantar la app
mvn spring-boot:run

# En otra terminal: instalar k6 (https://k6.io/docs/get-started/installation/)
# y correr la prueba de carga sobre el login
k6 run performance/login-performance.js
```

## Pipelines

Este proyecto integra el trabajo de las tres unidades del curso: control de
versiones y pruebas unitarias atómicas (Unidad I), un pipeline de CI con
pruebas de integración, análisis de seguridad, performance, dashboard y
alertas (Unidad II), y un pipeline de CD con Blue-Green, DAST y rollback
(Unidad III).

- `.github/workflows/ci.yml`: se dispara en cada push/PR. Corre en 4 jobs:
  - `build-and-test`: compila, corre pruebas unitarias y de integración,
    analiza dependencias (SCA) y publica reportes; avisa por Slack si falla.
  - `sast`: análisis estático de código con CodeQL.
  - `performance`: levanta la app y corre una prueba de carga con k6 sobre
    el login (perfil escalonado, umbrales de latencia/errores).
  - `reportes-y-alertas`: descarga todos los reportes (unitarias,
    integración, performance) y los publica como dashboard navegable en
    GitHub Pages; si `build-and-test` falla en `master`, abre un issue
    automático.
- `.github/workflows/cd.yml`: se dispara cuando el CI de `master` termina
  en verde. Despliega al color inactivo, corre acceptance tests BDD y un
  escaneo DAST, conmuta el tráfico, monitorea la salud del despliegue,
  hace rollback automático si algo falla, y deja registro en el log de
  auditoría. **Importante:** requiere un self-hosted runner para que el
  estado de Docker persista entre ejecuciones (ver el comentario al inicio
  de `cd.yml`).

## Estructura del proyecto

```
app-inventario-tienda/
├── .github/workflows/          # ci.yml, cd.yml
├── deploy/                     # docker-compose, script de conmutación, monitoreo
├── docs/                       # capturas de evidencia, log de auditoría
├── performance/                # prueba de carga (k6) sobre el login
├── src/main/java/...           # aplicación (dominio + controladores)
├── src/main/resources/         # vistas Thymeleaf, configuración por entorno
├── src/test/java/.../unit/         # pruebas unitarias (JUnit 5)
├── src/test/java/.../integration/  # pruebas de integración (Selenium)
├── src/test/java/.../acceptance/   # pruebas de aceptación (Cucumber/Gherkin)
├── Dockerfile
└── pom.xml
```

## Requisitos

- JDK 17+
- Apache Maven 3.8+
- Git 2.30+
- Chrome/Chromium (para las pruebas de integración y aceptación con Selenium)
- Docker + Docker Compose (para el entorno de pruebas Blue-Green)

## Evidencias

Capturas de ejecución en `docs/capturas/`. Registro de despliegues en
`docs/auditoria-despliegues.csv`. Ver el informe del examen
(`Douglas_Suarez.docx`) para el detalle de cada una y los diagramas.
