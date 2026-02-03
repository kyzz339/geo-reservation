🍽️ 매장 예약 및 위치 기반 서비스 (Demo Project)
Spring Boot를 활용한 지도 기반 매장 예약 시스템입니다. 대규모 트래픽 상황에서의 데이터 정합성과 확장성을 고려하여 설계되었습니다.

🚀 핵심 기술 스택
Backend: Java 21, Spring Boot 3.5.7

Database: PostgreSQL, PostGIS (위치 정보 처리)

ORM/Query: Spring Data JPA, Querydsl

Concurrency: Pessimistic Lock (비관적 락)

DevOps/Tools: Docker, Redis (V2 예정), Kafka (V2 예정)

🛠️ 주요 구현 기능
1. 위치 기반 매장 관리
   PostGIS의 Point 타입을 사용하여 매장의 위경도 정보를 저장하고 조회합니다.

특정 반경 내의 매장을 검색하는 기능을 제공합니다.

2. 동시성 제어를 고려한 예약 시스템 (V1)
   중복 예약 방지: 비관적 락(PESSIMISTIC_WRITE)을 활용하여 동일 시간대 및 인원 초과 예약을 DB 레벨에서 차단합니다.

Querydsl 활용: 복잡한 시간 중복 체크 쿼리를 타입 안정성이 보장된 Querydsl로 구현했습니다.

Soft Delete: 예약 취소 시 데이터를 삭제하지 않고 CANCELLED 상태로 관리하여 통계 데이터의 유실을 방지합니다.

3. 일 배치(Batch) 통계 서비스 (진행 예정)
   Spring Batch & Scheduler: 매일 자정, 전날의 예약 데이터를 집계하여 매장별 매출 및 노쇼(No-show) 통계를 생성합니다.

📈 시스템 아키텍처 (V1)
💡 개발 중점 사항
데이터 정합성: 예약 인원 합산 시 발생할 수 있는 Race Condition을 방지하기 위해 JPA 비관적 락을 도입했습니다.

확장성 고려: 현재 DB 락 기반의 시스템을 추후 Redis 분산 락 환경으로 고도화할 수 있도록 로직을 분리하여 설계했습니다.