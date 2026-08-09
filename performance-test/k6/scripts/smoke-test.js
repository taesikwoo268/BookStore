// smoke-test.js
// Đặt tại: performance-test/k6/scripts/smoke-test.js
// Mục đích: Kiểm tra nhanh hệ thống với 1 user

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    vus: 1,
    duration: '10s',
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const username = `smoke_test_${Date.now()}`;
    const password = 'Test@123456';
    const email = `${username}@test.com`;

    // 1. Register
    const regRes = http.post(`${BASE_URL}/api/v1/auth/register`, JSON.stringify({
        username, password, email, fullName: 'Smoke Test User',
    }), { headers: { 'Content-Type': 'application/json' } });

    check(regRes, { 'Register status is 200': (r) => r.status === 200 });

    // 2. Login
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        username, password,
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, { 'Login status is 200': (r) => r.status === 200 });
    const token = JSON.parse(loginRes.body)?.data?.accessToken;

    // 3. Get books
    const booksRes = http.get(`${BASE_URL}/api/v1/books`, {
        headers: { 'Authorization': `Bearer ${token}` },
    });
    check(booksRes, { 'Get books status is 200': (r) => r.status === 200 });

    sleep(1);
}