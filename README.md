# FSoft Project - Backend Server

Tài liệu này hướng dẫn chi tiết các bước thiết lập, cấu hình và chạy dự án Backend dành cho thành viên trong đội phát triển.

---

## 1. Yêu Cầu Hệ Thống (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:
- **Java Development Kit (JDK) 21**
- **Docker & Docker Compose** (để chạy database PostgreSQL và Redis)
- **Maven** (hoặc sử dụng trực tiếp Maven Wrapper `mvnw` đi kèm dự án)
- **Git**
- **IDE** (Khuyên dùng **IntelliJ IDEA** bản Ultimate hoặc Community) có cài đặt plugin **Lombok** và bật tùy chọn **Enable Annotation Processing** trong phần Settings/Preferences.

---

## 2. Cấu Trúc Thư Mục Dự Án

Cấu trúc dự án gồm hai phần chính:
```text
fsoft-project/
├── docker-compose.yml          # Cấu hình khởi chạy nhanh PostgreSQL và Redis qua Docker
├── .env-example                # File cấu hình mẫu cho các biến môi trường Docker
├── .env                        # File cấu hình thực tế cho Docker (sẽ được tạo từ .env-example)
└── fsoft-project-server/       # Thư mục mã nguồn Spring Boot Backend
    ├── mvnw / mvnw.cmd         # Maven Wrapper để build dự án không cần cài Maven thủ công
    ├── pom.xml                 # Khai báo thư viện và plugin của dự án (Spring Boot 4.1.0, Java 21)
    └── src/main/
        ├── java/...            # Package gốc: com.fivetuat.fsoftprojectserver
        └── resources/
            ├── application.yaml          # File cấu hình chung của ứng dụng
            ├── application-dev.properties # Cấu hình chi tiết cho môi trường phát triển (Local/Dev)
            └── application-prod.yml      # Cấu hình chi tiết cho môi trường sản phẩm (Production)
```

---

## 3. Các Bước Cài Đặt & Chạy Dự Án

### Bước 1: Clone dự án và đi tới thư mục gốc
Mở Terminal/PowerShell và di chuyển vào thư mục gốc của dự án:
```bash
cd fsoft-project
```

### Bước 2: Khởi động Cơ sở dữ liệu và Redis
Dự án sử dụng **PostgreSQL 16** làm Database chính và **Redis 8** làm cache/session store. Để khởi chạy nhanh hai dịch vụ này:

1. Sao chép file `.env-example` thành `.env`:
   - **Windows (Command Prompt):** `copy .env-example .env`
   - **Windows (PowerShell):** `cp .env-example .env`
   - **Linux / macOS:** `cp .env-example .env`

2. Kiểm tra nội dung file `.env` đã khớp với thông tin mong muốn chưa. Cấu hình mặc định:
   ```env
   POSTGRES_DB=fsoft-db
   POSTGRES_USER=admin
   POSTGRES_PASSWORD=12345678
   ```

3. Khởi chạy các container bằng Docker Compose:
   ```bash
   docker compose up -d
   ```
   *Lưu ý: Lệnh trên sẽ tải về các image và khởi chạy PostgreSQL trên cổng `5432` và Redis trên cổng `6379` ở chế độ chạy ngầm.*

---

### Bước 3: Cấu Hình Backend (Spring Boot)

Ứng dụng Spring Boot cấu hình mặc định chạy profile `dev` (được khai báo tại `application.yaml` -> `spring.profiles.active: dev`).

Profile `dev` sẽ tự động tải các cấu hình trong file `fsoft-project-server/src/main/resources/application-dev.properties` để kết nối vào Database và Redis do Docker Compose vừa tạo:
- **Cơ sở dữ liệu:** `jdbc:postgresql://localhost:5432/fsoft-db`
- **Tài khoản:** `admin` / `12345678`
- **Redis:** Host `localhost`, Port `6379`
- **JWT Configurations:**
  - `SECRET_KEY`: Khóa bí mật dùng để ký token.
  - `ACCESS_TOKEN_EX`: Thời hạn sống của Access Token (mặc định 259200000 ms - ~3 ngày).
  - `REFRESH_TOKEN_EX`: Thời hạn sống của Refresh Token (mặc định 604800000 ms - ~7 ngày).

> **Mẹo:** Bạn không cần thay đổi bất kỳ cấu hình nào trong file `application-dev.properties` nếu giữ nguyên cấu hình Docker mặc định.

---

### Bước 4: Khởi Chạy Backend

Di chuyển vào thư mục `fsoft-project-server` và khởi động dự án:

#### Sử dụng Maven Wrapper
- **Trên Windows (PowerShell/CMD):**
  ```powershell
  cd fsoft-project-server
  .\mvnw.cmd spring-boot:run
  ```
- **Trên Linux / macOS:**
  ```bash
  cd fsoft-project-server
  chmod +x mvnw
  ./mvnw spring-boot:run
  ```

#### Sử dụng IDE (IntelliJ IDEA)
1. Mở thư mục `fsoft-project-server` dưới dạng một dự án Maven mới trong IntelliJ.
2. Đợi IDE load xong các dependencies từ `pom.xml`.
3. Tìm file chính `FsoftProjectServerApplication.java` tại đường dẫn `src/main/java/com/fivetuat/fsoftprojectserver/FsoftProjectServerApplication.java`.
4. Click chuột phải chọn **Run 'FsoftProjectServerApplication'**.

Khi khởi chạy thành công, log console sẽ hiển thị Spring Boot đang lắng nghe tại cổng `8080` với context path `/fsoft`.

---

## 4. Kiểm Tra Hoạt Động & Tài Liệu API

- **Trang chủ API (Context Path):** `http://localhost:8080/fsoft`
- **Tài liệu API Swagger UI:** [http://localhost:8080/fsoft/swagger-ui.html](http://localhost:8080/fsoft/swagger-ui.html)
- **Đường dẫn API JSON (OpenAPI Docs):** [http://localhost:8080/fsoft/api-docs](http://localhost:8080/fsoft/api-docs)

---

## 5. Công Nghệ & Thư Viện Sử Dụng

- **Spring Boot 4.1.0** & **Java 21**: Nền tảng cốt lõi của ứng dụng.
- **Spring Security & OAuth2 Resource Server**: Quản lý xác thực và phân quyền bằng JSON Web Token (JWT).
- **Spring Data JPA & Hibernate Spatial**: Giao tiếp cơ sở dữ liệu PostgreSQL kèm hỗ trợ lưu trữ tọa độ địa lý/bản đồ.
- **Spring Data Redis**: Hỗ trợ lưu trữ phiên làm việc (Session) và cơ chế cache dữ liệu.
- **Springdoc OpenAPI**: Tự động sinh tài liệu Swagger UI phục vụ quá trình tích hợp với Frontend.
- **Lombok & MapStruct**: Giảm thiểu code boilerplate và tự động ánh xạ dữ liệu giữa DTO và Entity.
- **Múi giờ hệ thống**: Được đồng bộ mặc định sang `Asia/Ho_Chi_Minh` khi khởi chạy ứng dụng.
