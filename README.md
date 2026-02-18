# 🍽️ 매장 예약 및 위치 기반 서비스 (Store Reservation Service)

> **대용량 트래픽을 고려한 동시성 제어 및 위치 기반 매장 검색 백엔드 API**  
> **Java 21, Spring Boot 3.5.7, PostgreSQL(PostGIS)**

<br>

## 📖 프로젝트 소개
사용자의 위치를 기반으로 주변 매장을 검색하고, 실시간으로 예약을 진행할 수 있는 서비스입니다.
단순한 CRUD 기능을 넘어, 인기 매장의 예약 신청이 동시에 몰리는 상황(Race Condition)을 가정하여 데이터 무결성을 보장하는 데 초점을 맞췄습니다. 또한, **PostGIS**를 활용하여 대량의 매장 데이터 속에서도 효율적인 위치 기반 쿼리를 수행하도록 구현했습니다.

<br>

## 🛠️ Tech Stack

### Backend
*   **Language:** Java 21
*   **Framework:** Spring Boot 3.2
*   **Database:** PostgreSQL 15 (PostGIS), Redis, H2 (Test)
*   **ORM & Query:** Spring Data JPA, Querydsl
*   **Security:** Spring Security, JWT (Access/Refresh Token)
*   **Testing:** JUnit 5, Mockito, **Testcontainers**

### Infrastructure & Tools
*   **Build:** Gradle
*   **VCS:** Git, GitHub

### Frontend (User/Owner View)
*   **Template Engine:** Thymeleaf (SSR)
*   **Library:** Leaflet.js (Map), Bootstrap 5, Vanilla JS (CSR)

<br>

## 🚀 핵심 기술적 도전 및 해결 (Key Challenges)

### 1. 동시성 제어를 통한 예약 시스템 (Concurrency Control)
*   **문제 상황:** 인기 매장의 경우, 다수의 사용자가 동시에 예약을 시도할 때 `최대 수용 인원(MaxCapacity)`을 초과하여 예약이 생성되는 **Race Condition** 발생 가능성을 확인했습니다.
*   **해결 방법:**
   *   Java `synchronized`는 다중 서버 환경(Scale-out)에서 동작하지 않으므로 제외했습니다.
   *   DB 레벨의 비관적 락(Pessimistic Lock, `PESSIMISTIC_WRITE`)을 적용하여 데이터 정합성을 최우선으로 확보했습니다.
   *   **Lock Scope:** 예약 테이블이 비어있는 초기 상태(0건)에서의 동시성 이슈를 방지하기 위해, 부모 엔티티인 `Store` 조회 시점에 락을 걸어 트랜잭션을 직렬화(Serialize)했습니다.
*   **검증:** `ExecutorService`와 `CountDownLatch`를 활용하여 **100명의 동시 예약 요청 테스트**를 수행, 데이터 정합성이 100% 보장됨을 검증했습니다.

### 2. Testcontainers를 활용한 신뢰성 있는 테스트 환경 구축
*   **문제 상황:** 로컬 테스트용 H2(In-memory DB)와 운영 DB(PostgreSQL) 간의 **락 동작 방식 차이** 및 **공간 함수(Spatial Function) 지원 여부**가 달라, 테스트의 신뢰도가 떨어지는 문제가 있었습니다.
*   **해결 방법:** **Testcontainers**를 도입하여 테스트 실행 시 Docker로 독립된 PostgreSQL 컨테이너를 띄우고, 운영 환경과 동일한 조건에서 통합 테스트(Integration Test)를 수행하도록 개선했습니다.

### 3. 위치 기반 매장 검색 (LBS) 최적화
*   **구현:** PostgreSQL의 **PostGIS** 확장을 사용하여 매장의 위/경도 좌표를 `Geometry(Point)` 타입으로 저장했습니다.
*   **쿼리:** 사용자의 현재 위치 기준 반경 N km 내의 매장을 조회하기 위해 `ST_Distance_Sphere` 등의 공간 함수를 활용하여, 단순 사각형 범위 검색보다 정확하고 효율적인 검색 로직을 구현했습니다.

<br>

## 🏗️ 시스템 아키텍처 및 설계

### API 구조 (Controller 분리 전략)
*   **PageController:** Thymeleaf 기반의 HTML 뷰 반환 (SSR)
*   **RestController:** JSON 데이터 반환 및 비즈니스 로직 처리 (CSR)
   *   *설계 의도:* 화면(View)과 데이터(Data)의 책임을 명확히 분리하여, 향후 프론트엔드 프레임워크(React, Vue 등) 도입 시 API 재사용성을 높이고 유지보수를 용이하게 했습니다.

### ERD 설계
*   **User:** 사용자 및 점주 정보 (Role: USER, OWNER)
*   **Store:** 매장 정보 (위치 정보 `Point` 포함), 주인(User)과 1:N 관계
*   **Reservation:** 예약 정보 (방문 시간, 인원), User/Store와 N:1 관계

<br>

## 🧪 테스트 전략 (Testing Strategy)

| 구분 | 도구 | 설명 |
| :--- | :--- | :--- |
| **Unit Test** | **Mockito** | Service 계층의 비즈니스 로직(권한 체크, 예외 처리, 값 검증)을 외부 의존성 없이 빠르게 검증. |
| **Integration Test** | **Testcontainers** | 실제 DB 환경에서 동시성 제어(Lock) 및 PostGIS 쿼리가 정상 동작하는지 검증. |