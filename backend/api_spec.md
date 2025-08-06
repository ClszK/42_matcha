# Web Matcha API 명세서 (Enhanced)

> 모든 타임스탬프는 UTC 기준 ISO 8601 형식입니다.

## 1. 인증(Authentication) 및 계정 관리

### 회원가입

```http
POST /api/auth/register
```

- 요청 바디:

  ```json
  {
    "email_address": "user@example.com",
    "username": "john_doe",
    "first_name": "John",
    "last_name": "Doe",
    "password": "SecurePass123!"
  }
  ```

- 성공 응답 (201):

  ```json
  {
    "message": "Verification email sent",
    "user_id": 123
  }
  ```

- 에러 응답 (422):

  ```json
  {
    "error": "validation_failed",
    "details": {
      "password": [
        "Password contains common words",
        "Password must be ≥8 characters"
      ],
      "email_address": ["Email already exists"]
    }
  }
  ```

### 이메일 인증

```http
GET /api/auth/verify?token={verification_token}
```

- 성공 응답 (200):

  ```json
  { "message": "Email verified successfully" }
  ```

### 로그인

```http
POST /api/auth/login
```

- 요청 바디:

  ```json
  {
    "username": "john_doe",
    "password": "SecurePass123!"
  }
  ```

- 성공 응답 (200):

  ```json
  {
    "access_token": "eyJhbGci...",
    "refresh_token": "def456...",
    "user": {
      "id": 123,
      "username": "john_doe",
      "email_verified": true,
      "profile_completed": true
    }
  }
  ```

### 로그아웃

```http
POST /api/auth/logout
```

- 요청 바디:

  ```json
  { "refresh_token": "def456..." }
  ```

- 성공 응답 (204)

### 토큰 갱신

```http
POST /api/auth/refresh
```

- 요청 바디:

  ```json
  { "refresh_token": "def456..." }
  ```

- 성공 응답 (200):

  ```json
  {
    "access_token": "ghi789...",
    "refresh_token": "jkl012..."
  }
  ```

### 비밀번호 재설정 요청

```http
POST /api/auth/password-reset/request
```

- 요청 바디:

  ```json
  { "email_address": "user@example.com" }
  ```

- 성공 응답 (200):

  ```json
  { "message": "Password reset email sent" }
  ```

### 비밀번호 재설정 확인

```http
POST /api/auth/password-reset/confirm
```

- 요청 바디:

  ```json
  {
    "token": "reset_token",
    "new_password": "NewSecure123!"
  }
  ```

- 성공 응답 (200):

  ```json
  { "message": "Password has been reset" }
  ```

---

## 2. 사용자 기본 정보 관리

### 내 기본 정보 조회

```http
GET /api/users/me
```

- 성공 응답 (200):

  ```json
  {
    "id": 123,
    "username": "john_doe",
    "email_address": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "email_verified": true,
    "account_status": "active",
    "created_at": "2025-01-01T00:00:00Z",
    "profile_completed": true
  }
  ```

### 내 기본 정보 수정

```http
PUT /api/users/me
```

- 요청 바디:

  ```json
  {
    "first_name": "John",
    "last_name": "Doe",
    "email_address": "newemail@example.com"
  }
  ```

- 성공 응답 (200):

  ```json
  {
    "message": "User information updated successfully",
    "email_verification_required": true
  }
  ```

> **참고**: 이메일 주소 변경 시 재인증이 필요하며, 인증 완료 전까지 기존 이메일이 유지됩니다.

---

## 3. 사용자 프로필(Profile) 관리

### 내 프로필 조회

```http
GET /api/profiles/me
```

- 성공 응답 (200):

  ```json
  {
    "profile": {
      "id": 123,
      "user_id": 456,
      "gender": "male",
      "orientation": "straight",
      "birth_date": "1990-05-15",
      "biography": "Love hiking and photography",
      "fame_rating": 85,
      "is_active": true,
      "created_at": "2025-01-01T00:00:00Z"
    },
    "preferences": {
      "max_distance": 50,
      "min_age": 22,
      "max_age": 35,
      "preferred_gender": "female"
    },
    "location": {
      "latitude": 37.123456,
      "longitude": 127.123456,
      "updated_at": "2025-01-15T10:30:00Z"
    },
    "photos": [
      {
        "id": 1,
        "url": "https://example.com/photo1.jpg",
        "is_primary": true,
        "display_order": 1
      }
    ],
    "tags": ["hiking", "photography", "travel"]
  }
  ```

