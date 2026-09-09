# 🤖 Admin AI Agent — Hướng dẫn Workflow chi tiết

> Tài liệu này giải thích cách hoạt động của trợ lý AI dành cho Admin trong hệ thống FSoft Flashcards — từ kiến trúc tổng thể, luồng xử lý từng request, đến chi tiết mỗi thành phần.

---

## 1. Tổng quan nhanh (Quick Summary)

Admin AI Agent là một **chatbot quản trị** cho phép admin tương tác với hệ thống bằng ngôn ngữ tự nhiên:

| Khả năng | Ví dụ |
|---|---|
| **Tra cứu dữ liệu thật (Q&A)** | *"Có bao nhiêu bộ bài đang chờ duyệt?"* → trả lời bằng số liệu live từ DB |
| **Soạn hành động nguy hiểm** | *"Từ chối bộ bài 12 vì spam"* → bot soạn hành động, **không thực thi ngay** |
| **Xác nhận trước khi chạy** | Admin gọi `/confirm` → hành động mới được thực thi thật |
| **Audit log** | Mọi hành động đã thực thi đều được ghi lại, tra cứu được |

**Triết lý an toàn:** AI **chỉ đọc** dữ liệu trực tiếp; mọi hành động phá hủy (duyệt/từ chối/xóa deck, đổi roles, set official) phải đi qua bước **xác nhận 2 bước** với token 1-lần-dùng, hết hạn 5 phút.

---

## 2. Kiến trúc tổng thể

```
┌──────────────────────────────────────────────────────────────┐
│                     Admin (Frontend/Swagger)                  │
│   POST /fsoft/admin/ai/chat        {"message": "..."}         │
└──────────────────────────┬───────────────────────────────────┘
                           │ (JWT ROLE_ADMIN)
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  AdminAiChatController          (@PreAuthorize ROLE_ADMIN)   │
│  /admin/ai/chat  |  /confirm  |  /cancel                     │
│  /chat/history   |  DELETE /history  |  /audit-logs          │
└──────────────────────────┬───────────────────────────────────┘
                           ▼
┌──────────────────────────────────────────────────────────────┐
│  AdminAiChatServiceImpl          (orchestrator)              │
│  ├─ ConversationId = adminProfileId (UUID)                   │
│  ├─ MessageChatMemoryAdvisor  → JDBC chat memory             │
│  └─ .tools(adminAiTools)      → function calling             │
└──────────┬──────────────────────────────────┬────────────────┘
           ▼                                  ▼
┌─────────────────────────┐      ┌──────────────────────────────┐
│ ReasoningSanitizing     │      │  AdminAiTools (@Tool)         │
│ ChatModel (wrapper)     │      │  7 read tools + 5 destructive│
│ → strip reasoningContent│      │  read → gọi service trực tiếp │
│ → delegate.call(prompt) │      │  write → PendingActionStore   │
└──────────┬──────────────┘      └──────────────┬───────────────┘
           ▼                                    ▼
┌─────────────────────────┐      ┌──────────────────────────────┐
│ OpenAiChatModel → Groq  │      │  PendingActionStore (Redis)   │
│ model: qwen3.8-27b      │      │  key: admin-ai:pending:{id}   │
│ spring.ai.openai.*      │      │  TTL: 6 phút (hạn 5 phút)     │
└─────────────────────────┘      └──────────────────────────────┘
                                            │ confirm
                                            ▼
                     ┌──────────────────────────────────────┐
                     │ AdminDeckService / AdminUserService  │
                     │ (thi thật) + AdminAiAuditLog (DB)    │
                     └──────────────────────────────────────┘
```

---

## 3. Các file quan trọng (bản đồ code)

| File | Vai trò |
|---|---|
| `controllers/AdminAiChatController.java` | 6 REST endpoints, chốt quyền ROLE_ADMIN |
| `services/AdminAiChatService.java` | Interface: `chat`, `confirm`, `cancel`, `getHistory`, `clearHistory`, `getAuditLogs` |
| `services/implement/AdminAiChatServiceImpl.java` | Orchestrator chính — mọi logic luồng |
| `services/support/AdminAiTools.java` | 12 tool function-calling mà AI được gọi |
| `services/support/PendingActionStore.java` | Lưu hành động chờ xác nhận vào Redis + index "latest per admin" |
| `configurations/MultiAiConfig.java` | Bean `adminAiClient` (Groq/qwen) + system prompt + model config |
| `configurations/ReasoningSanitizingChatModel.java` | Wrapper bọc model: gỡ `reasoningContent` khỏi response |
| `configurations/JacksonConfig.java` | Bean ObjectMapper (Boot 4 không auto-register Jackson 2) |
| `entities/AdminAiAuditLog.java` | Bảng `admin_ai_audit_logs` |
| `enums/PendingActionStatus.java` | `EXECUTED / CANCELLED / FAILED` |

