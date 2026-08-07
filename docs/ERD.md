# Tài Liệu Thiết Kế Cơ Sở Dữ Liệu (ERD Document)
## Hệ Thống Học Từ Vựng Flashcard & SRS (Spaced Repetition System) - Tích Hợp Spring AI, 1vs1, Bảng Xếp Hạng & Huy Hiệu

---

## 1. Sơ Đồ Thực Thể Liên Kết Tổng Thể (Mermaid ERD Diagram)

```mermaid
erDiagram
    USERS ||--o{ REFRESH_TOKENS : "has many"
    USERS ||--o1 USER_SETTINGS : "has one"
    USERS ||--o{ DECKS : "owns"
    USERS ||--o{ USER_CARD_SRS : "tracks progression"
    USERS ||--o{ QUIZ_RESULTS : "completes"
    USERS ||--o{ USER_ACTIVITIES : "logs"
    USERS ||--o1 USER_STREAKS : "maintains"
    USERS ||--o{ SPRING_AI_CHAT_MEMORY : "stores chat history"

    USERS ||--o{ BATTLES : "player1 (host)"
    USERS ||--o{ BATTLES : "player2 (opponent)"
    DECKS ||--o{ BATTLES : "used in battle"
    BATTLES ||--o{ BATTLE_DETAILS : "contains"
    CARDS ||--o{ BATTLE_DETAILS : "tested in"

    USERS ||--o{ USER_BADGES : "earns"
    BADGES ||--o{ USER_BADGES : "awarded to"
    USERS ||--o{ LEADERBOARD_SNAPSHOTS : "ranked in"
    USERS ||--o{ NOTIFICATIONS : "receives"

    DECKS ||--o{ CARDS : "contains"
    DECKS ||--o{ DECK_TAGS : "categorized by"
    TAGS ||--o{ DECK_TAGS : "applied to"
    DECKS ||--o{ DECKS : "forked/cloned from (parent_deck_id)"

    CARDS ||--o{ USER_CARD_SRS : "has SRS state"
    CARDS ||--o{ QUIZ_DETAILS : "referenced in"
    QUIZ_RESULTS ||--o{ QUIZ_DETAILS : "contains"

    USERS {
        bigint id PK
        string email UK
        string password_hash
        string full_name
        string avatar_url
        enum role "ROLE_USER, ROLE_ADMIN"
        enum status "ACTIVE, INACTIVE, BLOCKED"
        timestamp created_at
        timestamp updated_at
    }

    REFRESH_TOKENS {
        bigint id PK
        bigint user_id FK
        string token UK
        string device_info
        timestamp expires_at
        timestamp created_at
    }

    USER_SETTINGS {
        bigint id PK
        bigint user_id FK,UK
        int daily_new_cards_goal
        int daily_review_cards_goal
        time reminder_time
        boolean sound_enabled
        timestamp updated_at
    }

    DECKS {
        bigint id PK
        bigint user_id FK "Owner"
        bigint parent_deck_id FK "Nullable - for cloned decks"
        string title
        text description
        string cover_image_url
        string source_language "en"
        string target_language "vi"
        enum visibility "PRIVATE, PUBLIC"
        boolean is_official
        int total_cards
        int fork_count
        boolean is_deleted
        timestamp created_at
        timestamp updated_at
    }

    TAGS {
        bigint id PK
        string name UK "TOEIC, IELTS, IT English"
        string slug UK
    }

    DECK_TAGS {
        bigint deck_id PK,FK
        bigint tag_id PK,FK
    }

    CARDS {
        bigint id PK
        bigint deck_id FK
        string word
        string phonetic
        string part_of_speech "Noun, Verb, Adj..."
        text meaning
        text definition_en
        text example_sentence
        text example_meaning
        string image_url
        string audio_url
        text note
        int position
        timestamp created_at
        timestamp updated_at
    }

    USER_CARD_SRS {
        bigint id PK
        bigint user_id FK
        bigint card_id FK
        enum status "NEW, LEARNING, MASTERED"
        float easiness_factor "Default 2.5"
        int repetitions "Default 0"
        int interval "Default 0 (days)"
        timestamp last_reviewed_at
        date next_review_date "Indexed for Study Queue"
        timestamp created_at
        timestamp updated_at
    }

    QUIZ_RESULTS {
        bigint id PK
        bigint user_id FK
        enum scope "DECK, ALL, TAG"
        int total_questions
        int correct_answers
        float score
        float accuracy_rate
        int time_spent_seconds
        timestamp created_at
    }

    QUIZ_DETAILS {
        bigint id PK
        bigint quiz_result_id FK
        bigint card_id FK
        enum question_type "MULTIPLE_CHOICE, FILL_IN_BLANK, MATCHING, LISTENING"
        text user_answer
        text correct_answer
        boolean is_correct
        float score_earned
    }

    USER_ACTIVITIES {
        bigint id PK
        bigint user_id FK
        date activity_date
        int cards_reviewed_count
        int quizzes_completed_count
        timestamp created_at
    }

    USER_STREAKS {
        bigint id PK
        bigint user_id FK,UK
        int current_streak
        int longest_streak
        date last_active_date
        timestamp updated_at
    }

    BATTLES {
        bigint id PK
        bigint player1_id FK "Host"
        bigint player2_id FK "Opponent"
        bigint deck_id FK "Battle Deck"
        enum status "WAITING, IN_PROGRESS, COMPLETED, CANCELLED"
        bigint winner_id FK "Nullable"
        float player1_score
        float player2_score
        int player1_time_seconds
        int player2_time_seconds
        timestamp created_at
        timestamp completed_at
    }

    BATTLE_DETAILS {
        bigint id PK
        bigint battle_id FK
        bigint card_id FK
        text player1_answer
        text player2_answer
        boolean player1_is_correct
        boolean player2_is_correct
        int player1_time_ms
        int player2_time_ms
    }

    BADGES {
        bigint id PK
        string code UK "STREAK_7_DAYS, BATTLE_WINNER_10"
        string name "Thánh Chăm Chỉ, Vua Đấu Trí"
        text description
        string icon_url
        int points "Bonus Points"
    }

    USER_BADGES {
        bigint id PK
        bigint user_id FK
        bigint badge_id FK
        timestamp unlocked_at
    }

    LEADERBOARD_SNAPSHOTS {
        bigint id PK
        bigint user_id FK
        enum period_type "WEEKLY, MONTHLY, ALL_TIME"
        string period_key "2026-W32, 2026-08, GLOBAL"
        int total_points
        int rank_position
        timestamp updated_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        enum type "BATTLE_INVITE, STREAK_REMINDER, BADGE_UNLOCKED, SYSTEM"
        string title
        text message
        string target_url
        boolean is_read "Default false"
        timestamp created_at
    }

    SPRING_AI_CHAT_MEMORY {
        string conversation_id PK "UUID (varchar 36)"
        bigint sequence_id PK "Message Order Index"
        text content "Message Body"
        enum type "USER, ASSISTANT, SYSTEM, TOOL"
        timestamp timestamp "Creation Time"
    }
```

