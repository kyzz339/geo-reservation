# Reservation Map: 위치 기반 실시간 매장 예약 시스템

본 프로젝트는 위치 기반 서비스를 활용하여 사용자가 근처 매장을 탐색하고 실시간으로 예약할 수 있는 서비스입니다. 멀티 모듈 아키텍처와 최신 보안 표준을 준수하여 설계되었습니다.

## 🚀 주요 기능 (Key Features)

### 🏪 매장 관리 (Owner Flow)
- **지도 기반 등록**: 지도를 클릭하여 위치 좌표 및 주소를 자동 추출하고 매장을 등록합니다.
- **영업 시간 설정**: 매장별 운영 시간을 관리하며, 해당 시간 외의 예약은 자동으로 차단됩니다.
- **실시간 예약 대시보드**: 매장에 접수된 모든 예약 현황을 한눈에 파악할 수 있습니다.

### 📅 예약 시스템 (User Flow)
- **주변 매장 탐색**: 내 위치 기준 반경 내 매장을 탐색하고 상세 정보를 확인합니다.
- **스마트 예약**: 인원수와 예약 시간을 선택하여 실시간으로 매장을 예약합니다.
- **예약 이력 관리**: 본인의 예약 목록을 확인하고 간편하게 취소할 수 있습니다.

### 🔐 보안 (Security)
- **하이브리드 인증**: JWT를 헤더와 쿠키 양방향에서 지원하여 API와 SSR 환경 모두에서 강력한 보안을 유지합니다.
- **자동 리다이렉트**: 권한이 없는 페이지 접근 시 사용자의 권한에 맞춰 메인 페이지 등으로 자동 유도합니다.

## 🛠 Tech Stack
- **Framework**: Spring Boot 3.5.7, Spring Data JPA
- **Language**: Java 21
- **Database**: Spring Data JPA, Querydsl, JTS (Spatial Data)
- **Security**: Spring Security, JWT (Json Web Token)
- **Frontend**: Thymeleaf, Leaflet.js, Bootstrap 5
- **Build Tool**: Gradle (Multi-module)

## 🛡️ 동시성 제어 및 성능 분석 (Concurrency & Performance)

본 프로젝트는 예약 시스템의 핵심인 **데이터 정합성**을 보장하기 위해 동시성 제어 전략을 단계별로 검증하고 있습니다.

### 📊 k6 부하 테스트 결과 (비관적 락 vs 낙관적 락)
| 지표 | 비관적 락 (Pessimistic) | 낙관적 락 (Optimistic) |
| :--- | :---: | :---: |
| **성공률** | **0.35%** (1 / 290) | **0.33%** (1 / 300) |
| **주요 에러** | HTTP 500 (Lock Timeout) | HTTP 500 (Optimistic Lock Failure) |
| **상태** | **정합성 보장, 가용성 낮음** | **정합성 보장, 재시도 필요** |

- **분석**: DB 레벨의 비관적 락은 데이터 무결성을 완벽히 지키지만, 대규모 트래픽 환경에서 DB 커넥션 풀 고갈 및 락 대기 시간 초과로 인한 병목 현상이 확인되었습니다.
- **개선**: `GlobalExceptionHandler`를 통해 락 충돌 시 사용자에게 `409 Conflict` 응답을 제공하도록 고도화하였으며, 향후 **Redis 분산 락**으로의 전환을 계획 중입니다.

## 🚀 프로젝트 로드맵 (Future Roadmap)

단순 기능을 넘어 대규모 트래픽을 견디는 **Enterprise Level** 시스템으로 고도화 중입니다.

### 1. 운영 및 안정성 (Reliability)
- [ ] **Redisson 분산 락 도입**: DB 커넥션 점유 시간을 단축하여 전체 시스템 처리량 개선.
- [ ] **Transactional Outbox 패턴**: 예약 저장과 Kafka 메시지 발행의 원자성 보장.
- [ ] **Circuit Breaker (Resilience4j)**: 알림 서비스 장애 시에도 예약 로직은 유지되는 장애 전파 차단.

