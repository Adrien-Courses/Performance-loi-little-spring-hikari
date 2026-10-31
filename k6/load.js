import http from 'k6/http';

// Modele OUVERT : k6 injecte RATE requetes/seconde quoi qu'il arrive,
// comme de vrais utilisateurs. C'est indispensable pour voir la file grossir.
const RATE = Number(__ENV.RATE || 2000);

export const options = {
  scenarios: {
    load: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: __ENV.DURATION || '10m',
      preAllocatedVUs: 300,
      maxVUs: 3000,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

const DB = __ENV.DB || 5;
const APP = __ENV.APP || 35;

export default function () {
  http.get(`http://localhost:8080/work?dbMs=${DB}&appMs=${APP}`, { timeout: '60s' });
}