### 내 프로필 생성 (최초 프로필 설정)

```http
POST /api/profiles
```

- 요청 바디:

  ```json
  {
    "gender": "male",
    "orientation": "straight",
    "birth_date": "1990-05-15",
    "biography": "Love hiking and photography"
  }
  ```

- 성공 응답 (201):

  ```json
  {
    "message": "Profile created successfully",
    "profile": {
      "id": 123,
      "user_id": 456,
      "gender": "male",
      "orientation": "straight",
      "birth_date": "1990-05-15",
      "biography": "Love hiking and photography",
      "fame_rating": 0,
      "is_active": true,
      "created_at": "2025-01-15T10:30:00Z"
    }
  }
  ```

- 에러 응답 (409, 이미 프로필 존재):

  ```json
  {
    "error": "profile_already_exists",
    "message": "Profile already exists. Use PUT /api/profiles/me to update."
  }
  ```

```http
PUT /api/profiles/me
```

- 요청 바디:

  ```json
  {
    "gender": "male",
    "orientation": "straight",
    "birth_date": "1990-05-15",
    "biography": "Updated biography"
  }
  ```

- 성공 응답 (200)

### Fame Rating 조회

```http
GET /api/profiles/me/fame-rating
GET /api/profiles/{profile_id}/fame-rating
```

- 성공 응답 (200):

  ```json
  {
    "fame_rating": 85,
    "breakdown": {
      "profile_completeness": 30,
      "photo_quality": 25,
      "activity_level": 20,
      "likes_received": 10
    },
    "rank_percentile": 78
  }
  ```

### 프로필 조회 기록

```http
POST /api/profiles/{profile_id}/view
```

- 자동 기록 또는 직접 기록용 엔드포인트
- 성공 응답 (204)

### 내 프로필 조회 이력

```http
GET /api/profiles/me/views?page=1&limit=20&from_date=2025-01-01
```

- 성공 응답 (200):

  ```json
  {
    "views": [
      {
        "viewer": {
          "id": 789,
          "username": "jane_doe",
          "profile_photo": "https://example.com/jane.jpg"
        },
        "viewed_at": "2025-01-15T14:30:00Z"
      }
    ],
    "total_count": 42,
    "new_views_count": 5
  }
  ```

### 나를 좋아한 사용자 목록

```http
GET /api/profiles/me/likes?page=1&limit=20
```

- 성공 응답 (200):

  ```json
  {
    "likes": [
      {
        "liker": {
          "id": 789,
          "username": "jane_doe",
          "profile_photo": "https://example.com/jane.jpg",
          "age": 28,
          "distance": 2.5
        },
        "liked_at": "2025-01-15T14:30:00Z",
        "is_mutual": false
      }
    ],
    "total_count": 15,
    "new_likes_count": 3
  }
  ```

### 관심사(태그) 관리

```http
GET    /api/profiles/me/tags
POST   /api/profiles/me/tags
DELETE /api/profiles/me/tags/{tag}
```

- POST 요청 바디:

  ```json
  { "tag": "cooking" }
  ```

- GET 성공 응답 (200):

  ```json
  {
    "tags": ["hiking", "photography", "travel", "cooking"],
    "popular_tags": ["travel", "music", "fitness", "cooking", "art"]
  }
  ```

### 사진 관리 (최대 5장)

```http
GET    /api/profiles/me/photos
POST   /api/profiles/me/photos
PUT    /api/profiles/me/photos/{photo_id}
DELETE /api/profiles/me/photos/{photo_id}
```

- POST (multipart/form-data):
  - `file`: 이미지
  - `is_primary`: boolean
  - `display_order`: integer

