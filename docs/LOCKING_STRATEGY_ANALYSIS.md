# 예약 시스템 동시성 제어 테스트 결과 보고서 (수정본)

본 문서는 `k6`를 이용한 예약 시스템의 비관적 락(Pessimistic Lock)과 낙관적 락(Optimistic Lock) 테스트 결과를 분석하고, 실제 매커니즘 작동 및 롤백 발생 원인과 개선 방안을 정리합니다.

## 1. 테스트 결과 요약

### A. 비관적 락 (Pessimistic Lock) 테스트
- **성공률**: 0.35% (1 / 290 성공)
- **실패율**: 99.65% (HTTP 500)
- **원인 분석**: 모든 요청이 동일한 `Store` 엔티티에 대해 `PESSIMISTIC_WRITE` 락을 획득하려고 대기하면서 병목이 발생했습니다. 락 획득 타임아웃이나 커넥션 풀 고갈로 인해 대부분 500 에러가 발생했습니다.

### B. 낙관적 락 (Optimistic Lock) 테스트
- **성공률**: 0.33% (1 / 300 성공)
- **실패율**: 99.67% (HTTP 500)
- **분석 내용**:
    - **실제 낙관적 락 작동**: 테스트 당시 실제 낙관적 락 매커니즘이 활성화된 상태였습니다.
    - **롤백 발생**: 다수의 요청이 동일한 엔티티의 버전을 동시에 수정하려 시도함에 따라 `ObjectOptimisticLockingFailureException`이 발생했습니다.
    - **500 에러 발생 원인**: 스프링 트랜잭션 내에서 예외가 발생하여 트랜잭션이 **롤백(Rollback)** 되었으나, 이 예외를 가로채어 적절한 응답(예: 409 Conflict)으로 변환하는 처리기가 없어 최종적으로 클라이언트에 500 Internal Server Error로 전달되었습니다.

---

## 2. 문제 해결을 위한 수정 가이드

### ① 전역 예외 처리기 (GlobalExceptionHandler) 보완
락 충돌로 인한 예외 발생 시 500 에러가 아닌 사용자 친화적인 응답을 주어야 합니다.

```java
@ExceptionHandler({PessimisticLockingFailureException.class, ObjectOptimisticLockingFailureException.class})
public ResponseEntity<ErrorResponse> handleLockingFailure(Exception e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
            .status(409)
            .code("LOCK_FAILURE")
            .message("동시 요청으로 인해 처리에 실패했습니다. 잠시 후 다시 시도해주세요.")
            .build());
}
```

### ② 낙관적 락을 위한 재시도 로직 도입
낙관적 락은 충돌이 발생했을 때 재시도(Retry)를 하지 않으면 성공률이 매우 낮습니다. `spring-retry`를 사용하거나 직접 재시도 로직을 구현해야 합니다.

```java
@Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
@Transactional
public CreateReservationResponse createReservation(...) { ... }
```

### ③ 락 범위 최적화 (비관적 락 개선)
- **락 타임아웃**: `PESSIMISTIC_WRITE` 사용 시 무한정 대기하지 않도록 힌트를 추가합니다.
- **분산 락 (Redisson)**: DB 락의 부하를 줄이기 위해 프로젝트에 포함된 Redis 기반 분산 락을 사용하는 것이 성능상 가장 유리합니다.

---

## 3. 요약 및 필요 작업
1. **[필수]** `@Version` 필드가 누락되어 있다면 `Store` 엔티티에 추가 (테스트 시에만 넣었다면 정식 반영 필요).
2. **[필수]** `GlobalExceptionHandler`에 락 관련 예외 처리 추가 (500 에러 방지).
3. **[권장]** 충돌 발생 시 자동 재시도를 위한 `@Retryable` 또는 수동 재시도 로직 구현.
4. **[성능]** 더 높은 동시 처리를 원한다면 Redis 분산 락(Redisson) 도입 검토.
