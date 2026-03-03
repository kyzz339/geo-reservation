import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * k6 Load Test Script for Pessimistic Lock Evaluation
 * Endpoint: POST /reservation
 * Logic: ReservationService.createReservation uses OPTIMISTIC lock on Store
 */

export const options = {
    scenarios: {
        constant_request_rate: {
            executor: 'constant-arrival-rate',
            rate: 10, // 1초당 10개의 요청 시도
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 10,
            maxVUs: 50,
        },
    },
};

// 테스트 설정값
const BASE_URL = 'http://localhost:8080';
const USER_EMAIL = 'test@test.com'; // 테스트 전 가입되어 있어야 함
const USER_PASSWORD = '1234';
const TARGET_STORE_ID = 1; // 락을 테스트할 매장 ID

export function setup() {
    // 1. 로그인을 통해 JWT 토큰 획득
    const loginRes = http.post(`${BASE_URL}/auth/signin`, JSON.stringify({
        email: USER_EMAIL,
        password: USER_PASSWORD,
    }), {
        headers: { 'Content-Type': 'application/json' },
    });

    if (loginRes.status !== 200) {
        console.error('Login failed! Check if user exists or BASE_URL is correct.');
        return { token: null };
    }

    const token = loginRes.json('accessToken');
    return { token: token };
}

export default function (data) {
    if (!data.token) return;

    const url = `${BASE_URL}/reservation`;
    
    // 예약 요청 데이터 (매번 다른 시간으로 요청하거나 고정하여 충돌 유도)
    const payload = JSON.stringify({
        storeId: TARGET_STORE_ID,
        visitorCount: 1,
        reservedAt: "2026-03-10T14:00:00",
        finishedAt: "2026-03-10T15:00:00",
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`,
        },
    };

    const res = http.post(url, payload, params);

    // 200: 성공, 400: 인원 초과(Capacity Exceeded), 500: 락 타임아웃 등
    check(res, {
        'status is 200': (r) => r.status === 200,
        'status is not 500': (r) => r.status !== 500,
    });

    sleep(0.1); // 요청 간 짧은 대기
}

export function teardown(data) {
    // 테스트 종료 후 처리 (필요시)
}