---

## 2. Mô Tả Chi Tiết Các Bảng & Mục Đích Các Trường

### 1. Phân Hệ Người Dùng & Cài Đặt Cá Nhân

#### 🟢 Bảng USERS (Quản lý tài khoản người dùng)
- **id** (bigint - PK): Mã định danh duy nhất cho mỗi tài khoản.
- **email** (string - UK): Email người dùng, dùng làm tên đăng nhập (duy nhất).
- **password_hash** (string): Mật khẩu đã được mã hóa an toàn (BCrypt).
- **full_name** (string): Họ và tên hiển thị của học viên.
- **avatar_url** (string): Link ảnh đại diện cá nhân.
- **role** (enum): Phân quyền (ROLE_USER: Học viên, ROLE_ADMIN: Quản trị viên).
- **status** (enum): Trạng thái tài khoản (ACTIVE: Đang hoạt động, INACTIVE: Chưa kích hoạt email, BLOCKED: Bị khóa).
- **created_at / updated_at**: Thời điểm tạo tài khoản và thời điểm cập nhật thông tin gần nhất.

#### 🟢 Bảng REFRESH_TOKENS (Quản lý đăng nhập đa thiết bị)
- **id** (bigint - PK): Mã bản ghi token.
- **user_id** (bigint - FK): Cho biết token này thuộc về người dùng nào.
- **token** (string - UK): Chuỗi Refresh Token JWT duy nhất cấp cho thiết bị.
- **device_info** (string): Thông tin thiết bị (ví dụ: "Chrome trên Windows", "App Android"), hỗ trợ người dùng đăng xuất từ xa từng thiết bị.
- **expires_at**: Thời gian token hết hạn.