- POST 성공 응답 (201):

  ```json
  {
    "photo": {
      "id": 5,
      "url": "https://example.com/photo5.jpg",
      "is_primary": false,
      "display_order": 2
    },
    "total_photos": 3,
    "max_allowed": 5
  }
  ```

- 에러 응답 (409, limit exceeded):

  ```json
  {
    "error": "photo_limit_exceeded",
    "message": "Maximum 5 photos allowed",
    "current_count": 5,
    "max_allowed": 5
  }
  ```

### 환경설정(Preferences) 관리

```http
GET /api/profiles/me/preferences
PUT /api/profiles/me/preferences
```

- PUT 요청 바디:

  ```json
  {
    "max_distance": 100,
    "min_age": 18,
    "max_age": 35,
    "preferred_gender": "any"
  }
  ```

### 위치(Location) 관리

```http
GET /api/profiles/me/location
PUT /api/profiles/me/location
```

- PUT 요청 바디:

  ```json
  {
    "latitude": 37.123456,
    "longitude": 127.123456,
    "manual_override": true
  }
  ```

### 사용자 활동 상태 업데이트

```http
POST /api/users/me/activity
```

- 요청 바디:

  ```json
  {
    "status": "online",
    "location": {
      "latitude": 37.123456,
      "longitude": 127.123456
    }
  }
  ```

- 성공 응답 (204)

> **구현 참고**: 실시간 상태는 Redis에 즉시 반영되며, MySQL은 배치로 주기적 업데이트

### 내 프로필 통계

```http
GET /api/profiles/me/analytics
```

- 성공 응답 (200):

  ```json
  {
    "profile_views": {
      "total": 156,
      "this_week": 23,
      "today": 5
    },
    "likes_received": {
      "total": 42,
      "this_week": 8,
      "today": 2
    },
    "matches": {
      "total": 15,
      "this_week": 3
    },
    "fame_rating_history": [
      { "date": "2025-01-01", "rating": 78 },
      { "date": "2025-01-15", "rating": 85 }
    ]
  }
  ```

---

## 4. 탐색(Browsing) 및 검색(Research)

### 추천 프로필 목록

```http
GET /api/browse?page=1&limit=20&sort_by=distance&order=asc&min_age=18&max_age=35&max_distance=50&min_fame=10&max_fame=100&tags=hiking,travel
```

- 성공 응답 (200):

  ```json
  {
    "profiles": [
      {
        "id": 789,
        "username": "jane_doe",
        "age": 28,
        "distance": 2.5,
        "fame_rating": 92,
        "primary_photo": "https://example.com/jane.jpg",
        "common_tags": ["hiking", "photography"],
        "compatibility_score": 85,
        "is_online": true,
        "last_seen": "2025-01-15T14:30:00Z"
      }
    ],
    "total_count": 156,
    "page": 1,
    "has_more": true
  }
  ```

### 고급 검색

```http
GET /api/search?page=1&limit=20&age_range=22-35&fame_range=50-100&max_distance=25&near_lat=37.123456&near_lng=127.123456&tags=hiking,photography&orientation=straight&gender=female&is_online=true&sort_by=compatibility
```

---

## 5. 프로필 조회 및 상호작용(Profile View & Interaction)

### 타 사용자 프로필 조회

```http
GET /api/profiles/{profile_id}
```

- 자동으로 조회 기록 생성
- 성공 응답 (200):

  ```json
  {
    "profile": {
      "id": 789,
      "username": "jane_doe",
      "age": 28,
      "gender": "female",
      "orientation": "straight",
      "biography": "Love outdoor activities",
      "fame_rating": 92,
      "distance": 2.5,
      "common_tags": ["hiking", "photography"],
      "photos": [...],
      "tags": [...],
      "is_online": true,
      "last_seen": "2025-01-15T14:30:00Z"
    },
    "interaction_status": {
      "i_liked": false,
      "they_liked": true,
      "is_matched": false,
      "is_blocked": false,
      "view_count": 3
    }
  }
  ```

### 온라인 상태 확인

```http
GET /api/profiles/{profile_id}/online-status
```

- 성공 응답 (200):

  ```json
  {
    "is_online": true,
    "last_seen_at": "2025-01-15T14:30:00Z",
    "status": "active"
  }
  ```

