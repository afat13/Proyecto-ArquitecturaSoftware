import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const statusCount = new Counter('tasks_http_status');

export const options = {
  vus: Number(__ENV.VUS || 30),
  duration: __ENV.DURATION || '60s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'max'],
  thresholds: {
    'tasks_http_status{codigo:200}': ['count>=0'],
    'tasks_http_status{codigo:400}': ['count>=0'],
    'tasks_http_status{codigo:401}': ['count>=0'],
    'tasks_http_status{codigo:403}': ['count>=0'],
    'tasks_http_status{codigo:404}': ['count>=0'],
    'tasks_http_status{codigo:429}': ['count>=0'],
    'tasks_http_status{codigo:500}': ['count>=0'],
    'tasks_http_status{codigo:502}': ['count>=0'],
    'tasks_http_status{codigo:503}': ['count>=0'],
    'tasks_http_status{codigo:504}': ['count>=0'],
  },
};

const URL = __ENV.BASE_URL || 'http://api:8080';
const PASSWORD = __ENV.TEST_PASSWORD || 'Aprende123!';

export function setup() {
  const vus = Number(__ENV.VUS || 30);
  if (!Number.isInteger(vus) || vus < 1 || vus > 5000) {
    throw new Error('Cantidad de VUs fuera del rango de la semilla');
  }
  const tokens = [];
  for (let n = 1; n <= vus; n += 1) {
    const email = `carga${String(n).padStart(4, '0')}@aprende.local`;
    const response = http.post(`${URL}/api/auth/login`,
      JSON.stringify({ email, password: PASSWORD }),
      { headers: { 'Content-Type': 'application/json' }, tags: { operacion: 'login' } });
    if (response.status !== 200 || !response.json('token')) {
      throw new Error(`Login falló para cuenta ${n}: HTTP ${response.status}`);
    }
    tokens.push(response.json('token'));
  }
  return { tokens };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const response = http.get(`${URL}/api/tasks`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { operacion: 'consulta_tareas' },
  });
  statusCount.add(1, { codigo: String(response.status) });
  check(response, {
    'respuesta 200': r => r.status === 200,
    'arreglo de 1000 tareas': r => {
      if (r.status !== 200) return false;
      try {
        const body = r.json();
        return Array.isArray(body) && body.length === 1000;
      } catch (_) {
        return false;
      }
    },
  });
}