#### 🟢 Bảng USER_SETTINGS (Cài đặt mục tiêu học tập)
- **user_id** (bigint - FK, UK): Nối 1-1 với người dùng.
- **daily_new_cards_goal** (int): Mục tiêu số từ mới muốn học mỗi ngày (ví dụ: 10 từ/ngày).
- **daily_review_cards_goal** (int): Mục tiêu số từ cũ cần ôn tập mỗi ngày (ví dụ: 30 từ/ngày).
- **reminder_time** (time): Khung giờ cài đặt để hệ thống nhắc nhở học bài (ví dụ: 20:00).
- **sound_enabled** (boolean): Bật/Tắt âm thanh hiệu ứng & tự động phát âm khi lật thẻ.

#### 🟢 Bảng USER_STREAKS (Chuỗi ngày học liên tục)
- **user_id** (bigint - FK, UK): Nối 1-1 với người dùng.
- **current_streak** (int): Số ngày học liên tiếp hiện tại (tăng +1 mỗi ngày học, về 0 nếu bỏ học 1 ngày).
- **longest_streak** (int): Kỷ lục chuỗi ngày học dài nhất từng đạt được (để vinh danh/trao danh hiệu).
- **last_active_date** (date): Ngày gần nhất học viên có học bài (dùng để kiểm tra có bị đứt chuỗi hay không).

#### 🟢 Bảng USER_ACTIVITIES (Nhật ký hoạt động - Activity Heatmap)
- **user_id** (bigint - FK): Xác định người dùng.
- **activity_date** (date): Ngày ghi nhận hoạt động (ví dụ: 2026-08-05).
- **cards_reviewed_count** (int): Tổng số lượt thẻ đã ôn trong ngày.
- **quizzes_completed_count** (int): Tổng số bài quiz đã làm trong ngày. *(Dữ liệu bảng này dùng để vẽ biểu đồ ô màu đóng góp / học tập tương tự GitHub)*.

---

### 2. Phân Hệ Bộ Thẻ & Từ Vựng

#### 🔵 Bảng DECKS (Quản lý các Bộ thẻ Flashcard)
- **id** (bigint - PK): Mã bộ thẻ.
- **user_id** (bigint - FK): Tác giả / Người sở hữu bộ thẻ.
- **parent_deck_id** (bigint - FK, Nullable): Lưu ID bộ thẻ gốc nếu đây là bản sao (Fork/Clone) từ bộ thẻ của người khác.
- **title / description**: Tiêu đề và mô tả bộ thẻ (ví dụ: "TOEIC 600 Từ Căn Bản").
- **cover_image_url** (string): Ảnh bìa trang trí bộ thẻ.
- **source_language / target_language**: Ngôn ngữ gốc ("en") và Ngôn ngữ đích ("vi").
- **visibility** (enum): PRIVATE (chỉ mình học) hoặc PUBLIC (chia sẻ cho cộng đồng).
- **is_official** (boolean): Đánh dấu bộ thẻ chuẩn do hệ thống biên soạn.
- **total_cards** (int): Tổng số thẻ từ vựng có trong bộ.
- **fork_count** (int): Số lượt người dùng khác đã sao chép bộ thẻ này.
- **is_deleted** (boolean): Xóa mềm bộ thẻ.

#### 🔵 Bảng TAGS & DECK_TAGS (Nhãn phân loại)
- **TAGS.name / TAGS.slug**: Tên nhãn (chủ đề) như "TOEIC", "IELTS", "IT" và đường dẫn tĩnh slug (toeic, ielts).
- **DECK_TAGS**: Bảng trung gian lưu cặp (deck_id, tag_id) để 1 bộ thẻ có thể gắn nhiều nhãn.

#### 🔵 Bảng CARDS (Chi tiết thẻ từ vựng trong bộ)
- **deck_id** (bigint - FK): Bộ thẻ chứa thẻ bài này.
- **word** (string): Từ vựng chính (ví dụ: "Algorithm").
- **phonetic** (string): Phiên âm IPA (ví dụ: "/ˈæl.ɡə.rɪ.ðəm/").
- **part_of_speech** (string): Từ loại (Noun, Verb, Adjective...).
- **meaning**: Định nghĩa / Nghĩa tiếng Việt.
- **definition_en**: Giải thích nghĩa bằng tiếng Anh.
- **example_sentence / example_meaning**: Câu ví dụ tiếng Anh và bản dịch nghĩa tiếng Việt.
- **image_url / audio_url**: Ảnh minh họa và file âm thanh phát âm bản ngữ.
- **note**: Ghi chú cá nhân (mẹo nhớ, từ đồng nghĩa).
- **position**: Thứ tự xuất hiện của thẻ trong bộ bài.

---