---

## 4. Luồng 1: Chat hỏi đáp thông thường (Q&A)

**Request:** `POST /admin/ai/chat` với `{"message": "Cho biet tong so user trong he thong"}`

```
1. Controller → Security filter (JWT, ROLE_ADMIN) → AdminAiChatService.chat()

2. chat() tính conversationId = adminProfileId (UUID 36 ký tự)
   ⚠️ Lý do: bảng spring_ai_chat_memory có cột conversation_id VARCHAR(36).
   Bất kỳ tiền tố nào (admin-ai-...) đều gây lỗi "value too long".

3. Gọi adminAiClient.prompt()
       .tools(adminAiTools)                       ← đăng ký 12 tool
       .advisors(MessageChatMemoryAdvisor)        ← nạp + lưu lịch sử
       .advisors(a -> a.param(CONVERSATION_ID))   ← thread riêng từng admin
       .user(message).call().content()

4. BÊN TRONG ChatClient (ToolCallingAdvisor loop — tự động):
   Vòng 1: gửi [system, user] → Groq trả tool_calls (ví dụ dashboardStats)
   ├─ ToolCallingAdvisor thực thi tool → kết quả text
   ├─ Vòng 2: gửi lại [system, user, assistant(tool_calls), tool(result)]
   └─ Lặp cho tới khi model trả lời KHÔNG còn tool_calls → content cuối

5. ReasoningSanitizingChatModel bọc delegate:
   - Prompt đi qua nguyên trạng (không đụng options — tránh ClassCastException)
   - Response: nếu AssistantMessage metadata chứa "reasoningContent"
     → rebuild message KHÔNG key đó (Groq từ chối replay reasoning_content)

6. Response: AdminAiChatResponse {
       conversationId, reply, pendingAction (null), result (null)
   }
```

**Điểm hay:** Admin không cần biết tool nào tồn tại — model tự quyết định gọi tool dựa trên câu hỏi.

---

## 5. Luồng 2: Hành động nguy hiểm (2 bước: soạn → xác nhận)

### Bước A — Soạn hành động (POST /chat)

**Request:** `{"message": "Soan hanh dong tu choi bo bai so 12 voi ly do spam"}`

1. Model nhận câu hỏi → quyết định gọi tool `requestRejectDeck(deckId=12, reason="spam")`
2. Tool **KHÔNG đụng** vào `AdminDeckService` (chưa thực thi!). Thay vào đó:
   - Tạo `PendingAction`: `{actionId: UUID, tool, params, adminId, summary, createdAt, expiresAt(+5p)}`
   - Lưu Redis: `admin-ai:pending:{actionId}` (TTL Redis 6 phút > hạn 5 phút → còn thấy key để báo lỗi 463 đúng)
   - Lưu index: `admin-ai:pending-admin:{adminId}` → actionId (để chat response lấy được)
3. Tool trả về chuỗi `PENDING_CONFIRMATION: actionId=...` cho model
4. Model soạn câu trả lời tự nhiên kèm actionId, yêu cầu admin xác nhận
5. Service đọc `findLatestByAdmin(adminId)` → gắn `pendingAction` vào response

```json
{
  "reply": "Đã soạn hành động từ chối... Vui lòng xác nhận (actionId: 9e09...)",
  "pendingAction": {
    "actionId": "9e09b9bf-e3a2-...",
    "tool": "requestRejectDeck",
    "summary": "Từ chối bộ bài #12 với lý do: spam",
    "expiresAt": "2026-09-08T03:59:04"
  }
}
```

### Bước B — Xác nhận (POST /chat/confirm)

**Request:** `{"actionId": "9e09b9bf-..."}` — **bắt buộc admin soạn mới được xác nhận**

```
confirm(actionId):
  1. load PendingAction từ Redis          → thiếu → 462 PENDING_ACTION_NOT_FOUND
  2. check ownership (action.adminId == currentAdmin) → sai → 403 ACCESS_DENIED
  3. check expiresAt < now?               → hết hạn → 463 PENDING_ACTION_EXPIRED
     (+ xóa key Redis dơ)
  4. executeAction(action) — switch theo tool:
       requestApproveDeck      → adminDeckService.approvePublicDeck(id, true, null)
       requestRejectDeck       → adminDeckService.approvePublicDeck(id, false, reason)
       requestDeleteDeck       → adminDeckService.deleteDeck(id)
       requestSetOfficial      → adminDeckService.updateDeckOfficial(id, official)
       requestUpdateUserRoles  → adminUserService.updateUserRoles(userId, roles)
  5. Ghi audit log:
       - thành công     → status = EXECUTED
       - AppException   → status = FAILED (persist ngay vì KHÔNG có @Transactional
                          trên confirm — tránh rollback mất log), rồi rethrow
       - Exception khác → status = FAILED (resultSummary = tên exception)
  6. Xóa Redis key + clearLatestIndex (one-time use)
  7. Response: reply "Đã thực hiện: ..." + result = output service
```

