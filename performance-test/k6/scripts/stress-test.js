// stress-test.js
// Đặt tại: performance-test/k6/scripts/stress-test.js
// Mục đích: Stress test với tăng dần tải

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const failureRate = new Rate('failure_rate');

export const options = {
    stages: [
        { duration: '1m', target: 20 },   // Chỉ lên tới 50 thay vì 200
        { duration: '2m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        failure_rate: ['rate<0.1'],
        http_req_duration: ['p(95)<3000'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/v1/books/public`);
    const success = check(res, { 'status is 200': (r) => r.status === 200 });
    failureRate.add(!success);
    sleep(Math.random() * 1);
}