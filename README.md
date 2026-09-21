# VoCat Clone – Smart Vocabulary Learning App for Android

Ứng dụng học từ vựng tiếng Anh thông minh cho Android, lấy cảm hứng từ VoCat. Người dùng có thể tạo bộ từ vựng theo chủ đề, ôn tập qua Flashcard / trắc nghiệm / chính tả, và theo dõi tiến độ học tập theo thời gian.

## Giới thiệu

Dự án là bài tập nhóm môn Lập trình Mobile, xây dựng bằng **Kotlin** + **Jetpack Compose**, lưu trữ dữ liệu cục bộ bằng **Room (SQLite)**. Ứng dụng hướng tới trải nghiệm học từ vựng cá nhân hoá: người dùng tự quản lý bộ từ, luyện tập qua nhiều chế độ ôn tập, và (ở giai đoạn nâng cao) đồng bộ dữ liệu qua nhiều thiết bị.

## Tính năng

### Nhóm tính năng lõi (bắt buộc)
- [ ] Quản lý từ vựng: thêm / sửa / xóa / xem chi tiết (từ, nghĩa, loại từ, ví dụ, ảnh minh họa)
- [ ] Phân loại từ vựng theo chủ đề (bookcase / bộ từ)
- [ ] Lưu trữ dữ liệu cục bộ bằng SQLite (Room)
- [ ] Gợi ý nghĩa từ tự động khi thêm từ mới (gọi API dịch/từ điển)
- [ ] Đọc phát âm từ vựng bằng Text-to-Speech

### Nhóm tính năng ôn tập (điểm nhấn)
- [ ] Chế độ Flashcard (lật thẻ ghi nhớ)
- [ ] Chế độ trắc nghiệm (Multiple choice)
- [ ] Chế độ chính tả (Dictation — nghe và gõ lại từ)
- [ ] Theo dõi tiến độ học (đánh dấu đã thuộc / chưa thuộc, lịch sử ôn tập)

### Nhóm tính năng nâng cao (nếu kịp thời gian)
- [ ] Nhắc học qua Notification (AlarmManager)
- [ ] Sao lưu / Khôi phục dữ liệu qua Google Drive
- [ ] Đồng bộ dữ liệu đa thiết bị qua Firebase (merge theo từng từ)
- [ ] Import / Export danh sách từ vựng qua file CSV

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose |
| Kiến trúc | MVVM |
| Cơ sở dữ liệu cục bộ | Room (SQLite) |
| Bất đồng bộ | Kotlin Coroutines / Flow |
| Text-to-Speech | Android TTS Engine |
| Build | Gradle (Kotlin DSL) |

## Hướng dẫn cài đặt

### Yêu cầu môi trường
- Android Studio Ladybug (2024.2.1) trở lên
- JDK 17
- Android SDK 34 (compileSdk), minSdk 24

### Các bước cài đặt

1. Clone repository:
   ```bash
   git clone https://github.com/2nguyen3/VoCatClone.git
   cd VoCatClone
   ```

2. Mở project bằng Android Studio:
   ```
   File > Open > chọn thư mục VoCatClone
   ```

3. Đồng bộ Gradle (Android Studio sẽ tự động chạy, hoặc thủ công):
   ```bash
   ./gradlew build
   ```

4. Chạy ứng dụng trên máy ảo (emulator) hoặc thiết bị thật:
   ```bash
   ./gradlew installDebug
   ```
   Hoặc bấm nút **Run ▶** trong Android Studio.

5. Chạy unit test:
   ```bash
   ./gradlew test
   ```

6. Chạy instrumented test (cần thiết bị/emulator đang chạy):
   ```bash
   ./gradlew connectedAndroidTest
   ```

### Cấu hình API (tùy chọn)

Nếu dùng tính năng gợi ý nghĩa từ tự động, tạo file `local.properties` ở thư mục gốc (file này đã nằm trong `.gitignore`, không commit lên git):

```properties
DICTIONARY_API_KEY=your_api_key_here
```

## Cách sử dụng

1. Mở ứng dụng, tạo **bộ từ vựng** mới (ví dụ: "Từ vựng TOEIC Unit 1")
2. Thêm từ vào bộ: nhập từ tiếng Anh, ứng dụng gợi ý nghĩa và ví dụ tự động
3. Nhấn biểu tượng loa để nghe phát âm
4. Vào mục **Ôn tập**, chọn chế độ: Flashcard / Trắc nghiệm / Chính tả
5. Theo dõi tiến độ học trong tab **Thống kê**

## Cấu trúc thư mục

```
app/src/main/java/com/vocatclone/app/
├── data/
│   ├── local/
│   │   ├── entity/       # Room Entity (Word, Bookcase, ReviewLog...)
│   │   └── dao/          # Room DAO interfaces
│   └── repository/       # Repository pattern — nguồn dữ liệu duy nhất cho ViewModel
├── domain/
│   └── model/            # Model nghiệp vụ (tách khỏi Entity nếu cần)
├── ui/
│   ├── home/             # Màn hình danh sách bộ từ
│   ├── vocabulary/       # Màn hình thêm/sửa/xem chi tiết từ
│   ├── review/           # Các chế độ ôn tập (Flashcard, Quiz, Dictation)
│   └── theme/            # Compose Theme, Color, Typography
└── MainActivity.kt       # Entry point
```

## Đóng góp

1. Fork repository này
2. Tạo nhánh feature mới: `git checkout -b feature/ten-tinh-nang`
3. Commit thay đổi: `git commit -m "feat: mô tả ngắn gọn"`
4. Push lên nhánh: `git push origin feature/ten-tinh-nang`
5. Mở Pull Request, mô tả rõ tính năng/fix và cách test

### Quy ước commit
- `feat:` thêm tính năng mới
- `fix:` sửa lỗi
- `refactor:` tái cấu trúc code, không đổi hành vi
- `docs:` chỉnh sửa tài liệu
- `test:` thêm/sửa test

### Thành viên nhóm
- Nguyễn Minh Đức 23520313 
- Trần Minh Đức 24520330 
- Nguyễn Trung Đức 24520324 

## Giấy phép

Dự án phục vụ mục đích học tập, chưa xác định giấy phép phát hành chính thức.
