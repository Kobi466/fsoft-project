# BÁO CÁO KẾ HOẠCH SPRINT & DANH SÁCH USER STORIES
## Dự Án: Hệ Thống Học Từ Vựng Flashcard & Ôn Tập Ngắt Quãng (SRS) Tích Hợp AI Tutor, Đấu Kháng 1vs1 & Gamification

---

### 📋 1. BÁO CÁO TỔNG HỢP KẾ HOẠCH SPRINT (SPRINT REPORT)

| Sprint | Mục Tiêu Báo Cáo | Nội Dung Thực Hiện Chi Tiết |
|:---:|:---|:---|
| **Sprint 1** | **Xây dựng Nền tảng Hệ thống & Tính năng Cốt lõi**: Khởi tạo hạ tầng backend/CSDL, hoàn thiện phân hệ Quản lý tài khoản (Đăng ký, Đăng nhập, OAuth2, Phân quyền), Quản lý Bộ từ vựng và tính năng Học Flashcard lật cơ bản. | • Khởi tạo và cấu hình dự án Spring Boot, CSDL MySQL, Bộ nhớ tạm Redis, Lưu trữ Cloud AWS S3 và Dịch vụ gửi Email tự động Brevo.<br/>• Phân tích yêu cầu nghiệp vụ, thiết kế Sơ đồ Cơ sở dữ liệu (ERD) và chuẩn hóa cấu trúc phản hồi API thống nhất.<br/>• Xây dựng phân hệ Xác thực người dùng: Đăng ký tài khoản, Đăng nhập bảo mật JWT, Đăng nhập nhanh qua Google, Đăng xuất và Khôi phục mật khẩu qua OTP email.<br/>• Xây dựng trang Quản lý hồ sơ cá nhân: Cập nhật thông tin, Cài đặt mục tiêu số từ học/ngày, Giờ nhắc nhở và Tải ảnh đại diện cá nhân.<br/>• Xây dựng phân hệ Quản lý Từ vựng: Quản trị viên/Người dùng tạo, sửa, xóa các Bộ từ vựng theo chủ đề (TOEIC, IELTS, CNTT), Gắn nhãn phân loại và Sao chép bộ từ vựng công khai.<br/>• Triển khai tính năng Học từ vựng Flashcard lật 2 mặt tích hợp Phát âm âm thanh bản ngữ, Phiên âm quốc tế, Định nghĩa và Câu ví dụ minh họa. |
| **Sprint 2** | **Hoàn thiện Tính năng Nâng cao & Bản Phát hành Đầy đủ**: Triển khai thuật toán Ôn tập ngắt quãng (SRS), Bài kiểm tra đánh giá tự động, AI Agent sinh Quiz thông minh, Thi đấu kháng 1vs1, Trợ lý Trí tuệ Nhân tạo (AI Tutor), Hệ thống Vinh danh & Thanh toán trực tuyến. | • Triển khai Thuật toán Lặp lại ngắt quãng (SRS): Tự động tính toán chu kỳ quên từ dựa trên đánh giá của học viên (Khó/Vừa/Dễ) và lọc Hàng chờ từ vựng cần ôn trong ngày.<br/>• Triển khai Phân hệ Bài kiểm tra (Quiz) tự động với 4 dạng bài (Trắc nghiệm, Điền từ vào câu, Nghe phát âm chọn từ, Nối từ) và tự động chấm điểm chi tiết.<br/>• **Ứng dụng AI Agent khởi tạo Quiz tự động (AI Quiz Generator)**: AI tự tạo câu hỏi ngữ cảnh thực tế, tạo đáp án nhiễu thông minh và tự động giải thích chi tiết đáp án sau khi học viên nộp bài.<br/>• Xây dựng Phân hệ Đấu kháng 1vs1 thời gian thực: Cho phép hai người chơi tạo phòng thách đấu, so tài độ chính xác và tốc độ phản xạ trả lời.<br/>• Tích hợp Trợ lý Trí tuệ Nhân tạo (AI Tutor): Tự động phân loại ý định người dùng, tra cứu dữ liệu từ vựng nội bộ (RAG), tự động viết lại câu hỏi theo ngữ cảnh và lưu lịch sử hội thoại.<br/>• Triển khai Hệ thống Thi đua & Vinh danh (Gamification): Theo dõi Chuỗi ngày học liên tục (Streak), Biểu đồ ô màu đóng góp theo ngày (Heatmap), Bảng xếp hạng thi đua và Mở khóa Huy hiệu thành tựu.<br/>• Tích hợp Cổng thanh toán trực tuyến VNPay: Cho phép nâng cấp tài khoản Pro, Tự động gửi hóa đơn xác nhận và Báo cáo thống kê quản trị. |

