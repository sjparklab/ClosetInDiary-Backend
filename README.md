
# Closet in Diary — Backend (Spring Boot 3)

> **의류 코디 일기 관리 웹 서비스의 백엔드**  
> 사용자 인증, 친구/메시지 기능, AWS S3 이미지 업로드, 스프링 캐시 기반 이미지 조회를 지원
> 
## ✨ Features
- **JWT 인증/인가**: Access/Refresh 토큰 발급·검증·재발급
- **옷장(Outfit)**: 업로드(메타 + 이미지), 조회/수정/삭제
- **일기(Diary)**: 텍스트 + 메인 이미지 + 착장 다중 연결, CRUD
- **친구/메시지**: 친구 요청/수락/목록, 1:1 메시지
- **이미지 처리**: 업로드 시 서버 리사이즈(Thumbnailator) → **AWS S3 저장**
- 캐시: Caffeine 기반 읽기 성능 개선

---

## Tech Stack
- Spring Boot 3, Spring Security, Spring Data JPA
- JWT(io.jsonwebtoken), AWS SDK(S3), Thumbnailator, Caffeine Cache
- MySQL, H2

---

## 📂 주요 패키지
- `config/` : Security, JWT, S3Config, WebConfig
- `controller/` : Diary, Outfit(closet), Friend, FriendRequest, Message, User, Token, Image
- `domain/` : User, Diary, Outfit, RefreshToken, FriendRequest 등 JPA 엔티티
- `repository/` : *Repository (Spring Data JPA)
- `service/` : *Service 계층 (토큰, 이미지, 친구, 일기 등)

---

## 🔐 보안 설정 요약
- **허용 경로**: `/api/login`, `/api/signup`, `/api/refresh-token`
- 그 외 경로는 JWT 인증 필수
- `TokenAuthenticationFilter`에서 헤더 검증 → `SecurityContext` 등록
- 실패 시 `JwtAuthenticationEntryPoint`로 401 처리

---

## 🔑 주요 엔드포인트 (발췌)

### [Auth/User] (base: `/api`)
- `POST /login` → 로그인, access/refresh 발급
- `POST /signup` → 회원가입
- `POST /refresh-token` → 토큰 재발급
- `GET /user-data` → 사용자 정보 조회
- `PUT /user-data` → 사용자 정보 변경
- `GET /user-profile-picture` → 프로필 이미지 조회(또는 `/{fileKey}`)

### [Closet] (base: `/api/closet`)
- `POST /upload` → 멀티파트 업로드(이미지 + 메타)
- `GET /{category}` → 카테고리별 조회
- `GET /edit/{id}` → 단건 조회
- `PUT /image/{id}` → 이미지 교체
- `DELETE /image/{id}` → 삭제
- `GET /{id}/diaries` → 해당 착장이 포함된 일기 목록

### [Diary] (base: `/api/diaries`)
- `GET /{id}` → 단건 조회(권한 검사 포함)
- `POST` (multipart/form-data) → 생성(본문 + 메인 이미지 + 착장 목록)
- `PUT /{id}` (multipart/form-data) → 수정
- `DELETE /{id}` → 삭제
- `GET /image/{fileKey}` → (캐시) 이미지 서빙

### [Friends] (base: `/api/friends`)
- `GET /list` → 내 친구 목록
- `DELETE /{id}` → 친구 삭제

### [Friend Requests] (base: `/api/friend-requests`)
- `POST /send/by-id`
- `POST /send/by-username`
- `POST /send/by-email`
- `POST /accept`
- `POST /reject`

### [Message] (base: `/api/message`)
- `POST /send`
- `GET /history/{friendId}`

---

## 환경 변수
```properties
# JWT
jwt.issuer=ClosetInDiary
jwt.secretKey=YOUR_LONG_RANDOM_SECRET

# AWS S3
aws.credentials.accessKey=AKIA...
aws.credentials.secretKey=...
aws.region=ap-northeast-2
```

---

## 이미지 처리 흐름
1. 멀티파트 수신 → 임시 파일 변환
2. Thumbnailator로 리사이즈
3. S3에 업로드(메타데이터 포함)
4. 조회 시 Caffeine 캐시로 핫 컨텐츠 가속

---

## 실행
```bash
./gradlew bootRun
```
