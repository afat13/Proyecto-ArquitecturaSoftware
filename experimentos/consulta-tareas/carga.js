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
const PASSWORD = __ENV.TEST_PASSWORD || 'Aprende123!';

function emailFor(index) {
  return `carga${String(index).padStart(4, '0')}@aprende.local`;
}

export function setup() {
  const vus = Number(__ENV.VUS || 30);
  if (vus < 1 || vus > 5000) {
    throw new Error(`VUS debe estar entre 1 y 5000; recibido: ${vus}`);
  }

  const tokens = [];

  for (let i = 1; i <= vus; i += 1) {
    const email = emailFor(i);
    const login = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email, password: PASSWORD }),
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
      throw new Error(`No fue posible iniciar sesión con ${email}: ${login.status} ${login.body}`);
    }

    tokens.push(login.json('token'));
  }

  return { tokens };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const response = http.get(`${BASE_URL}/api/tasks`, {
    headers: { Authorization: `Bearer ${token}` },
    tags: { operacion: 'consulta_tareas' },
  });

  check(response, {
    'GET /api/tasks responde 200': (r) => r.status === 200,
    'la respuesta contiene exactamente 1000 tareas': (r) => {
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
