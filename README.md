## ✅ Kotlin TODO API

Spring Boot + Kotlin 기반의 TODO 관리 백엔드 API입니다.  
JWT 인증과 카카오/네이버 소셜 로그인, Soft Delete, Pageable 검색을 지원합니다.

---

### 🚀 실행 방법

```bash
# 프로젝트 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

---

### 📘 API 명세서
#### 🔐 인증
| Method | Endpoint          | Description         |
|--------|-------------------|---------------------|
| POST   | /users/signup     | 일반 회원가입             |
| POST   | /users/login      | 일반 로그인              |
| GET    | /users/me         | 내 정보 조회             |
| PUT    | /users/me         | 내 정보 수정 (닉네임/비밀번호)  |
| DELETE | /users/me         | 회원 탈퇴 (soft delete) |
| POST   | /auth/login/kakao | 카카오 소셜 로그인          |
| POST   | /auth/login/naver | 네이버 소셜 로그인          |

#### ✅ TODO
| Method  | 	Endpoint      | Description               |
|---------|----------------|---------------------------|
| POST    | /todos	        | TODO 등록                   |
| GET     | /todos	        | 내 TODO 목록 조회              |
| GET     | /todos/{id}    | TODO 단건 조회                |
| PUT     | /todos/{id}    | TODO 수정                   |
| DELETE  | /todos/{id}    | TODO 삭제 (soft delete)     |
| GET     | /todos/search	 | TODO 검색 (Pageable + Spec) |

### 📎 참고
- API 문서: http://localhost:8080/swagger-ui.html
- DB: SQLite (로컬 테스트용)