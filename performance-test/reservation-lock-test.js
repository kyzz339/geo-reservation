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
            executor: 'constant-arrival-rate', // 일정한 속도로 요청을 생성하는 방식
            rate: 10,                          // 초당 요청 횟수 (목표치)
            timeUnit: '1s',                    // rate 수치의 시간 단위 (1초)
            duration: '30s',                   // 테스트 총 지속 시간
            preAllocatedVUs: 10,               // 시작 시 미리 할당할 가상 사용자 수
            maxVUs: 50,                        // 목표 rate 유지를 위해 동원 가능한 최대 가상 사용자 수
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
        reservedAt: "2026-03-17T11:00:00",
        finishedAt: "2026-03-17T12:00:00",
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
