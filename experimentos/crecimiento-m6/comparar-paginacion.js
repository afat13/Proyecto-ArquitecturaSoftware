import http from 'k6/http';
import { check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

const getDuration = new Trend('tasks_get_duration_ms', true);
const getStatus = new Counter('tasks_get_status');
const BASE_URL = __ENV.BASE_URL || 'http://api:8080';
const PASSWORD = __ENV.TEST_PASSWORD || 'Aprende123!';
const MODE = __ENV.TASKS_MODE || 'paged';

if (!['paged', 'legacy'].includes(MODE)) throw new Error('TASKS_MODE debe ser paged o legacy');

export const options = {
  vus: Number(__ENV.VUS || 30),
  duration: __ENV.DURATION || '60s',
  summaryTrendStats: ['avg', 'min', 'med', 'p(50)', 'p(95)', 'max'],
  thresholds: { tasks_get_duration_ms: ['p(95)<10000'] }
};

export function setup() {
  const vus = Number(__ENV.VUS || 30);
  if (!Number.isInteger(vus) || vus < 1 || vus > 5000) throw new Error('VUS fuera de rango');
  const tokens = [];
  for (let n = 1; n <= vus; n++) {
    const email = `carga${String(n).padStart(4, '0')}@aprende.local`;
    const r = http.post(`${BASE_URL}/api/auth/login`,
      JSON.stringify({ email, password: PASSWORD }),
      { headers: { 'Content-Type': 'application/json' }, tags: { operation: 'login' } });
    if (r.status !== 200 || !r.json('token')) throw new Error(`Login inválido: ${n}, HTTP ${r.status}`);
    tokens.push(r.json('token'));
  }
  return { tokens };
}

export default function(data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const path = MODE === 'paged' ? '/api/tasks/page?page=0&size=50' : '/api/tasks';
  const response = http.get(BASE_URL + path, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { operation: MODE === 'paged' ? 'tasks_page' : 'tasks_legacy' }
  });
  // Esta submétrica registra exclusivamente la petición GET, sin incluir login.
  getDuration.add(response.timings.duration, { mode: MODE });
  getStatus.add(1, { mode: MODE, status: String(response.status) });
  check(response, {
    'HTTP 200': r => r.status === 200,
    'forma y cantidad esperada': r => {
      if (r.status !== 200) return false;
      try {
        const body = r.json();
        return MODE === 'paged'
          ? Array.isArray(body.items) && body.items.length === 50 && body.page === 0 && body.size === 50
          : Array.isArray(body) && body.length === 1000;
      } catch (_) { return false; }
    }
  });
}
