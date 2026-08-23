import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 30),
  duration: __ENV.DURATION || '60s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    'http_req_duration{operacion:consulta_tareas}': ['p(95)<2000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const login = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: 'estudiante@aprende.local', password: 'Aprende123!' }),
    {
      headers: { 'Content-Type': 'application/json' },
      tags: { operacion: 'inicio_sesion' },
    },
  );

  const loginValido = check(login, {
    'login responde 200': (r) => r.status === 200,
    'login devuelve token': (r) => Boolean(r.json('token')),
  });

  if (!loginValido) {
    throw new Error(`No fue posible iniciar sesión: ${login.status} ${login.body}`);
  }

  return { token: login.json('token') };
}

export default function (data) {
  const response = http.get(`${BASE_URL}/api/tasks`, {
    headers: { Authorization: `Bearer ${data.token}` },
    tags: { operacion: 'consulta_tareas' },
  });

  check(response, {
    'GET /api/tasks responde 200': (r) => r.status === 200,
    'la respuesta contiene tareas': (r) => {
      if (r.status !== 200) return false;
      try {
        const body = r.json();
        return Array.isArray(body) && body.length > 0;
      } catch (_) {
        return false;
      }
    },
  });
}
