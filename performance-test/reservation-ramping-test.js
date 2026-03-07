import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * k6 Load Test Script: Store List Search (Near Seongsu Station)
 * 점진적으로 사용자(VU)를 늘려가며 서버의 한계를 측정합니다.
 */

export const options = {
    stages: [
        { duration: '10s', target: 20 }, // 10초 동안 0~20명 증가
        { duration: '20s', target: 50 }, // 20초 동안 20~50명 유지/증가
        { duration: '10s', target: 0 },  // 10초 동안 0명으로 감소
    ],
};

// 테스트 설정값
const BASE_URL = 'http://172.30.1.200';
const USER_EMAIL = 'test@test.com';
const USER_PASSWORD = '1234';

export function setup() {
    const loginRes = http.post(`${BASE_URL}/auth/signin`, JSON.stringify({
        email: USER_EMAIL,
        password: USER_PASSWORD,
    }), {
        headers: { 'Content-Type': 'application/json' },
    });

    if (loginRes.status !== 200) {
        console.error('Login failed!');
        return { token: null };
    }

    return { token: loginRes.json('accessToken') };
}

export default function (data) {
    if (!data.token) return;

    // 성수역 좌표 정보
    const SEONGSU_LAT = 37.5446;
    const SEONGSU_LON = 127.0559;

    // 매장 목록 조회 URL (쿼리 파라미터 방식)
    const url = `${BASE_URL}/store/storeList?latitude=${SEONGSU_LAT}&longitude=${SEONGSU_LON}&distance=1.0`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`,
        },
    };

    // GET 요청으로 매장 목록 조회
    const res = http.get(url, params);

    // 성공한 응답 값을 샘플로 한 번만 보고 싶을 때 (첫 번째 VU의 첫 번째 요청만 출력)
    if (res.status === 200 && __ITER === 0 && __VU === 1) {
        console.log("--- Store List Sample Response ---");
        console.log(JSON.stringify(res.json(), null, 2)); // JSON을 예쁘게 출력
        console.log("----------------------------------");
    }

    // 에러 발생 시 로그 출력
    if (res.status !== 200) {
        console.log(`[Error] VU:${__VU} Status:${res.status} Body:${res.body}`);
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
        'has response body': (r) => r && r.body && r.body.length > 0,
    });

    sleep(1);
}