### 2. 성능 및 확장성 (Scalability)
- [ ] **Redis Geo 검색**: 위경도 기반 "내 주변 5km 이내 맛집" 초고속 검색 API 구현.
- [ ] **Redis Sorted Set 기반 랭킹**: 실시간 예약 및 인기 매장 TOP 10 실시간 집계.
- [ ] **S3 Presigned URL 업로드**: 서버 리소스 점유 없이 클라이언트가 직접 사진 업로드.

### 3. 이벤트 기반 아키텍처 (EDA)
- [ ] **Kafka 기반 비동기 알림**: 예약 성공 시 알림톡/이메일 발송 로직 분리.
- [ ] **CQRS 기반 통계 시스템**: 예약 이벤트를 구독하여 별도의 조회전용 통계 DB 관리.

### 4. 관측 가능성 (Observability)
- [ ] **Prometheus + Grafana 연동**: 락 대기 시간, 예약 성공률, API TPS 실시간 모니터링 대시보드 구축.

## 🏗 아키텍처 상세
프로젝트의 세부 설계 및 기술적 강점은 [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)와 [Database ERD](./docs/ERD.md)에서 확인하실 수 있습니다.

## 🏃 실행 방법 (Getting Started)

1. **Prerequisites**
   - Java 17 이상
   - Redis (설정된 환경에 따라 필요)

2. **Clone & Build**
   ```bash
   git clone [repository-url]
   ./gradlew clean build
   ```

3. **Run**
   ```bash
   java -jar web/build/libs/web-0.0.1-SNAPSHOT.jar
   ```

## 🧪 테스트 전략 (Testing Strategy)

본 프로젝트는 서비스의 신뢰성과 유지보수성을 위해 **계층별 테스트 전략**을 수립하고 실천합니다.

### 1. 단위 테스트 (Unit Testing with Mockito)
- **Tool**: JUnit 5, Mockito
- **목적**: 외부 의존성(DB, Redis) 없이 비즈니스 로직의 순수성을 고속으로 검증합니다.
- **핵심 기법**:
    - `@Mock`과 `when(...).thenReturn(...)`을 활용하여 가짜 객체의 동작을 정의하고 서비스 레이어의 로직에만 집중합니다.
    - `@InjectMocks`를 통해 의존성을 주입하고, JUnit 5의 `Assertions`와 `AssertJ`를 사용하여 결과의 정확성을 판정합니다.

### 2. 통합 테스트 (Integration Testing with Testcontainers)
- **Tool**: Testcontainers (Docker), Spring Boot Test
- **인프라**: **PostGIS (PostgreSQL)**, **Redis**
- **특징**:
    - 실제 운영 환경과 동일한 **PostGIS** 이미지를 컨테이너로 실행하여 공간 데이터(Geometry) 연산의 정확성을 실제 DB 레벨에서 검증합니다.
    - **Redis** 컨테이너를 연동하여 실시간 캐싱 및 동시성 제어 로직의 정합성을 실제 인프라 환경에서 확인합니다.
    - `@SpringBootTest`와 `@ActiveProfiles("test")`를 활용하여 전체 애플리케이션 컨텍스트 로드 및 빈 주입 상태를 점검합니다.

### 3. 성능 및 동시성 검증 (Performance & Concurrency)
- **Tool**: **k6**, JUnit 5 (Multi-thread)
- **검증 내용**:
    - 수백 명의 사용자가 동시에 예약 시 `maxCapacity` 초과 여부를 정밀하게 검증합니다.
    - 비관적 락(`PESSIMISTIC_WRITE`) 상황에서의 성능 병목 현상을 **k6**로 수치화하여 분석하고, 예외 처리 및 롤백 매커니즘을 테스트합니다.
