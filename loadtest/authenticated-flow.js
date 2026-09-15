// loadtest/authenticated-flow.js
import http from 'k6/http';
import { check, sleep, group } from 'k6';

const BASE_URL = 'https://d1ll4f9lg8cczv.cloudfront.net';
const SESSION_COOKIE = __ENV.SESSION_COOKIE;

export const options = {
  stages: [
    { duration: '20s', target: 100 },
    { duration: '40s', target: 100 },
    { duration: '20s', target: 300 },
    { duration: '40s', target: 300 },
    { duration: '20s', target: 500 },
    { duration: '40s', target: 500 },
    { duration: '20s', target: 800 },
    { duration: '40s', target: 800 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],
    http_req_failed: ['rate<0.10'],
  },
};

const params = {
  headers: {
    Cookie: SESSION_COOKIE,
  },
};

export default function () {
  group('GET /api/skills', function () {
    const res = http.get(`${BASE_URL}/api/skills`, params);
    check(res, {
      'skills status is 200': (r) => r.status === 200,
    });
  });

  sleep(0.3);

  group('GET /api/study-logs', function () {
    const res = http.get(`${BASE_URL}/api/study-logs`, params);
    check(res, {
      'study-logs status is 200': (r) => r.status === 200,
    });
  });

  sleep(0.5);
}