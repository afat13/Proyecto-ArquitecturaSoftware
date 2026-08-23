import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 30),
  duration: __ENV.DURATION || '60s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const login = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: 'estudiante@aprende.local', password: 'Aprende123!' }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(login, { 'login responde 200': (r) => r.status === 200 });
  if (login.status !== 200) {
    throw new Error(`No fue posible iniciar sesión: ${login.status} ${login.body}`);
  }
  return { token: login.json('token') };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/tasks`, {
    headers: { Authorization: `Bearer ${data.token}` },
  });
  check(response, {
    'GET /api/tasks responde 200': (r) => r.status === 200,
    'la respuesta contiene tareas': (r) => Array.isArray(r.json()) && r.json().length > 0,
  });
}
