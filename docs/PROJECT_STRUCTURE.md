# 프로젝트 아키텍처 개요 (Technical Architecture)

본 프로젝트는 확장성과 유지보수성을 고려하여 **Spring Boot 기반 멀티 모듈 아키텍처**로 설계되었습니다. 각 레이어와 모듈은 역할에 충실하며, 핵심 비즈니스 로직의 응집도를 높이고 도메인 간의 의존성을 최소화했습니다.

## 1. 멀티 모듈 아키텍처 (Multi-Module Strategy)
- **Stack**: Spring Boot 3.5.7 / Java 21
- **`core` 모듈**: 도메인 엔티티, Repository, Querydsl Custom Implementation 및 공통 인프라 설정(Redis, JPA, Spatial)을 포함하며 모든 서비스의 근간이 됩니다.
- **`web` 모듈**: 클라이언트 요청 처리, 서비스 로직 실행, 보안 필터링(JWT) 및 화면 렌더링(Thymeleaf)을 담당합니다.

## 2. 주요 기술적 강점 (Technical Highlights)

### 🛡️ 하이브리드 인증 시스템 (JWT with Cookie/Header)
- **API Security**: REST API 요청 시 `Authorization` 헤더의 Bearer 토큰을 통한 무상태(Stateless) 인증.
- **SSR Compatibility**: Thymeleaf를 이용한 서버 사이드 렌더링 환경에서 `@PreAuthorize`를 통한 페이지 접근 제어를 위해 **쿠키 기반 토큰 전송** 방식 병행 사용.
- **RBAC (Role-Based Access Control)**: 사용자 권한(`USER`, `OWNER`)에 따른 엄격한 API 및 화면 접근 제어.

### 📍 공간 데이터 핸들링 (Spatial Data Processing)
- **JTS (Java Topology Suite)**를 활용하여 위경도 좌표를 `Point` 엔티티로 관리.
- **Leaflet.js** 연동을 통해 지도 기반의 직관적인 위치 선정 및 검색 기능 제공.

### ⚡ 동시성 제어 및 최적화 (Concurrency & Performance)
- **Pessimistic Lock (비관적 락)**: 선착순 예약 시스템의 데이터 정합성을 위해 JPA 비관적 락을 적용하여 중복 예약 및 정원 초과 방지.
- **Querydsl**: 복잡한 조건 기반의 매장 검색 쿼리를 Type-Safe하게 구현하여 런타임 오류 최소화 및 성능 최적화.

## 3. 디렉토리 구조 및 역할

```text
root/
├── core/ (Domain & Infrastructure)
│   └── entity/       # 유저, 매장, 예약 핵심 도메인 모델
│   └── repository/   # Querydsl을 결합한 고도화된 데이터 접근 계층
│   └── config/       # 공간 데이터 및 데이터베이스 설정 인프라
├── web/ (Application & Presentation)
│   └── service/      # 트랜잭션 단위의 비즈니스 유즈케이스 처리
│   └── controller/   # REST API 및 View Controller 통합 관리
│   └── jwt/          # 인증/인가 핵심 필터 및 토큰 프로바이더
│   └── templates/    # 사용자 권한별 반응형 뷰 (Thymeleaf)
└── docs/             # 설계 문서 (ERD, API Specs 등)
```

## 4. 핵심 API 워크플로우
1. **사장님(OWNER)**: 매장 등록(영업시간 설정) → 실시간 예약 현황 확인 → 매장 정보 관리.
2. **사용자(USER)**: 지도 기반 매장 탐색 → 예약 가능 시간 확인 및 신청 → 본인 예약 이력 관리.
