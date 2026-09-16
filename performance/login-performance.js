/**
 * ===========================================================================
 *  Prueba de performance - Funcionalidad critica: LOGIN
 *  Herramienta: k6 (https://k6.io)
 *  Adaptada del taller de la Unidad II (qa-automation-ci) al login real de
 *  este proyecto: POST /login (formulario, no JSON), usuario "qa.tester".
 *
 *  Ejecucion local : mvn spring-boot:run  (en otra terminal)
 *                     k6 run performance/login-performance.js
 *  Ejecucion en CI  : ver job "performance" en .github/workflows/ci.yml
 * ===========================================================================
 *
 *  ¿Por que el login? Es la puerta de entrada del sistema: si se degrada,
 *  TODAS las demas funcionalidades quedan inaccesibles. Es el punto de mayor
 *  concurrencia en la hora peak.
 *
 *  INDICADORES MONITOREADOS
 *  ------------------------
 *  1. TPS / throughput (http_reqs)        -> transacciones por segundo que
 *     soporta el endpoint. Mide capacidad.
 *  2. Latencia (http_req_duration)        -> se observa el percentil 95 y 99,
 *     NO el promedio: el promedio esconde la cola de usuarios lentos.
 *  3. Tasa de errores (http_req_failed)   -> % de respuestas != 2xx/3xx.
 *     Es el indicador de estabilidad bajo carga.
 *  4. Usuarios virtuales concurrentes (vus)-> carga aplicada en cada momento.
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Metricas personalizadas: permiten graficar el login por separado del resto.
const erroresLogin = new Rate('errores_login');
const latenciaLogin = new Trend('latencia_login', true);

export const options = {
  // --- Perfil de carga escalonado (ramping) ---
  // Se sube la carga por etapas para identificar el punto de quiebre,
  // en lugar de golpear el sistema de una sola vez.
  stages: [
    { duration: '15s', target: 5 },   // rampa de subida: calentamiento
    { duration: '30s', target: 20 },  // carga nominal esperada en hora peak
    { duration: '15s', target: 40 },  // carga de estres: 2x lo esperado
    { duration: '15s', target: 0 },   // rampa de bajada: verifica recuperacion
  ],

  // --- Umbrales (SLO): si no se cumplen, k6 devuelve exit code 99
  //     y el pipeline marca el paso como fallido (con continue-on-error en
  //     CI, para que una degradacion alerte sin bloquear el merge).
  thresholds: {
    'http_req_duration': ['p(95)<800', 'p(99)<1500'], // 95% bajo 800 ms
    'http_req_failed':   ['rate<0.01'],               // menos de 1% de errores
    'errores_login':     ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const payload = { usuario: 'qa.tester', clave: 'Test1234' };

  const params = {
    tags: { funcionalidad: 'login' }, // etiqueta para filtrar en el dashboard
    redirects: 5,                     // sigue el redirect a /inventario
  };

  // El formulario de login.html envia x-www-form-urlencoded, no JSON.
  const res = http.post(`${BASE_URL}/login`, payload, params);

  // Validaciones funcionales dentro de la prueba de carga: un servicio que
  // responde rapido pero con error no esta "sano".
  const ok = check(res, {
    'login exitoso (200, ya en /inventario)': (r) => r.status === 200,
    'responde en menos de 800 ms': (r) => r.timings.duration < 800,
    'la pagina de inventario cargo': (r) => r.body && r.body.includes('Inventario'),
  });

  erroresLogin.add(!ok);
  latenciaLogin.add(res.timings.duration);

  sleep(1); // think time: simula el tiempo real entre acciones de un usuario
}

/**
 * Genera un resumen navegable al final de la ejecucion.
 * El HTML se publica como artefacto del pipeline junto a los reportes
 * funcionales, y el JSON alimenta el dashboard de metricas.
 */
export function handleSummary(data) {
  return {
    'performance/resultados/summary.json': JSON.stringify(data, null, 2),
    stdout: textSummary(data),
  };
}

function textSummary(data) {
  const m = data.metrics;
  const val = (metrica, campo) => (m[metrica] && m[metrica].values[campo] != null)
      ? m[metrica].values[campo].toFixed(2) : 'n/d';
  return `
===========================================
 RESUMEN DE PERFORMANCE - LOGIN
===========================================
 Throughput (TPS)      : ${val('http_reqs', 'rate')} req/s
 Latencia promedio     : ${val('http_req_duration', 'avg')} ms
 Latencia p(95)        : ${val('http_req_duration', 'p(95)')} ms
 Latencia p(99)        : ${val('http_req_duration', 'p(99)')} ms
 Tasa de error         : ${val('http_req_failed', 'rate')}
 Iteraciones totales   : ${val('iterations', 'count')}
===========================================
`;
}
