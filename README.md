# Reservation Map: 위치 기반 실시간 매장 예약 시스템

본 프로젝트는 위치 기반 서비스를 활용하여 사용자가 근처 매장을 탐색하고 실시간으로 예약할 수 있는 서비스입니다. 멀티 모듈 아키텍처와 최신 보안 표준을 준수하여 설계되었습니다.

## 🚀 주요 기능 (Key Features)

### 🏪 매장 관리 (Owner Flow)
- **지도 기반 등록**: 지도를 클릭하여 위치 좌표 및 주소를 자동 추출하고 매장을 등록합니다.
- **영업 시간 설정**: 매장별 운영 시간을 관리하며, 해당 시간 외의 예약은 자동으로 차단됩니다.
- **실시간 예약 대시보드**: 매장에 접수된 모든 예약 현황을 한눈에 파악할 수 있습니다.

### 📅 예약 시스템 (User Flow)
- **주변 매장 탐색**: 내 위치 기준 반경 내 매장을 탐색하고 상세 정보를 확인합니다.
- **스마트 예약**: 인원수와 시간을 선택하여 예약하며, 수용 인원 초과 시 자동으로 대기 처리됩니다.
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

## 🧪 테스트 (Testing)
- Mockito를 이용한 Service 단위 테스트 및 Testcontainers 기반 통합 테스트 제공.
- 예약 동시성 문제 해결을 위한 비관적 락 검증 테스트 포함.
