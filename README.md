# VoCat Clone – Smart Vocabulary Learning App for Android

Ứng dụng học từ vựng tiếng Anh thông minh cho Android, lấy cảm hứng từ VoCat. Người dùng có thể tạo bộ từ theo chủ đề, tra nghĩa tiếng Việt, thêm ảnh minh họa, tìm kiếm và đánh dấu từ đã thuộc.

## Giới thiệu

Dự án là bài tập nhóm môn Lập trình Mobile, xây dựng bằng **Kotlin** + **Jetpack Compose**, lưu trữ dữ liệu cục bộ bằng **Room (SQLite)**. Ứng dụng hướng tới trải nghiệm học từ vựng cá nhân hoá: người dùng tự quản lý bộ từ, luyện tập qua nhiều chế độ ôn tập, và (ở giai đoạn nâng cao) đồng bộ dữ liệu qua nhiều thiết bị.

## Tính năng

### Nhóm tính năng lõi (bắt buộc)
- [x] Đăng ký / đăng nhập / quên mật khẩu bằng email (Firebase Authentication)
- [x] Quản lý từ vựng: thêm / sửa / xóa / xem chi tiết (từ, nhiều nghĩa, phiên âm, loại từ, ví dụ, đồng nghĩa); chuyển từ sang bộ khác, đổi ngày tạo
- [x] Thêm ảnh minh họa cho từ bằng máy ảnh hoặc thư viện ảnh
- [x] Phân loại từ vựng: tủ sách (bookshelf) → bộ từ (bookcase), có thêm / sửa / xóa
- [x] Lưu trữ dữ liệu cục bộ bằng SQLite (Room), dữ liệu tách theo tài khoản
- [x] Tra cứu gợi ý nghĩa tiếng Việt trong form thêm/sửa từ qua MyMemory (cần Internet)
- [x] Tìm kiếm từ vựng từ tab **Vocabulary** hoặc trong bộ từ
- [ ] Đọc phát âm từ vựng bằng Text-to-Speech

### Nhóm tính năng ôn tập (điểm nhấn)
- [ ] Chế độ Flashcard (lật thẻ ghi nhớ)
- [ ] Chế độ trắc nghiệm (Multiple choice)
- [ ] Chế độ chính tả (Dictation — nghe và gõ lại từ)
- [x] Đánh dấu từ đã thuộc / chưa thuộc
- [ ] Theo dõi lịch sử ôn tập
- [ ] Chế độ Blinker (quiz chớp nhoáng — từ xuất hiện/biến mất trong vài trăm ms, yêu cầu tối ưu hiệu năng UI real-time)
- [ ] "Làm lại chỉ với các từ trả lời sai" sau mỗi lượt quiz (dùng chung cho mọi chế độ)

### Nhóm tính năng nâng cao (nếu kịp thời gian)
- [ ] Đồng bộ dữ liệu đa thiết bị qua Firebase (merge theo từng từ, xử lý xung đột)
- [ ] Import / Export danh sách từ vựng qua file CSV
- [ ] Nhắc học qua Notification (AlarmManager)
- [ ] Sao lưu / Khôi phục dữ liệu qua Google Drive
- [ ] Hội thoại tiếng Anh theo kịch bản với LLM (Gemini API / OpenAI API)

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Kiến trúc | MVVM (Repository + ViewModel) |
| Cơ sở dữ liệu cục bộ | Room (SQLite), sinh code bằng KSP |
| Xác thực | Firebase Authentication (email/mật khẩu) |
| Gợi ý nghĩa | MyMemory Translation API |
| Hiển thị ảnh | Coil |
| Điều hướng | Navigation Compose |
| Bất đồng bộ | Kotlin Coroutines / Flow |
| Build | Gradle 8.9 (Kotlin DSL, version catalog), AGP 8.6 |

## Hướng dẫn cài đặt

### Yêu cầu môi trường
- Android Studio Ladybug (2024.2.1) trở lên
- JDK 17
- Android SDK 34 (compileSdk), minSdk 24

### Các bước cài đặt

1. Clone repository:
   ```bash
   git clone https://github.com/nguyentrungduc-cyber/VoCatClone.git
   cd VoCatClone
   ```

2. Mở project bằng Android Studio:
   ```
   File > Open > chọn thư mục VoCatClone
   ```

