package com.fivetuat.fsoftprojectserver.enums;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum SuccessCode {
    AUTHENTICATED_GOOGLE_SUCCESS(200, "Đăng nhập thành công với Google", HttpStatus.OK),
    CREATED_USER_SUCCESS(201, "Tạo thành công người dùng", HttpStatus.CREATED),
    AUTHENTICATED_SUCCESS(200, "Đăng nhập thành công", HttpStatus.OK),
    REFRESH_TOKEN_SUCCESS(200, "Refresh token thành công", HttpStatus.OK),
    LOGOUT_SUCCESS(200, "Đăng xuất thành công", HttpStatus.OK),
    INTROSPECT_TOKEN_SUCCESS(200, "Introspect token thành công", HttpStatus.OK),
    UPDATE_PROFILE_SUCCESS(200, "Cập nhật thành công", HttpStatus.OK),
    UPDATE_AVATAR_SUCCESS(200, "Cập nhật avatar thành công", HttpStatus.OK),
    GET_PROFILE_SUCCESS(200, "Lấy thông tin thành công", HttpStatus.OK),
    UPLOAD_POST_THUMBNAIL_SUCCESS(200, "Cập nhật ảnh bài viết thành công", HttpStatus.OK),
    GET_POSTS_BY_STATUS_SUCCESS(200, "Lấy danh sách bài viết theo trạng thái thành công", HttpStatus.OK),
    GET_POST_SUCCESS(200, "Lấy bài viết thành công", HttpStatus.OK),
    UPDATED_POST_SUCCESS(200, "Cập nhật bài viết thành công", HttpStatus.OK),
    DELETE_POST_SUCCESS(200, "Xóa bài viết thành công", HttpStatus.OK),
    CREATE_COURSE_SUCCESS(201, "Tạo khóa học thành công", HttpStatus.CREATED),
    GET_COURSE_SUCCESS(200, "Lấy khóa học thành công", HttpStatus.OK),
    DELETE_COURSE_SUCCESS(200, "Xóa khóa học thành công", HttpStatus.OK),
    UPDATE_COURSE_SUCCESS(200, "Cập nhật khóa học thành công", HttpStatus.OK),
    UPDATE_STATUS_COURSE_SUCCESS(200, "Cập nhật trạng thái khóa học thành công", HttpStatus.OK),
    GET_COURSES_SUCCESS(200, "Lấy danh sách khóa học thành công", HttpStatus.OK),
    ENROLL_COURSE_SUCCESS(200, "Đăng ký khóa học thành công", HttpStatus.OK),
    CREATE_PAYMENT(201, "Tạo thanh toán thành công", HttpStatus.CREATED),
    VN_PAY_IPN_SUCCESS(200, "Xử lý thanh toán thành công", HttpStatus.OK),
    PAYMENT_SUCCESS(200, "Thanh toán thành công", HttpStatus.OK),
    REFUND_SUCCESS(200, "Hoàn tiền thành công", HttpStatus.OK),
    CHECK_TRANSACTION_STATUS_SUCCESS(200, "Kiểm tra trạng thái giao dịch thành công", HttpStatus.OK),
    GET_ORDERS_SUCCESS(200, "Lấy danh sách đơn hàng thành công", HttpStatus.OK),
    UPLOAD_FILE_SUCCESS(200, "Cập nhật ảnh thành công", HttpStatus.OK),
    GET_LIST_COURSE_ENROLLMENT_SUCCESS(200, "Lấy danh sách khóa học ghi danh thành công", HttpStatus.OK),
    GET_DASHBOARD_STATS_SUCCESS(200, "Lấy thống kê dashboard thành công", HttpStatus.OK),
    GET_USERS_SUCCESS(200, "Lấy danh sách người dùng thành công", HttpStatus.OK),
    DELETE_USER_SUCCESS(200, "Xóa người dùng thành công", HttpStatus.OK),
    DELETE_ACCOUNT_SUCCESS(200, "Xóa tài khoản thành công", HttpStatus.OK),
    FORGOT_PASSWORD_SUCCESS(200, "Gửi mã OTP thành công", HttpStatus.OK),
    RESET_PASSWORD_SUCCESS(200, "Đặt lại mật khẩu thành công", HttpStatus.OK),
    GENERATED_QUIZ_SUCCESS(200, "", HttpStatus.OK),
    CREATED_EMERGENCY_SUCCESS(200, "Tạo yêu cầu khẩn cấp thành công", HttpStatus.OK),
    GET_NEARBY_SUCCESS(200, "Lấy danh sách yêu cầu khẩn cấp gần nhất thành công", HttpStatus.OK),
    CREATE_DECK_SUCCESS(201, "Tạo bộ thẻ thành công", HttpStatus.CREATED),
    GET_DECK_SUCCESS(200, "Lấy bộ thẻ thành công", HttpStatus.OK),
    UPDATE_DECK_SUCCESS(200, "Cập nhật bộ thẻ thành công", HttpStatus.OK),
    DELETE_DECK_SUCCESS(200, "Xóa bộ thẻ thành công", HttpStatus.OK),
    GET_DECKS_SUCCESS(200, "Lấy danh sách bộ thẻ thành công", HttpStatus.OK),
    DECK_VISIBILITY_UPDATED(200, "Cập nhật trạng thái thẻ thành công", HttpStatus.OK),
    DECK_FORKED(200, "Sao chép thẻ thành công", HttpStatus.OK),
    DECK_SHARED(200, "Deck shared successfully", HttpStatus.OK),
    CREATE_TAG_SUCCESS(201, "Tạo nhãn thành công", HttpStatus.CREATED),
    GET_TAGS_SUCCESS(200, "Lấy danh sách nhãn thành công", HttpStatus.OK),
    UPDATE_TAG_SUCCESS(200, "Cập nhật nhãn thành công", HttpStatus.OK),
    DELETE_TAG_SUCCESS(200, "Xóa nhãn thành công", HttpStatus.OK),
    UPDATE_CARD_SUCCESS(200, "Cập nhật thẻ ghi nhớ thành công", HttpStatus.OK),
    CREATE_CARD_SUCCESS(201, "Tạo thẻ ghi nhớ thành công", HttpStatus.CREATED),
    GET_CARDS_SUCCESS(200, "Lấy danh sách thẻ ghi nhớ thành công", HttpStatus.OK),
    GET_CARD_SUCCESS(200, "Lấy thông tin thẻ ghi nhớ thành công", HttpStatus.OK),
    DELETE_CARD_SUCCESS(200, "Xóa thẻ ghi nhớ thành công", HttpStatus.OK),
    CARD_CHANGED_SINCE_SUCCESS(200, "Lấy danh sách thẻ đã thay đổi thành công", HttpStatus.OK),
    CARD_IDS_SUCCESS(200, "Lấy danh sách ID thẻ thành công", HttpStatus.OK),
    GET_STREAK_SUCCESS(200, "Lấy thông tin chuỗi ngày học thành công", HttpStatus.OK),
    GET_ACTIVITIES_SUCCESS(200, "Lấy danh sách nhật ký hoạt động thành công", HttpStatus.OK),
    GET_SHARED_USERS_SUCCESS(200, "Lấy danh sách người được chia sẻ thành công", HttpStatus.OK),
    UPDATE_ROLE_DECK_SUCCESS(200, "Cập nhật quyền truy cập thành công", HttpStatus.OK);

    final int status;
    final String message;
    final HttpStatus httpStatus;

    SuccessCode(int status, String message, HttpStatus httpStatus) {
        this.status = status;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