---

### 📊 2. BẢNG DANH SÁCH USER STORIES CHI TIẾT (28 USER STORIES)

| STT | Mã US | Câu Chuyện Người Dùng (User Story) | Độ Ưu Tiên | Sprint |
|:---:|:---:|:-----------------------------------|:----------:|:------:|
| **Phân Hệ 1: Xác Thực & Cài Đặt Tài Khoản** |||||
| 1 | **U001** | Là người dùng mới, tôi muốn đăng ký tài khoản bằng Email hoặc Google, để tạo tài khoản và đăng nhập nhanh chóng. | **High** | Sprint 1 |
| 2 | **U002** | Là người dùng, tôi muốn khôi phục mật khẩu và xác thực email qua mã OTP, để đảm bảo an toàn tài khoản. | **High** | Sprint 1 |
| 3 | **U003** | Là người dùng, tôi muốn chỉnh sửa hồ sơ (ảnh đại diện, họ tên, mục tiêu học hàng ngày), để cá nhân hóa trải nghiệm. | **Medium** | Sprint 1 |
| 4 | **U004** | Là Quản trị viên, tôi muốn phân quyền tài khoản (Học viên / Quản trị viên / Gói Thường vs Pro), để quản lý quyền hạn tính năng. | **High** | Sprint 1 |
| **Phân Hệ 2: Quản Lý Bộ Từ Vựng & Thẻ Học** |||||
| 5 | **U005** | Là người dùng/Quản trị viên, tôi muốn tạo, sửa, xóa các Bộ từ vựng theo chủ đề và gắn nhãn phân loại, để quản lý bài học dễ dàng. | **High** | Sprint 1 |
| 6 | **U006** | Là người dùng, tôi muốn tìm kiếm và xem các Bộ từ vựng chuẩn hoặc bộ từ vựng do cộng đồng chia sẻ, để mở rộng kiến thức. | **High** | Sprint 1 |
| 7 | **U007** | Là người dùng, tôi muốn sao chép bộ từ vựng công khai của người khác về tài khoản của mình, để tự do chỉnh sửa và học cá nhân. | **Medium** | Sprint 1 |
| 8 | **U008** | Là người dùng/Quản trị viên, tôi muốn thêm thẻ từ vựng đầy đủ phiên âm, nghĩa tiếng Việt, câu ví dụ, hình ảnh và phát âm chuẩn, để học qua nhiều giác quan. | **High** | Sprint 1 |
| **Phân Hệ 3: Thuật Toán Ôn Tập Ngắt Quãng (SRS)** |||||
| 9 | **U009** | Là người dùng, tôi muốn học từ vựng qua giao diện Thẻ lật 2 mặt tự động phát âm thanh, để ghi nhớ từ vựng hiệu quả. | **High** | Sprint 1 |
| 10 | **U010** | Là người dùng, tôi muốn đánh giá mức độ ghi nhớ (Khó / Vừa / Dễ) sau khi lật thẻ, để hệ thống tự động tính toán lại lịch ôn tập phù hợp. | **High** | Sprint 2 |
| 11 | **U011** | Là người dùng, tôi muốn hệ thống tự động tổng hợp danh sách từ vựng cần ôn trong ngày, để ôn tập đúng thời điểm sắp quên. | **High** | Sprint 2 |
| **Phân Hệ 4: Hệ Thống Bài Kiểm Tra (Quiz)** |||||
| 12 | **U012** | Là người dùng, tôi muốn hệ thống tự động sinh bài kiểm tra 10/20/50 câu từ bộ từ vựng đã chọn, để đánh giá mức độ thuộc bài. | **High** | Sprint 2 |
| 13 | **U013** | Là người dùng, tôi muốn làm bài kiểm tra với nhiều dạng câu hỏi (Trắc nghiệm, Điền từ, Nghe phát âm, Nối từ), để tăng phản xạ ngôn ngữ. | **Medium** | Sprint 2 |
| 14 | **U014** | Là người dùng, tôi muốn xem ngay kết quả bài kiểm tra (điểm số, tỷ lệ % chính xác, thời gian làm bài và chi tiết đáp án), để biết lỗi sai cần khắc phục. | **Medium** | Sprint 2 |
| **Phân Hệ 5: Thi Đấu Kháng 1 vs 1 (PvP Battle)** |||||
| 15 | **U015** | Là người dùng, tôi muốn tạo phòng thi đấu 1vs1 hoặc mời bạn bè cùng tham gia, để thách đấu kiến thức từ vựng. | **Medium** | Sprint 2 |
| 16 | **U016** | Là người dùng, tôi muốn thi đấu trực tiếp với đối thủ trên cùng bộ từ vựng, để so tài độ chính xác và tốc độ phản xạ. | **Medium** | Sprint 2 |
| 17 | **U017** | Là người dùng, tôi muốn xem bảng tổng kết trận đấu (kết quả thắng/thua, tốc độ trả lời tính bằng mili-giây), để nhận điểm kinh nghiệm vinh danh. | **Low** | Sprint 2 |
| **Phân Hệ 6: Trợ Lý Trí Tuệ Nhân Tạo (AI Agent & RAG)** |||||
| 18 | **U018** | Là người dùng, tôi muốn nhắn tin với Trợ lý AI để giải đáp thắc mắc về nghĩa từ, cách dùng từ và ngữ pháp, để được hỗ trợ tức thời. | **High** | Sprint 2 |
| 19 | **U019** | Là người dùng, tôi muốn AI tự động tra cứu bộ từ vựng tôi đang học để đặt câu ví dụ thực tế, để tôi áp dụng vào ngữ cảnh giao tiếp. | **Medium** | Sprint 2 |
| 20 | **U020** | Là người dùng, tôi muốn hệ thống lưu lại toàn bộ lịch sử trò chuyện với AI, để có thể mở lại xem lại bất cứ lúc nào. | **Medium** | Sprint 2 |
| 28 | **U028** | Là người dùng, tôi muốn hệ thống ứng dụng **AI Agent tự động tạo các bài Quiz ngữ cảnh thực tế và giải thích chi tiết đáp án**, để nâng cao phản xạ và hiểu sâu cách dùng từ. | **High** | Sprint 2 |
| **Phân Hệ 7: Thống Kê Học Tập & Thi Đua (Gamification)** |||||
| 21 | **U021** | Là người dùng, tôi muốn theo dõi chuỗi ngày học liên tục (Streak) và nhận thông báo nhắc nhở, để duy trì thói quen học mỗi ngày. | **High** | Sprint 2 |
| 22 | **U022** | Là người dùng, tôi muốn xem biểu đồ ô màu thể hiện mức độ chăm chỉ học tập theo từng ngày, để theo dõi sự tiến bộ của bản thân. | **Medium** | Sprint 2 |
| 23 | **U023** | Là người dùng, tôi muốn mở khóa các Huy hiệu thành tựu khi đạt mốc học tập, để tạo cảm giác chinh phục và động lực học. | **Low** | Sprint 2 |
| 24 | **U024** | Là người dùng, tôi muốn xem vị trí của mình trên Bảng xếp hạng thi đua Tuần/Tháng, để so sánh thành tích với các học viên khác. | **Low** | Sprint 2 |
| **Phân Hệ 8: Thanh Toán Trực Tuyến & Quản Lý Gói Dịch Vụ** |||||
| 25 | **U025** | Là người dùng, tôi muốn nâng cấp lên tài khoản Pro và thanh toán qua cổng VNPay, để mở khóa đầy đủ tính năng nâng cao. | **Medium** | Sprint 2 |
| 26 | **U026** | Là người dùng Pro, tôi muốn nhận email xác nhận thanh toán và thông báo thời hạn gói dịch vụ, để quản lý tài khoản minh bạch. | **Low** | Sprint 2 |
| 27 | **U027** | Là Quản trị viên, tôi muốn xem báo cáo doanh thu và số lượng học viên hoạt động hàng ngày, để đánh giá hiệu quả vận hành hệ thống. | **Low** | Sprint 2 |