3. Đồng bộ Gradle trong Android Studio. Để build bản debug từ terminal:
   ```bash
   ./gradlew assembleDebug
   ```

4. Chạy ứng dụng trên máy ảo (emulator) hoặc thiết bị thật:
   ```bash
   ./gradlew installDebug
   ```
   Hoặc bấm nút **Run ▶** trong Android Studio.

   Trên Windows PowerShell, dùng `.\gradlew.bat` thay cho `./gradlew` trong các lệnh Gradle.

5. Chạy unit test:
   ```bash
   ./gradlew test
   ```

6. Chạy instrumented test (cần thiết bị/emulator đang chạy):
   ```bash
   ./gradlew connectedAndroidTest
   ```

### Firebase

File `app/google-services.json` của project Firebase `vocat-clone-nhom18` đã có sẵn trong repo. Nếu dùng dự án Firebase riêng, hãy bật phương thức Email/Password và thay file cấu hình bằng file của ứng dụng Android `com.vocatclone.app`. App hiện chỉ dùng đăng nhập email/mật khẩu nên **không cần** thêm SHA-1. Chỉ khi bổ sung Google Sign-In mới phải thêm SHA-1 của debug keystore vào Firebase Console (lấy bằng `./gradlew signingReport`).

## Cách sử dụng

1. Mở ứng dụng: Splash → màn Welcome (chỉ lần đầu) → **Đăng nhập** hoặc **Đăng ký** bằng email
2. Tab **Vocabulary**: tạo **bộ từ vựng** mới (ví dụ: "Từ vựng TOEIC Unit 1") trong tủ sách
3. Mở bộ từ → thêm từ. Nhập từ tiếng Anh và các nghĩa tiếng Việt, hoặc dùng nút tra cứu để lấy gợi ý. Có thể bổ sung phiên âm, loại từ, ví dụ và ảnh minh họa.
4. Tìm từ trong tab **Vocabulary** hoặc trong bộ từ.
5. Mở chi tiết từ để sửa, xóa, đánh dấu đã thuộc hoặc chuyển sang bộ khác
6. Tab **My**: xem tài khoản, đăng xuất

Các tab **Add**, **Quiz**, **Calendar** hiện mới là placeholder. Ở bản debug, khi chưa có dữ liệu, tab Vocabulary có nút nạp dữ liệu mẫu.

## Cấu trúc thư mục

```
app/src/main/java/com/vocatclone/app/
├── MainActivity.kt          # Entry point (gọi AppNavHost) + MainApp: Scaffold + thanh điều hướng dưới
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt   # Room database (singleton, version 3)
│   │   ├── entity/          # Bookshelf, Bookcase, Word, WordMeaning (+ WordWithMeanings, BookcaseWordCount)
│   │   └── dao/             # BookshelfDao, BookcaseDao, WordDao
│   ├── image/               # Lưu ảnh từ vựng trong bộ nhớ riêng của ứng dụng
│   ├── remote/              # Gọi MyMemory Translation API
│   ├── repository/          # Các repository, gồm TranslationRepository, và OwnershipGuard
│   └── seed/                # SampleData — dữ liệu mẫu cho bản debug
└── ui/
    ├── navigation/          # Routes, AppNavHost (Splash → Welcome → Login/Register → Main)
    ├── splash/, welcome/    # Màn khởi động và giới thiệu lần đầu
    ├── auth/                # Màn Login/Register, AuthViewModel (Firebase Auth)
    ├── components/          # VoCatBottomBar (5 tab), logo, mascot
    ├── home/                # Tab "Vocabulary" — danh sách tủ sách / bộ từ
    ├── bookcase/            # Chi tiết bộ từ (danh sách từ), form tạo/sửa bộ từ
    ├── word/                # Chi tiết từ, form thêm/sửa từ, menu thao tác
    ├── profile/             # Tab "My"
    └── theme/               # Color, Theme, Type
```

Schema Room được xuất ra `app/schemas/` để review khi có migration.

Quan hệ dữ liệu: `bookshelves 1-n bookcases 1-n words 1-n word_meanings`. Không có bảng Users — người dùng được xác thực bằng Firebase Auth, dữ liệu lọc theo cột `firebaseUid` của bảng `bookshelves`.

Dự kiến bổ sung: `ui/quiz/`, `ui/settings/`.

## Đóng góp

1. Cập nhật `main` mới nhất: `git checkout main && git pull`
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
