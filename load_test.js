import http from 'k6/http';
import { check, sleep } from 'k6';

// Run with: k6 run load_test.js
// Simulating 5,000 virtual users over a 30 second period
export const options = {
    stages: [
        { duration: '10s', target: 1000 }, // Ramp up to 1,000 users over 10 seconds
        { duration: '10s', target: 5000 }, // Spike to 5,000 users over the next 10 seconds
        { duration: '10s', target: 0 },    // Ramp down to 0 users
    ],
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    // Step 1: Login to get the JWT token
    const loginPayload = JSON.stringify({
        username: 'testuser',
        password: 'password123',
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);

    // Some users might fail to login if rate-limited on the login endpoint itself, so we check if login was successful
    if (loginRes.status === 200) {
        const token = loginRes.json('token');

        // Step 2: Access the protected profile endpoint
        const apiParams = {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        };

        const apiRes = http.get(`${BASE_URL}/api/user/me`, apiParams);

        // We expect either a 200 OK or a 429 Too Many Requests (Rate Limit triggered)
        check(apiRes, {
            'is status 200 (Success)': (r) => r.status === 200,
            'is status 429 (Rate Limited)': (r) => r.status === 429,
        });
    }

    // Small sleep to simulate realistic user think time
    sleep(1);
}