### 3. Phân Hệ Thuật Toán Lặp Lại Ngắt Quãng (SRS)

#### 🟣 Bảng USER_CARD_SRS (Tiến độ ghi nhớ theo thuật toán SM-2)
- **user_id / card_id** (FK): Xác định học viên nào đang học từ vựng cụ thể nào.
- **status** (enum): Mức độ ghi nhớ (NEW: Từ mới, LEARNING: Đang học, MASTERED: Đã thuộc lòng).
- **easiness_factor** (float - Mặc định 2.5): Hệ số độ dễ (EF). Bấm từ "Khó" EF sẽ giảm, bấm "Dễ" EF sẽ tăng.
- **repetitions** (int): Số lần liên tiếp học viên trả lời đúng từ này.
- **interval** (int): Khoảng cách số ngày cho đến lần ôn tiếp theo.
- **last_reviewed_at**: Thời điểm học viên ôn từ này gần nhất.
- **next_review_date** (date - Indexed): Ngày cần ôn tập tiếp theo. Được đánh chỉ mục Index để hệ thống query cực nhanh danh sách từ vựng cần học hôm nay.

---

### 4. Phân Hệ Bài Kiểm Tra (Quiz)

#### 🟡 Bảng QUIZ_RESULTS (Kết quả tổng hợp bài quiz)
- **user_id** (bigint - FK): Người làm bài quiz.
- **scope** (enum): Phạm vi tạo bài quiz (DECK: theo bộ thẻ, TAG: theo chủ đề, ALL: tất cả từ vựng).
- **total_questions / correct_answers**: Tổng số câu hỏi và số câu trả lời đúng.
- **score / accuracy_rate**: Tổng điểm và tỷ lệ phần trăm chính xác (ví dụ: 85.5%).
- **time_spent_seconds**: Thời gian hoàn thành bài kiểm tra (giây).

#### 🟡 Bảng QUIZ_DETAILS (Chi tiết từng câu hỏi trong bài quiz)
- **quiz_result_id** (bigint - FK): Thuộc về bài quiz tổng hợp nào.
- **card_id** (bigint - FK): Câu hỏi này được sinh ra từ thẻ bài nào.
- **question_type** (enum): Dạng câu hỏi (MULTIPLE_CHOICE: Trắc nghiệm, FILL_IN_BLANK: Điền từ, MATCHING: Nối từ, LISTENING: Nghe).
- **user_answer**: Đáp án học viên đã nhập/chọn.
- **correct_answer**: Đáp án đúng của hệ thống.
- **is_correct** (boolean): Trạng thái ĐÚNG (true) hay SAI (false).
- **score_earned**: Điểm đạt được riêng cho câu hỏi này.

---

### 5. Phân Hệ Đấu Kháng 1 vs 1 (PvP Battle Module)

#### 🔴 Bảng BATTLES (Quản lý phòng đấu 1vs1)
- **id** (bigint - PK): Mã trận đấu.
- **player1_id** (bigint - FK): Người phát động thách đấu (Host phòng).
- **player2_id** (bigint - FK): Đối thủ tham gia thi đấu (Opponent).
- **deck_id** (bigint - FK): Bộ thẻ từ vựng được dùng làm bộ đề thi đấu.
- **status** (enum): Trạng thái trận đấu (`WAITING`: Đang chờ đối thủ, `IN_PROGRESS`: Đang thi đấu, `COMPLETED`: Đã kết thúc, `CANCELLED`: Đã hủy).
- **winner_id** (bigint - FK, Nullable): ID người chiến thắng (null nếu hòa).
- **player1_score / player2_score** (float): Tổng điểm của Người 1 và Người 2.
- **player1_time_seconds / player2_time_seconds** (int): Tổng thời gian hoàn thành bài của từng người.
- **created_at / completed_at** (timestamp): Thời điểm tạo phòng và thời điểm trận đấu kết thúc.

#### 🔴 Bảng BATTLE_DETAILS (Chi tiết từng lượt đối đầu câu hỏi)
- **id** (bigint - PK): Mã lượt thi đấu.
- **battle_id** (bigint - FK): Thuộc về trận đấu 1vs1 nào (`BATTLES.id`).
- **card_id** (bigint - FK): Thẻ từ vựng được hỏi trong lượt này.
- **player1_answer / player2_answer** (text): Câu trả lời của Người 1 và Người 2.
- **player1_is_correct / player2_is_correct** (boolean): Trạng thái trả lời Đúng hay Sai của từng người.
- **player1_time_ms / player2_time_ms** (int): Tốc độ trả lời (mili giây - ms) của từng người. *(Dùng để phân định người thắng lượt đó khi cả 2 cùng trả lời đúng)*.