> **구현 참고**: 온라인 상태는 Redis로 실시간 관리되며, `last_seen_at`는 Redis 우선 조회 후 MySQL 백업 데이터 사용

### 좋아요(Like) 추가/삭제

```http
POST   /api/profiles/{profile_id}/like
DELETE /api/profiles/{profile_id}/like
```

- POST 성공 응답 (201):

  ```json
  {
    "liked": true,
    "is_mutual": true,
    "match_created": true,
    "match_id": 456
  }
  ```

### 차단(Block) 및 신고(Report)

```http
POST   /api/profiles/{profile_id}/block
DELETE /api/profiles/{profile_id}/block
POST   /api/profiles/{profile_id}/report
```

- Block POST 요청 바디:

  ```json
  {
    "reason": "Inappropriate behavior"
  }
  ```

- Report POST 요청 바디:

  ```json
  {
    "category": "inappropriate_content",
    "description": "Detailed description of the issue"
  }
  ```

---

## 6. 매칭(Match) 및 대화(Conversation)

### 매칭 목록

```http
GET /api/matches?page=1&limit=20&status=active
```

- 성공 응답 (200):

  ```json
  {
    "matches": [
      {
        "id": 456,
        "matched_user": {
          "id": 789,
          "username": "jane_doe",
          "profile_photo": "https://example.com/jane.jpg",
          "age": 28
        },
        "matched_at": "2025-01-15T10:00:00Z",
        "last_message": {
          "content": "Hey! How are you?",
          "sent_at": "2025-01-15T14:30:00Z",
          "sender_id": 789
        },
        "unread_count": 2,
        "compatibility_score": 85
      }
    ],
    "total_count": 15
  }
  ```

### 매칭 상세 정보

```http
GET /api/matches/{match_id}/details
```

- 성공 응답 (200):

  ```json
  {
    "match_id": 456,
    "matched_at": "2025-01-15T10:00:00Z",
    "common_tags": ["hiking", "photography", "travel"],
    "distance": 2.5,
    "compatibility_score": 85,
    "compatibility_breakdown": {
      "location": 20,
      "interests": 35,
      "age_preference": 15,
      "activity_level": 15
    }
  }
  ```

### 매칭 해제

```http
DELETE /api/matches/{match_id}
```

- 요청 바디 (optional):

  ```json
  { "reason": "No longer interested" }
  ```

- 성공 응답 (204)

### 대화방 목록

```http
GET /api/conversations?page=1&limit=20
```

- 성공 응답 (200):

  ```json
  {
    "conversations": [
      {
        "id": 123,
        "match_id": 456,
        "other_user": {
          "id": 789,
          "username": "jane_doe",
          "profile_photo": "https://example.com/jane.jpg",
          "is_online": true
        },
        "last_message": {
          "content": "See you tomorrow!",
          "sent_at": "2025-01-15T14:30:00Z",
          "sender_id": 789
        },
        "unread_count": 0,
        "started_at": "2025-01-15T10:00:00Z"
      }
    ]
  }
  ```

### 메시지 조회 및 전송

```http
GET  /api/conversations/{conversation_id}/messages?page=1&limit=50&before_id=123
POST /api/conversations/{conversation_id}/messages
PUT  /api/conversations/{conversation_id}/messages/{message_id}/read
```

- POST 요청 바디:

  ```json
  {
    "content": "Hello! Nice to meet you",
    "message_type": "text"
  }
  ```

- POST 성공 응답 (201):

  ```json
  {
    "message": {
      "id": 789,
      "content": "Hello! Nice to meet you",
      "message_type": "text",
      "sent_at": "2025-01-15T14:30:00Z",
      "sender_id": 123
    }
  }
  ```

---

## 7. 알림(Notifications)

### 알림 목록 조회

```http
GET /api/notifications?page=1&limit=20&is_read=false&type=like
```