### Bước C — Hủy (POST /chat/confirm/cancel)

Cùng các bước 1-3, nhưng thay thực thi bằng: ghi audit `CANCELLED` + xóa Redis.

---

## 6. Danh mục 12 Tool

### 6.1 Read-only (thực thi ngay, trả text cho model)

| Tool | Gọi vào | Mô tả |
|---|---|---|
| `dashboardStats()` | AdminDashboardService | Tổng users/decks/cards/game/official/public |
| `users(keyword, page, size)` | AdminUserService | Tìm + list users |
| `userDetail(userId)` | AdminUserService | Chi tiết 1 user |
| `pendingDecks(page, size)` | AdminDeckService | Decks chờ duyệt (pendingPublic=true) |
| `allDecks(keyword, page, size)` | AdminDeckService | Toàn bộ decks |
| `tags()` | TagService | Danh sách tag |
| `gameRecords(page, size)` | AdminGameService | Lượt chơi game |

### 6.2 Destructive — chỉ SOẠN, cần /confirm

| Tool | Hành động thật khi confirm | Audit action |
|---|---|---|
| `requestApproveDeck(deckId)` | approvePublicDeck(id, true, null) | APPROVE_DECK |
| `requestRejectDeck(deckId, reason)` | approvePublicDeck(id, false, reason) | REJECT_DECK |
| `requestDeleteDeck(deckId)` | deleteDeck(id) | DELETE_DECK |
| `requestSetOfficial(deckId, isOfficial)` | updateDeckOfficial(id, official) | SET_OFFICIAL |
| `requestUpdateUserRoles(userId, rolesCsv)` | updateUserRoles(userId, roles) | UPDATE_USER_ROLES |

> Lưu ý: tool soạn hành động **không validate deck/user tồn tại trước** (AdminDeckService không expose findById, interface bị khóa). Hành động sai → fail lúc confirm → ghi audit `FAILED` → admin thấy rõ lỗi.

---

## 7. Cấu hình AI

```yaml
# application.yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}          # Groq API key (gsk_...)
      base-url: https://api.groq.com/openai/v1

admin-ai:
  model: ${ADMIN_AI_MODEL:qwen/qwen3.8-27b}   # đổi model không cần sửa code
```

**Tại sao là qwen3.8-27b?** Đây là bài học đắt giá từ debugging:

| Model | Vấn đề |
|---|---|
| `openai/gpt-oss-120b` | Là reasoning model — trả `reasoning` trong response. Spring AI serialize nó thành `reasoning_content` khi replay assistant message trong tool-loop → **Groq từ chối 400** |
| `llama-3.3-70b-versatile` | **Không tồn tại** trên Groq org này (model_not_found) |
| `qwen/qwen3.8-27b` | ✅ Không phát reasoning traces, tool-calling chuẩn, replay OK |

**Lớp bảo vệ `ReasoningSanitizingChatModel`:** dù model đổi sau này, wrapper này gỡ `reasoningContent` khỏi metadata của AssistantMessage trong response trước khi ToolCallingAdvisor dùng nó dựng history vòng 2 — chống lỗi 400 `reasoning_content is unsupported`.

**Tại sao cần `@Qualifier`?** `groqClient` bean là `@Primary`. Lombok `@RequiredArgsConstructor` KHÔNG ưu tiên theo tên field → Spring tiềm `groqClient` thay vì `adminAiClient` âm thầm. Đây là root cause khiến mọi fix model trước đó "không có tác dụng". Bài học: **mọi ChatClient injection trong codebase này phải dùng constructor tường minh + `@Qualifier`** (giống AiExtractionServiceImpl, AiRoleplayServiceImpl...).

---

## 8. Bộ nhớ hội thoại (Chat Memory)

- Nằm trong bảng `spring_ai_chat_memory` (Spring AI tự tạo, `initialize-schema: ALWAYS`)
- `conversationId = adminProfileId` — mỗi admin một thread, **bền vững qua restart**
- Window 30 tin nhắn (`MessageWindowChatMemory.maxMessages(30)`)
- `GET /admin/ai/chat/history?page=&size=` — trả tin nhắn mới nhất trước, phân trang thủ công (page 1 = `size` tin mới nhất)
- `DELETE /admin/ai/chat/history` — xóa thread (reset)

