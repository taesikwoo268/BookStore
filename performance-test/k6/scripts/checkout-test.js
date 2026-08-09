// checkout-test.js
// Đặt tại: performance-test/k6/scripts/checkout-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// ============================================================
// CONFIG
// ============================================================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BOOK_ID = __ENV.BOOK_ID || '1';

// ============================================================
// METRICS
// ============================================================
const successRate = new Rate('success_rate');
const orderSuccess = new Counter('order_success');
const orderFailed = new Counter('order_failed');
const checkoutDuration = new Trend('checkout_duration');

// ============================================================
// TEST OPTIONS
// ============================================================
export const options = {
    // Test 1: Smoke test (1 user)
    // stages: [
    //     { duration: '10s', target: 1 },
    // ],

    // Test 2: Load test (100 users)
    stages: [
        { duration: '10s', target: 10 },
        { duration: '20s', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '30s', target: 100 },
        { duration: '10s', target: 0 },
    ],

    thresholds: {
        http_req_duration: ['p(95)<2000'],
        success_rate: ['rate>0.01'],
        order_success: ['count>0'],
    },
};

// ============================================================
// HELPER FUNCTIONS
// ============================================================
function login(username, password) {
    const payload = JSON.stringify({ username, password });
    const res = http.post(`${BASE_URL}/api/v1/auth/login`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    if (res.status !== 200) return null;
    const body = JSON.parse(res.body);
    return body?.data?.accessToken;
}

function register(username, password, email, fullName) {
    const payload = JSON.stringify({ username, password, email, fullName });
    return http.post(`${BASE_URL}/api/v1/auth/register`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });
}

function addToCart(token, bookId, quantity) {
    const payload = JSON.stringify({ bookId, quantity });
    return http.post(`${BASE_URL}/api/v1/cart/items`, payload, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    });
}

function checkout(token, address) {
    const payload = JSON.stringify({
        shippingAddress: address,
        billingAddress: address,
        paymentMethod: 'CREDIT_CARD',
        notes: `Order from k6 test at ${new Date().toISOString()}`,
    });

    const startTime = Date.now();
    const res = http.post(`${BASE_URL}/api/v1/checkout`, payload, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
        },
    });
    checkoutDuration.add(Date.now() - startTime);

    return res;
}

// ============================================================
// MAIN TEST
// ============================================================
export default function () {
    const userId = __VU;
    const timestamp = Date.now();
    const username = `test_user_${timestamp}_${userId}`;
    const email = `test_${timestamp}_${userId}@test.com`;
    const password = 'Test@123456';
    const fullName = `Test User ${userId}`;

    // 1. Register
    const registerRes = register(username, password, email, fullName);
    if (registerRes.status !== 200 && registerRes.status !== 201) return;

    // 2. Login
    const token = login(username, password);
    if (!token) return;

    // 3. Add to cart
    const cartRes = addToCart(token, BOOK_ID, 1);
    if (cartRes.status !== 200 && cartRes.status !== 201) return;

    // 4. Checkout
    const checkoutRes = checkout(token, `Address ${userId}, Test City`);
    const isSuccess = checkoutRes.status === 200 || checkoutRes.status === 201;

    successRate.add(isSuccess);
    if (isSuccess) {
        orderSuccess.add(1);
        console.log(`✅ [${userId}] Checkout SUCCESS`);
    } else {
        orderFailed.add(1);
        console.log(`❌ [${userId}] Checkout FAILED: ${checkoutRes.status}`);
    }

    sleep(0.5);
}