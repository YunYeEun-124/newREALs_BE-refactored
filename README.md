# 📰 NewREALs Backend

---

## 📌 프로젝트 소개
NewREALs는 사용자의 키워드를 통해 자신에게 맞는 진짜 뉴스를 새롭게 제시하는 서비스입니다.  
1020 세대의 뉴스 참여 증가와 개인화된 뉴스 경험 제공을 목표로 합니다.


---


## 🌟 서비스 주요 기능
- 사용자 맞춤 관심키워드 뉴스 제공
- AI를 활용해 쉽게 재구성한 설명, 요약, 용어 해설 제공
- T/F 퀴즈와 생각정리 : 능동적 참여 유도
- 매월 활동 및 관심사 분석 레포트 제공


---


## 📂 프로젝트 디렉토리 구조

```plaintext
src
├── main
│   ├── java
│   │   └── newREALs.backend
│   │       ├── 🛠️ config         # 설정 파일 (Spring Security 등)
│   │       ├── 📂 controller     # 컨트롤러 레이어
│   │       ├── 🗃️ domain         # 엔티티 클래스
│   │       ├── 📑 DTO            # 데이터 전송 객체 (DTO)
│   │       ├── 📦 repository     # JPA 리포지토리
│   │       ├── 🔒 security       # 보안 관련 클래스 (JWT, 필터 등)
│   │       └── 🧩 service        # 서비스 레이어
│   └── resources
│       ├── ⚙️ application.yml    # Spring Boot 설정 파일
│       ├── 🧪 DummyData.sql      # 테스트용 데이터 SQL
│       └── 📜 schema.sql         # 데이터베이스 스키마 SQL
├── test                          # 테스트 코드
├── build.gradle                  # 빌드 설정
├── settings.gradle               # Gradle 설정
└── .gitignore                    # Git 무시 파일
```


---



## 🛠️ 기술 스택
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.3.5
- **데이터베이스**: PostgreSQL
- **보안**: Spring Security, JWT
- **API 통신**: RESTful API, Kakao OAuth 2.0
- **기타**: Hibernate, Gradle, Log4j2


---



## 🚀 설치 및 실행 방법

#### 1. 프로젝트 클론
```bash
git clone https://github.com/2024-Fall-CapstoneDesign/newREALs_BE.git
cd newREALs_BE
```

#### 2. 환경 변수 설정
`application.yml` 또는 `.env` 파일에 아래와 같은 환경 변수를 설정합니다:

**`application.yml` 예시**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db
    username: postgres
    password: your-password
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: YOUR_KAKAO_CLIENT_ID
            client-secret: ""
            redirect-uri: "http://localhost:8080/login/oauth2/code/kakao"

openai:
  secret-key:""
  organization-id:""
```
#### 3. Gradle 빌드 및 실행
```bash
./gradlew clean build
./gradlew bootRun
```

#### 4. 서버 실행 확인
현재는 로컬 실행만 가능하며, 서버 배포 시 추가 정보를 제공하겠습니다.


---



## 🔮 향후 계획
