package com.fivetuat.fsoftprojectserver.enums;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    EMAIL_OR_PHONE_EXISTS(400, "Email or Phone already exists", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(400, "Validation error", HttpStatus.BAD_REQUEST),
    GENERATED_TOKEN_FAILED(401, "Generated token faild", HttpStatus.BAD_REQUEST),
    VERIFY_TOKEN_FAILED(402, "Verify token faild", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_FAILED(207, "Authentication failed", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(403, "Invalid key", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_NOT_FOUND(404, "Refresh token not found", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED(405, "Refresh token expired", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_FAILED(406, "Refresh token was used or expired", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN(407, "Invalid access token", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(408, "You do not have permission to access this resource", HttpStatus.UNAUTHORIZED),
    PROFILE_NOT_FOUND(409, "Profile not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(410, "Category not found", HttpStatus.NOT_FOUND),
    UPLOAD_FILE_FAILED(411, "Upload file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_NOT_FOUND(412, "Post not found", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND(413, "Course not found", HttpStatus.NOT_FOUND),
    ALREADY_ENROLLED(414, "Already enrolled", HttpStatus.CONFLICT),
    COURSE_IS_NOT_FREE(415, "Course is not free", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(416, "Order not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(417, "User not found", HttpStatus.NOT_FOUND),
    INVALID_OTP(418, "Invalid OTP", HttpStatus.BAD_REQUEST),
    DECK_NOT_FOUND(404, "Deck not found", HttpStatus.NOT_FOUND),
    DECK_NOT_PUBLIC(420, "Only public decks can be forked", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(403, "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    DECK_NOT_SHARED(420, "Only SHARED decks can be SHARE", HttpStatus.BAD_REQUEST),
    DECK_ALREADY_FORKED(426, "You have already forked this deck", HttpStatus.CONFLICT),
    DECK_ALREADY_SHARED(427, "Deck already shared with this user", HttpStatus.CONFLICT),
    CANNOT_FORK_OWN_DECK(436, "Cannot fork your own deck", HttpStatus.BAD_REQUEST),
    FORKED_DECK_CANNOT_BE_PUBLIC(437, "Forked deck cannot be set to public", HttpStatus.BAD_REQUEST),

    //tanvy Mã lỗi bổ sung cho Module Card:
    TAG_NOT_FOUND(424, "Tag not found", HttpStatus.NOT_FOUND),
    TAG_ALREADY_EXISTS(425, "Tag already exists", HttpStatus.CONFLICT),
    CARD_NOT_FOUND(420, "Card not found", HttpStatus.NOT_FOUND),
    WORD_REQUIRED(421, "Word is required", HttpStatus.BAD_REQUEST),
    MEANING_REQUIRED(422, "Meaning is required", HttpStatus.BAD_REQUEST),
    DECK_ID_REQUIRED(423, "Deck ID is required", HttpStatus.BAD_REQUEST),
    TAG_NAME_EXISTS(425, "Tag name already exists", HttpStatus.BAD_REQUEST),
    CANNOT_SHARE_WITH_YOURSELF(426, "Cannot share with yourself", HttpStatus.BAD_REQUEST), 
    AI_SEARCH_ERROR(428, "Lỗi tìm kiếm AI", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_CHAT_ERROR(429, "Lỗi hỏi đáp AI", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_QUIZ_ERROR(430, "Lỗi sinh bài tập AI", HttpStatus.INTERNAL_SERVER_ERROR),
    DICTIONARY_LOOKUP_ERROR(431, "Lỗi tra từ điển", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_ERROR(432, "Lỗi đọc file", HttpStatus.INTERNAL_SERVER_ERROR),
    URL_READ_ERROR(433, "Lỗi đọc URL", HttpStatus.INTERNAL_SERVER_ERROR),
    AI_EXTRACTION_ERROR(434, "Lỗi trích xuất AI", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_CARDS(435, "Không đủ thẻ để tạo bài tập", HttpStatus.BAD_REQUEST),
    AI_SERVICE_UNAVAILABLE(436, "Hệ thống AI đang khởi động hoặc nạp dữ liệu, vui lòng thử lại sau giây lát", HttpStatus.SERVICE_UNAVAILABLE),
    AI_LOOKUP_ERROR(437, "Lỗi tra từ vựng bằng AI", HttpStatus.INTERNAL_SERVER_ERROR),
    // --- Word Blast multiplayer room ---
    ROOM_NOT_FOUND(440, "Room not found", HttpStatus.NOT_FOUND),
    ROOM_FULL(441, "Room is full (max 20 players)", HttpStatus.BAD_REQUEST),
    ROOM_ALREADY_STARTED(442, "Room already started or already finished", HttpStatus.BAD_REQUEST),
    NOT_ROOM_HOST(443, "Only the host can perform this action", HttpStatus.FORBIDDEN),
    PLAYER_NOT_IN_ROOM(444, "You are not a member of this room", HttpStatus.FORBIDDEN),
    NOT_ENOUGH_PLAYERS(445, "Need at least 2 players to start", HttpStatus.BAD_REQUEST),
    INVALID_QUESTION_COUNT(446, "Số lượng câu hỏi phải lớn hơn 0 và không vượt quá tổng số từ của các bộ thẻ", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(450, "Notification not found", HttpStatus.NOT_FOUND),

    // Community Posts
    COMMUNITY_POST_NOT_FOUND(450, "Bài viết cộng đồng không tồn tại", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(451, "Bình luận không tồn tại", HttpStatus.NOT_FOUND),
    ALREADY_LIKED(452, "Bạn đã thích bài viết này rồi", HttpStatus.CONFLICT),
    NOT_LIKED(453, "Bạn chưa thích bài viết này", HttpStatus.BAD_REQUEST),
    COMMUNITY_POST_ACCESS_DENIED(454, "Bạn không có quyền chỉnh sửa hoặc xóa bài viết này", HttpStatus.FORBIDDEN),
    COMMENT_ACCESS_DENIED(455, "Bạn không có quyền xóa bình luận này", HttpStatus.FORBIDDEN),
    REJECTION_REASON_REQUIRED(456, "Lý do từ chối không được để trống khi từ chối bài viết", HttpStatus.BAD_REQUEST),
    POST_NOT_APPROVED(457, "Bài viết chưa được phê duyệt nên không thể tương tác", HttpStatus.FORBIDDEN),
    QUIZ_RESULT_NOT_FOUND(458, "Kết quả bài kiểm tra Quiz không tồn tại", HttpStatus.NOT_FOUND),
    DECK_REJECTION_REASON_REQUIRED(459, "Lý do từ chối không được để trống khi từ chối bộ bài", HttpStatus.BAD_REQUEST),
    DECK_NOT_PENDING(460, "Bộ bài không nằm trong hàng chờ duyệt", HttpStatus.BAD_REQUEST),
    DECK_ALREADY_PENDING_PUBLIC(461, "Bộ bài đang chờ duyệt, vui lòng chờ kết quả kiểm duyệt", HttpStatus.CONFLICT),
    PENDING_ACTION_NOT_FOUND(462, "Không tìm thấy hành động chờ xác nhận hoặc đã được xử lý", HttpStatus.BAD_REQUEST),
    PENDING_ACTION_EXPIRED(463, "Hành động chờ xác nhận đã hết hạn, vui lòng thực hiện lại", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS(429, "Bạn đã vượt quá số lượng truy cập cho phép, vui lòng thử lại sau giây lát", HttpStatus.TOO_MANY_REQUESTS);
    int status;
    String message;
    HttpStatus httpStatus;

    ErrorCode(int status, String message, HttpStatus httpStatus) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}