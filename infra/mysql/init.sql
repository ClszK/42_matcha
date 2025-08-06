SET FOREIGN_KEY_CHECKS = 0;

-- ====================================
-- 1) USER AGGREGATE (Root: users)
-- ====================================
CREATE TABLE users (
  id                   INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  email_address        VARCHAR(255)     NOT NULL,
  username             VARCHAR(255)     NOT NULL,
  first_name           VARCHAR(100)     NOT NULL,  -- 추가: 이름
  last_name            VARCHAR(100)     NOT NULL,  -- 추가: 성
  password_hash        VARCHAR(255)     NOT NULL,
  email_verified       TINYINT(1)       NOT NULL DEFAULT 0,
  verification_token   VARCHAR(255)     DEFAULT NULL,
  password_reset_token VARCHAR(255)     DEFAULT NULL,
  account_status       ENUM('active','suspended','deleted') 
                       NOT NULL DEFAULT 'active',
  token_version        INT              NOT NULL DEFAULT 0,
  created_at           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP 
                        ON UPDATE CURRENT_TIMESTAMP,
  last_seen_at         DATETIME         DEFAULT NULL,  -- 추가: 마지막 접속 (Redis 백업)
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email    (email_address),
  UNIQUE KEY uk_users_username (username),
  INDEX idx_users_last_seen (last_seen_at)  -- 성능 최적화
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
  id            INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  user_id       INT UNSIGNED     NOT NULL,
  token_hash    CHAR(64)         NOT NULL,
  issued_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at    DATETIME         NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY uk_refresh_tokens_hash (token_hash)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ====================================
-- 2) PROFILE AGGREGATE (Root: profiles)
-- ====================================
CREATE TABLE profiles (
  id           INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  user_id      INT UNSIGNED     NOT NULL, -- FK 제거 (순수 DDD)
  gender       ENUM('male','female','non-binary','other') DEFAULT NULL,
  orientation  ENUM('straight','gay','lesbian','bisexual','pansexual','other') 
               DEFAULT NULL,
  birth_date   DATE             DEFAULT NULL,
  biography    TEXT             DEFAULT NULL,
  fame_rating  INT              DEFAULT 0,  -- 추가: 명성 점수
  is_active    TINYINT(1)       NOT NULL DEFAULT 1,
  created_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP 
               ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_profiles_user (user_id),
  INDEX idx_profiles_fame_rating (fame_rating),  -- 성능 최적화
  INDEX idx_profiles_active_fame (is_active, fame_rating)  -- 복합 인덱스
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profile_preferences (
  profile_id       INT UNSIGNED     NOT NULL,
  max_distance     INT              NOT NULL DEFAULT 50,
  min_age          INT              NOT NULL DEFAULT 18,
  max_age          INT              NOT NULL DEFAULT 100,
  preferred_gender ENUM('male','female','non-binary','any') 
                    NOT NULL DEFAULT 'any',
  created_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP 
                    ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (profile_id),
  FOREIGN KEY (profile_id) REFERENCES profiles(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profile_locations (
  profile_id  INT UNSIGNED NOT NULL,
  latitude    DECIMAL(9,6) DEFAULT NULL,
  longitude   DECIMAL(9,6) DEFAULT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP 
               ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (profile_id),
  FOREIGN KEY (profile_id) REFERENCES profiles(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profile_photos (
  id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  profile_id    INT UNSIGNED NOT NULL,
  url           VARCHAR(500)   NOT NULL,
  display_order INT            NOT NULL DEFAULT 0,
  is_primary    TINYINT(1)     NOT NULL DEFAULT 0,
  uploaded_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  FOREIGN KEY (profile_id) REFERENCES profiles(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profile_tags (
  profile_id  INT UNSIGNED NOT NULL,
  tag         VARCHAR(100)   NOT NULL,
  added_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (profile_id, tag),
  FOREIGN KEY (profile_id) REFERENCES profiles(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE profile_fame_rating_history (
  id                 INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  profile_id         INT UNSIGNED     NOT NULL,
  rating             INT              NOT NULL,
  calculated_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  calculation_reason TEXT             DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (profile_id) REFERENCES profiles(id) 
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_profile_calculated (profile_id, calculated_at)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ====================================
-- 3) MATCHING AGGREGATE (User 기반으로 수정)
-- ====================================
CREATE TABLE match_views (
  id         INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  viewer_id  INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  viewed_id  INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  viewed_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_views_viewer_viewed (viewer_id, viewed_id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE match_likes (
  id            INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  liker_id      INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  liked_id      INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  liked_at      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  withdrawn_at  DATETIME         DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_likes_liker_liked (liker_id, liked_id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE matches (
  id            INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  user1_id      INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  user2_id      INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  u1            INT              AS (LEAST(user1_id, user2_id)) STORED,
  u2            INT              AS (GREATEST(user1_id, user2_id)) STORED,
  matched_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  dissolved_at  DATETIME         DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_matches_ordered (u1, u2)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ====================================
-- 4) CONVERSATION AGGREGATE (Root: conversations)
-- ====================================
CREATE TABLE conversations (
  id               INT UNSIGNED NOT NULL AUTO_INCREMENT,
  match_id         INT UNSIGNED NOT NULL, -- Match ID만 저장, FK 없음
  last_message_at  DATETIME     DEFAULT NULL,
  started_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_conversations_match (match_id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversation_messages (
  id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id INT UNSIGNED NOT NULL,
  sender_id       INT UNSIGNED NOT NULL, -- User ID만 저장, FK 없음
  content         TEXT         NOT NULL,
  message_type    ENUM('text','image','emoji') NOT NULL DEFAULT 'text',
  sent_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at         DATETIME     DEFAULT NULL,
  is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  FOREIGN KEY (conversation_id) REFERENCES conversations(id)
    ON DELETE CASCADE ON UPDATE CASCADE
  -- sender_id는 FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ====================================
-- 5) NOTIFICATION AGGREGATE (Root: notifications)  
-- ====================================
CREATE TABLE notifications (
  id                INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  recipient_id      INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  notification_type ENUM('like','match','message','view','system') 
                    NOT NULL,
  actor_id          INT UNSIGNED     DEFAULT NULL, -- User ID만 저장, FK 없음
  reference_id      INT UNSIGNED     DEFAULT NULL,
  title             VARCHAR(255)     NOT NULL,
  body              TEXT             DEFAULT NULL,
  is_read           TINYINT(1)       NOT NULL DEFAULT 0,
  created_at        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at           DATETIME         DEFAULT NULL,
  PRIMARY KEY (id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ====================================
-- 6) MODERATION AGGREGATE
-- ====================================
CREATE TABLE blocks (
  id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  blocker_id    INT UNSIGNED NOT NULL, -- User ID만 저장, FK 없음
  blocked_id    INT UNSIGNED NOT NULL, -- User ID만 저장, FK 없음
  reason        VARCHAR(255) DEFAULT NULL,
  blocked_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  unblocked_at  DATETIME     DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_blocks_blocker_blocked (blocker_id, blocked_id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reports (
  id             INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  reporter_id    INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  reported_id    INT UNSIGNED     NOT NULL, -- User ID만 저장, FK 없음
  category       ENUM('inappropriate_content','harassment','fake_profile','spam','other') 
                 NOT NULL,
  description    TEXT             DEFAULT NULL,
  status         ENUM('pending','reviewed','resolved','dismissed') 
                 NOT NULL DEFAULT 'pending',
  priority       ENUM('low','medium','high','critical') 
                 NOT NULL DEFAULT 'medium',
  reported_at    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at    DATETIME         DEFAULT NULL,
  reviewed_by    INT UNSIGNED     DEFAULT NULL, -- User ID만 저장, FK 없음
  PRIMARY KEY (id)
  -- FK 없음: Aggregate 간 참조는 ID만
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