**Lưu ý vận chuyển:** cột `conversation_id` là `VARCHAR(36)` — chỉ vừa UUID. Không thêm prefix nào khác.

---

## 9. Audit Log

Bảng `admin_ai_audit_logs` (auto-created bởi `ddl-auto: update`):

| Cột | Ghi chú |
|---|---|
| admin_id | UUID admin thực hiện |
| action | APPROVE_DECK / REJECT_DECK / DELETE_DECK / SET_OFFICIAL / UPDATE_USER_ROLES |
| params | JSON các tham số tool |
| status | EXECUTED / CANCELLED / FAILED |
| result_summary | Kết quả service (cắt 500 ký tự) hoặc lý do lỗi |
| reference_id | deckId hoặc userId |

**Chỉ log các hành động destructive** (thực thi/hủy/thất bại). Q&A đọc-only không log.

**Quan trọng — FAILED phải persist được:** `confirm()` cố ý **không** `@Transactional`. Mỗi service bên dưới tự có transaction của nó; audit row ghi bằng repository save riêng → kể cả khi hành động fail + exception, log FAILED vẫn còn trong DB.

---

## 10. Bảo mật

| Lớp | Cơ chế |
|---|---|
| Endpoint | `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` class-level — user thường → 403 (có test `AdminAiChatControllerSecurityTest`) |
| Pending action | Chỉ admin **soạn** hành động mới confirm được (ownership check trong Redis payload) |
| One-time use | Confirm/cancel xóa Redis key ngay — không confirm 2 lần được |
| Expiry | `expiresAt = now + 5 phút`; Redis TTL 6 phút để phân biệt 462 (missing/consumed) và 463 (expired) |
| Audit | Mọi thực thi/hủy/thất bại đều ghi DB |
| LLM safety | System prompt cấm bịa dữ liệu; destructive phải qua tool soạn, không được tự thi |

**Điểm yếu đã biết (chấp nhận):** GET-then-DELETE trong confirm không atomic — 2 confirm song song cùng actionId về lý thuyết có thể double-execute. Tác động thấp (các action này hầu như idempotent), có thể xử lý sau bằng Redis SETNX atomic nếu cần.

---

## 11. API tham khảo nhanh

| Endpoint | Method | Body | Ý nghĩa |
|---|---|---|---|
| `/admin/ai/chat` | POST | `{"message": "..."}` | Hỏi bot (tự động tool-call, trả pendingAction nếu có hành động) |
| `/admin/ai/chat/confirm` | POST | `{"actionId": "..."}` | Thực thi thật |
| `/admin/ai/chat/confirm/cancel` | POST | `{"actionId": "..."}` | Hủy |
| `/admin/ai/chat/history` | GET | `?page=1&size=20` | Lịch sử hội thoại |
| `/admin/ai/chat/history` | DELETE | — | Reset hội thoại |
| `/admin/ai/audit-logs` | GET | `?page=1&size=10` | Nhật ký hành động AI |

**Mã lỗi riêng:** `460` = business validation fail (vd. deck không pending), `462` = actionId không tồn tại/đã dùng, `463` = đã hết hạn.

---

## 12. Troubleshooting nhanh (bài học từ production debugging)

| Triệu chứng | Nguyên nhân | Xử lý |
|---|---|---|
| `503 AI_SERVICE_UNAVAILABLE` | Catch-all trong `chat()` — **đọc log dòng `Admin AI chat call failed:`** để thấy lỗi thật | Theo bảng dưới |
| `401 Invalid API Key` | `OPENAI_API_KEY` sai/hết hạn | Cập nhật key trong IntelliJ run config / env |
| `value too long for varchar(36)` | conversationId > 36 ký tự | Giữ conversationId = UUID trần |
| `400 reasoning_content is unsupported` | Groq trả reasoning traces, bị replay | `ReasoningSanitizingChatModel` (đã cài) |
| `ClassCastException DefaultChatOptions` | Wrapper/merge trả sai type options | Override `getOptions()` + `getDefaultOptions()` delegate (đã cài) |
| Model tự bịa `page/size` cho tool 0-arg | Groq strict schema | Tool description ghi rõ "KHÔNG có tham số" (đã cài) |
| Confirm báo `Validation error: actionId không được để trống` | actionId chưa truyền/trống trong body | Kiểm tra body JSON |

---

*Tài liệu tạo: 2026-09-08 · Branch: `feat/ai-agent-admin` · Model: qwen/qwen3.8-27b trên Groq · Spring AI 2.0 / Spring Boot 4.1*