- 성공 응답 (200):

  ```json
  {
    "notifications": [
      {
        "id": 1,
        "type": "like",
        "title": "New Like!",
        "body": "jane_doe liked your profile",
        "actor": {
          "id": 789,
          "username": "jane_doe",
          "profile_photo": "https://example.com/jane.jpg"
        },
        "metadata": {
          "profile_id": 789,
          "match_created": false
        },
        "is_read": false,
        "created_at": "2025-01-15T14:30:00Z"
      }
    ],
    "unread_count": 5,
    "total_count": 42
  }
  ```

### 읽음 처리

```http
PUT /api/notifications/{notification_id}/read
PUT /api/notifications/mark-all-read
```

- 성공 응답 (204)

### 실시간 알림 스트림

```http
SSE: GET /api/notifications/stream
WebSocket: /ws/notifications/{user_id}
```

---

## 8. 실시간 기능 (WebSocket/SSE)

> **온라인 상태 관리**: Redis 기반으로 실시간 온라인 상태 추적, TTL 5분 설정으로 자동 만료

### 실시간 채팅

```http
WebSocket: /ws/chat/{conversation_id}
```

- 연결 시 인증:

  ```json
  {
    "type": "auth",
    "token": "jwt_token_here"
  }
  ```

- 메시지 전송:

  ```json
  {
    "type": "message",
    "content": "Hello!",
    "message_type": "text"
  }
  ```

- 메시지 수신:

  ```json
  {
    "type": "message",
    "id": 789,
    "content": "Hello!",
    "sender_id": 456,
    "sent_at": "2025-01-15T14:30:00Z"
  }
  ```

- 타이핑 상태:

  ```json
  {
    "type": "typing",
    "user_id": 456,
    "is_typing": true
  }
  ```

### 실시간 알림

```http
WebSocket: /ws/notifications/{user_id}
```

- 알림 수신:

  ```json
  {
    "type": "notification",
    "notification": {
      "id": 123,
      "type": "like",
      "title": "New Like!",
      "actor": {...},
      "created_at": "2025-01-15T14:30:00Z"
    }
  }
  ```

---

## 9. 보안·에러 처리·상태 코드·헤더

### 표준 에러 응답

```json
{
  "error": "error_code",
  "message": "Human readable error message",
  "details": {
    "field_name": ["Specific error for this field"]
  },
  "timestamp": "2025-01-15T14:30:00Z",
  "path": "/api/profiles/me"
}
```

### 상태 코드

- `200` OK - 성공
- `201` Created - 생성됨
- `204` No Content - 성공 (응답 바디 없음)
- `400` Bad Request - 잘못된 요청
- `401` Unauthorized - 인증 필요
- `403` Forbidden - 권한 없음
- `404` Not Found - 찾을 수 없음
- `409` Conflict - 충돌 (이미 존재함)
- `422` Unprocessable Entity - 검증 실패
- `429` Too Many Requests - 너무 많은 요청
- `500` Internal Server Error - 서버 에러

### 공통 헤더

```
Authorization: Bearer {access_token}
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1620000000
Access-Control-Allow-Origin: https://your-domain.com
Content-Security-Policy: default-src 'self';
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
```

### 레이트 리미팅

모든 API 응답에 레이트 리미팅 헤더 포함:

- 인증 관련: 10 requests/minute
- 일반 API: 100 requests/hour
- 검색/탐색: 50 requests/hour
- 실시간 기능: 1000 requests/hour

---

## 10. 환경 변수 (.env)

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=matcha
DB_USER=matcha_user
DB_PASSWORD=secure_password

# Redis (온라인 상태 관리)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_password
REDIS_DB=0

# JWT
JWT_SECRET=your_super_secret_jwt_key
JWT_EXPIRES_IN=1h
REFRESH_TOKEN_EXPIRES_IN=7d

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# File Upload
UPLOAD_MAX_SIZE=5MB
ALLOWED_FILE_TYPES=jpg,jpeg,png,gif

# Security
BCRYPT_ROUNDS=12
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_WINDOW=15

# Real-time
WEBSOCKET_PORT=3001

# Activity Management
ACTIVITY_BATCH_INTERVAL=300  # 5분마다 MySQL 동기화
ONLINE_STATUS_TTL=300        # Redis TTL 5분
```
