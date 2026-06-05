# Tutor Learning Management System

Hệ thống quản lý trung tâm gia sư và học tập được phát triển bằng Spring Boot, hỗ trợ quản lý học sinh, giáo viên, lớp học, thời khóa biểu, bài viết, tin nhắn, xác thực người dùng và nhiều tính năng mở rộng khác.

## Công nghệ sử dụng

### Backend
- Java 21
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- Spring Mail
- Spring WebSocket
- Thymeleaf
- JWT Authentication

### Database
- MySQL 8
- Hibernate / JPA

### Third-party Services
- Face++ API (Face Recognition Login)
- Microsoft Cognitive Services Speech SDK
- Google Calendar API
- Gmail SMTP

### Build Tool
- Maven

---

## Chức năng chính

### Authentication & Authorization
- Đăng nhập
- Đăng xuất
- JWT Authentication
- Quản lý phân quyền

### Quản lý người dùng
- Admin
- Giáo viên
- Học sinh
- Nhân viên

### Quản lý học tập
- Lớp học
- Danh sách lớp
- Thời khóa biểu
- Điểm danh
- Quản lý phòng học

### Blog & Nội dung
- Bài viết
- Blog
- Bình luận

### Giao tiếp
- Hệ thống nhắn tin
- WebSocket Realtime Chat

### Tích hợp AI & Sinh trắc học
- Đăng nhập bằng khuôn mặt (Face++)
- Xác thực giọng nói

### Lịch học
- Đồng bộ Google Calendar
- Quản lý sự kiện

### Thông báo
- Email Notification
- Schedule Notification

---

## Cấu trúc thư mục

```text
src
├── main
│   ├── java/com/example/demo
│   │   ├── ControllerAuth
│   │   ├── ControllerFace
│   │   ├── ControllerVoice
│   │   ├── ControllerGET
│   │   ├── ControllerPOST
│   │   ├── GoogleCalendarService
│   │   ├── ModelOOP
│   │   ├── Repository
│   │   ├── UserDetailsService
│   │   └── config
│   │
│   └── resources
│       ├── static
│       ├── templates
│       └── application.properties
│
└── test
```

---

## Các Entity chính

- Admin
- Students
- Teachers
- Employees
- Timetable
- ClassroomDetails
- Attendances
- Messages
- Posts
- Blogs
- Comments
- Events
- Rooms
- OnlineRooms
- Feedbacks
- ScheduleNotifications

---

## Yêu cầu môi trường

| Software | Version |
|-----------|----------|
| Java | 21+ |
| Maven | 3.9+ |
| MySQL | 8.0+ |

---

## Cài đặt

### Clone project

```bash
git clone https://github.com/vuthanhtruong/tutor-learning03.git
cd tutor-learning03
```

### Cấu hình Database

Tạo database MySQL:

```sql
CREATE DATABASE tutor_learning;
```

Cập nhật thông tin kết nối trong:

```properties
src/main/resources/application.properties
```

Ví dụ:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tutor_learning
spring.datasource.username=root
spring.datasource.password=your_password
```

---

## Cấu hình biến môi trường

Khuyến nghị sử dụng biến môi trường thay vì hardcode:

```bash
DB_URL=
DB_USERNAME=
DB_PASSWORD=

MAIL_USERNAME=
MAIL_PASSWORD=

FACEPLUSPLUS_API_KEY=
FACEPLUSPLUS_API_SECRET=
```

---

## Chạy ứng dụng

### Maven Wrapper

Linux / MacOS

```bash
./mvnw spring-boot:run
```

Windows

```bash
mvnw.cmd spring-boot:run
```

### Maven

```bash
mvn spring-boot:run
```

Ứng dụng mặc định chạy tại:

```text
http://localhost:8080
```

---

## Build Project

```bash
mvn clean package
```

File build:

```text
target/demo-0.0.1-SNAPSHOT.jar
```

Chạy file jar:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## Docker

Build image:

```bash
docker build -t tutor-learning .
```

Run container:

```bash
docker run -p 8080:8080 tutor-learning
```

---

## API Modules

### Authentication

```text
/auth/**
```

### Face Recognition

```text
/face/**
```

### Voice Authentication

```text
/voice/**
```

### Students

```text
/student/**
```

### Teachers

```text
/teacher/**
```

### Timetable

```text
/timetable/**
```

### Blogs

```text
/blog/**
```

---

## Security Notes

Trước khi triển khai production cần:

- Thay đổi JWT Secret
- Thay đổi thông tin Database
- Thay đổi thông tin Gmail SMTP
- Thay đổi Face++ API Key
- Không commit credentials lên GitHub
- Bật HTTPS

---

## Future Improvements

- OAuth2 Login
- Mobile Application
- Dashboard Analytics
- Attendance via QR Code
- AI-based Learning Recommendation
- Video Meeting Integration

---

## Authors

Tutor Learning Development Team

GitHub:

https://github.com/vuthanhtruong

---

## License

This project is developed for educational and learning management purposes.