---

### 6. Phân Hệ Bảng Xếp Hạng & Huy Hiệu (Leaderboards & Badges)

#### 🏆 Bảng BADGES (Danh mục các Huy hiệu / Thành tựu)
- **id** (bigint - PK): Mã huy hiệu.
- **code** (string - UK): Mã định danh huy hiệu (vd: `STREAK_7_DAYS`, `QUIZ_MASTER`, `BATTLE_WINNER_10`, `VOCAB_1000`).
- **name** (string): Tên danh hiệu hiển thị (vd: "Thánh Chăm Chỉ 7 Ngày", "Vua Đấu Trí 1vs1").
- **description** (text): Mô tả điều kiện đạt huy hiệu (vd: "Thắng 10 trận đấu kháng 1vs1").
- **icon_url** (string): Đường dẫn ảnh biểu tượng huy hiệu.
- **points** (int): Số điểm thưởng kinh nghiệm cộng thêm khi mở khóa huy hiệu này.

#### 🏆 Bảng USER_BADGES (Huy hiệu người dùng đã mở khóa)
- **id** (bigint - PK): Mã mở khóa.
- **user_id** (bigint - FK): Học viên sở hữu huy hiệu.
- **badge_id** (bigint - FK): Huy hiệu được mở khóa.
- **unlocked_at** (timestamp): Thời điểm đạt được huy hiệu.

#### 🏆 Bảng LEADERBOARD_SNAPSHOTS (Bảng xếp hạng tích điểm)
- **id** (bigint - PK): Mã bản ghi xếp hạng.
- **user_id** (bigint - FK): Học viên trong bảng xếp hạng.
- **period_type** (enum): Chu kỳ xếp hạng (`WEEKLY`: Tuần, `MONTHLY`: Tháng, `ALL_TIME`: Tổng tích lũy).
- **period_key** (string): Khóa mốc thời gian (vd: `2026-W32`, `2026-08`, `GLOBAL`).
- **total_points** (int): Tổng điểm kinh nghiệm tích lũy (từ việc học SRS, làm Quiz, Thắng 1vs1, Đạt huy hiệu).
- **rank_position** (int): Thứ hạng hiển thị trên Bảng xếp hạng (Top 1, Top 2, Top 3...).
- **updated_at** (timestamp): Thời điểm cập nhật bảng xếp hạng.

---

### 7. Phân Hệ Thông Báo (Notifications System)

#### 🔔 Bảng NOTIFICATIONS (Thông báo hệ thống & Thách đấu)
- **id** (bigint - PK): Mã thông báo.
- **user_id** (bigint - FK): Người nhận thông báo.
- **type** (enum): Loại thông báo (`BATTLE_INVITE`: Lời mời thi đấu 1vs1, `STREAK_REMINDER`: Nhắc nhở giữ chuỗi Streak, `BADGE_UNLOCKED`: Mở khóa huy hiệu mới, `SYSTEM`: Thông báo từ hệ thống).
- **title** (string): Tiêu đề thông báo (vd: *"Nguyễn Văn A đã thách đấu 1vs1 với bạn!"*).
- **message** (text): Nội dung chi tiết thông báo.
- **target_url** (string): Đường dẫn chuyển hướng khi nhấp vào thông báo (vd: `/battles/room/102`).
- **is_read** (boolean - Mặc định `false`): Trạng thái đã đọc hay chưa.
- **created_at** (timestamp): Thời điểm phát thông báo.

---

### 8. Phân Hệ Lịch Sử Hội Thoại AI (Spring AI Chat Memory Module)

#### 🤖 Bảng SPRING_AI_CHAT_MEMORY (Lịch sử chat với Trợ lý AI)
- **conversation_id** (varchar(36) - PK): Chuỗi UUID định danh phiên hội thoại giữa học viên và AI Agent.
- **sequence_id** (bigint - PK): Thứ tự vị trí câu thoại trong phiên hội thoại (tăng dần 1, 2, 3...).
- **content** (text): Nội dung câu thoại chi tiết (câu hỏi của người dùng hoặc câu trả lời của AI).
- **type** (enum): Loại vai trò đối thoại (`USER`: Học viên, `ASSISTANT`: AI Tutor, `SYSTEM`: Prompt hệ thống, `TOOL`: Kết quả gọi Tool/Function).
- **timestamp** (timestamp): Thời điểm phát sinh tin nhắn hội thoại.
