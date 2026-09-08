package com.fivetuat.fsoftprojectserver.services.support;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminGameRecordResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminUserResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.TagResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AdminDashboardService;
import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
import com.fivetuat.fsoftprojectserver.services.AdminGameService;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import com.fivetuat.fsoftprojectserver.services.TagService;
import com.fivetuat.fsoftprojectserver.utils.PageUtils;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAiTools {

    AdminDeckService adminDeckService;
    AdminUserService adminUserService;
    AdminDashboardService adminDashboardService;
    TagService tagService;
    AdminGameService adminGameService;
    PendingActionStore pendingActionStore;

    @Tool(description = "Thống kê tổng quan hệ thống: tổng users, decks, cards, lượt chơi game, official decks, public decks. KHÔNG có tham số — gọi trống, không truyền page/size hay bất kỳ tham số nào.")
    public String dashboardStats() {
        var s = adminDashboardService.getDashboardStats();
        return String.format(
                "Tổng users: %d | Tổng decks: %d | Tổng cards: %d | Lượt chơi game: %d | Decks chính thức: %d | Decks công khai: %d",
                s.getTotalUsers(), s.getTotalDecks(), s.getTotalCards(),
                s.getTotalGameRecords(), s.getTotalOfficialDecks(), s.getTotalPublicDecks());
    }

    @Tool(description = "Tìm kiếm và liệt kê người dùng theo keyword (tùy chọn, truyền rỗng nếu không lọc), phân trang page từ 1")
    public String users(String keyword, int page, int size) {
        PageResponse<AdminUserResponse> users =
                adminUserService.getAllUsers(keyword, null, PageUtils.createPageable(page, size));
        if (users.getContent().isEmpty()) return "Không tìm thấy người dùng nào.";
        StringBuilder sb = new StringBuilder("Tổng " + users.getTotalElements() + " người dùng:\n");
        users.getContent().forEach(u -> sb.append(String.format(
                "- id=%s | %s | %s | roles=%s%n",
                u.getId(), u.getFullName(), u.getEmail(), u.getRoles())));
        return sb.toString();
    }

    @Tool(description = "Xem chi tiết một người dùng theo userId dạng UUID")
    public String userDetail(String userId) {
        AdminUserResponse u = adminUserService.getUserById(UUID.fromString(userId));
        return String.format("id=%s | %s | %s | phone=%s | roles=%s",
                u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRoles());
    }

    @Tool(description = "Liệt kê các bộ bài đang chờ duyệt công khai (pendingPublic=true), phân trang page từ 1")
    public String pendingDecks(int page, int size) {
        PageResponse<DeckResponse> decks =
                adminDeckService.getPendingPublicDecks(PageUtils.createPageable(page, size));
        if (decks.getContent().isEmpty()) return "Không có bộ bài nào đang chờ duyệt.";
        StringBuilder sb = new StringBuilder("Tổng " + decks.getTotalElements() + " bộ bài chờ duyệt:\n");
        decks.getContent().forEach(d -> sb.append(String.format(
                "- id=%d | '%s' | chủ: %s | %d thẻ%n",
                d.getId(), d.getTitle(), d.getProfileId(), d.getTotalCards())));
        return sb.toString();
    }

    @Tool(description = "Liệt kê tất cả bộ bài, lọc theo keyword (tùy chọn, truyền rỗng nếu không lọc), phân trang page từ 1")
    public String allDecks(String keyword, int page, int size) {
        PageResponse<DeckResponse> decks =
                adminDeckService.getAllDecks(keyword, null, null, PageUtils.createPageable(page, size));
        if (decks.getContent().isEmpty()) return "Không tìm thấy bộ bài nào.";
        StringBuilder sb = new StringBuilder("Tổng " + decks.getTotalElements() + " bộ bài:\n");
        decks.getContent().forEach(d -> sb.append(String.format(
                "- id=%d | '%s' | visibility=%s | official=%s | chủ: %s%n",
                d.getId(), d.getTitle(), d.getVisibility(), d.isOfficial(), d.getProfileId())));
        return sb.toString();
    }

    @Tool(description = "Liệt kê tất cả nhãn (tags) của hệ thống. KHÔNG có tham số — gọi trống, không truyền tham số nào.")
    public String tags() {
        return tagService.getAllTags().stream()
                .map(t -> t.getId() + ":" + t.getName())
                .collect(Collectors.joining(", ", "Các nhãn: ", ""));
    }

    @Tool(description = "Liệt kê các lượt chơi game gần đây, phân trang page từ 1")
    public String gameRecords(int page, int size) {
        PageResponse<AdminGameRecordResponse> records =
                adminGameService.getAllGameRecords(null, null, PageUtils.createPageable(page, size));
        if (records.getContent().isEmpty()) return "Không có lượt chơi game nào.";
        StringBuilder sb = new StringBuilder("Tổng " + records.getTotalElements() + " lượt chơi:\n");
        records.getContent().forEach(r -> sb.append("- ").append(r.toString()).append("\n"));
        return sb.toString();
    }

    @Tool(description = "SOẠN HÀNH ĐỘNG duyệt bộ bài lên public. KHÔNG thực thi ngay — cần admin xác nhận qua endpoint confirm.")
    public String requestApproveDeck(Long deckId) {
        return createPendingAction("requestApproveDeck",
                Map.of("deckId", deckId),
                "Duyệt bộ bài #" + deckId + " lên công khai");
    }

    @Tool(description = "SOẠN HÀNH ĐỘNG từ chối bộ bài lên public với lý do bắt buộc. KHÔNG thực thi ngay — cần admin xác nhận qua endpoint confirm.")
    public String requestRejectDeck(Long deckId, String reason) {
        return createPendingAction("requestRejectDeck",
                Map.of("deckId", deckId, "reason", reason),
                "Từ chối bộ bài #" + deckId + " với lý do: " + reason);
    }

    @Tool(description = "SOẠN HÀNH ĐỘNG xóa bộ bài. KHÔNG thực thi ngay — cần admin xác nhận qua endpoint confirm.")
    public String requestDeleteDeck(Long deckId) {
        return createPendingAction("requestDeleteDeck",
                Map.of("deckId", deckId),
                "Xóa bộ bài #" + deckId);
    }

    @Tool(description = "SOẠN HÀNH ĐỘNG đặt hoặc hủy đánh dấu bộ bài chính thức (official). KHÔNG thực thi ngay — cần admin xác nhận qua endpoint confirm.")
    public String requestSetOfficial(Long deckId, boolean isOfficial) {
        return createPendingAction("requestSetOfficial",
                Map.of("deckId", deckId, "isOfficial", isOfficial),
                (isOfficial ? "Đặt" : "Hủy") + " chính thức cho bộ bài #" + deckId);
    }

    @Tool(description = "SOẠN HÀNH ĐỘNG cập nhật roles cho người dùng (rolesCsv ví dụ: 'USER,ADMIN'). KHÔNG thực thi ngay — cần admin xác nhận qua endpoint confirm.")
    public String requestUpdateUserRoles(String userId, String rolesCsv) {
        return createPendingAction("requestUpdateUserRoles",
                Map.of("userId", userId, "rolesCsv", rolesCsv),
                "Cập nhật roles [" + rolesCsv + "] cho user " + userId);
    }

    private String createPendingAction(String tool, Map<String, Object> params, String summary) {
        UUID adminId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                UUID.randomUUID(), tool, params, adminId, summary,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
        pendingActionStore.save(action);
        pendingActionStore.indexLatest(adminId, action.actionId());
        return "PENDING_CONFIRMATION: actionId=" + action.actionId()
                + "; Hành động đã được soạn nhưng CHƯA thực hiện. Nội dung: " + summary
                + ". Hãy trả lời admin kèm actionId để xác nhận hoặc hủy.";
    }
}