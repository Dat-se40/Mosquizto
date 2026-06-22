<p align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="Mosquizto" width="128"/>
</p>

# Mosquizto

Ứng dụng Android học từ vựng / flashcard theo mô hình tương tự Quizlet. Người dùng có thể tạo bộ thẻ, học qua flashcard và mini-game, khám phá nội dung cộng đồng, kết nối với người dùng khác qua hệ thống follow, và nhận thông báo thời gian thực.

**Quy mô codebase:** ~124 file Java, 1 module `app`.

**Video demo tham khảo:** [Google Drive — Mosquizto demo](https://drive.google.com/drive/folders/1gLWMnU3PYVKcDHFislVJVRVxg4zmWMp7?usp=sharing)

---

## Thuật ngữ hiển thị

Trong **code và API**, dữ liệu chính vẫn dùng tên `collection` (và `CollectionApi`, `collectionId`, …) vì đó là tên entity từ backend.

Trên **giao diện người dùng**, app thống nhất dùng:

| Trong code / API | Hiển thị (EN) | Hiển thị (VI) |
|------------------|---------------|---------------|
| `collection` | study set | bộ thẻ |
| `term` | term | thẻ |

Lý do: từ *collection* dễ gây hiểu nhầm với “bộ sưu tập”; *study set / bộ thẻ* sát hơn với ngữ cảnh học flashcard. Tương tự, *term / thẻ* rõ ràng hơn *thuật ngữ* trong UI tiếng Việt.

---

## Tính năng chính

### Xác thực & tài khoản
- Đăng nhập, đăng ký, quên mật khẩu
- Lưu session (access token, refresh token, user) qua `SharedPreferences`
- Màn hình Welcome là launcher, điều hướng theo trạng thái đăng nhập
- Cài đặt tài khoản: username, email, dark mode, push notification, xóa tài khoản

### Học tập
- **Flashcard:** lật thẻ, vuốt trái/phải, shuffle, auto-play, đếm Known/Learning
- **Memory Game / Learn mode:** nhiều chế độ (`LEARN`, `TEST`, `ONLY_MCQ`, `ONLY_FB`, `ONLY_MATCH`)
  - Trắc nghiệm (MCQ)
  - Điền khuyết (Fill-in-the-blank)
  - Ghép cặp (Matching)
- Ghi nhận phiên học (`study-session`), gửi kết quả batch lên server qua `WorkManager` (`CompleteSessionWorker`) khi mất mạng / thoát app
- **Study streak:** thống kê chuỗi ngày học, phiên học (`AchievementActivity`)

### Quản lý nội dung
- Tạo / sửa / xóa bộ thẻ và từng thẻ (term/definition)
- Thư viện cá nhân, folder, starred items
- Bộ thẻ mở gần đây (**Recents**), Jump back in
- **Gợi ý (Recommendation):** dựa trên **lịch sử học / bộ thẻ mở gần đây** — backend phân tích hoạt động recent để đề xuất study set phù hợp (`GET recommendation/collections`)

### Khám phá & cộng đồng
- Tìm kiếm bộ thẻ (theo từ khóa, tác giả, phân trang)
- Chia sẻ bộ thẻ, chấp nhận/từ chối lời mời
- Báo cáo nội dung vi phạm, xử lý report (owner)

### Mạng người dùng (User Network)

Tính năng xã hội giúp người dùng khám phá tác giả và theo dõi hoạt động học tập của nhau.

| Tính năng | Mô tả |
|-----------|--------|
| **Tìm kiếm người dùng** | Tab *Users* trong `SearchFragment`, gọi `GET user/search` với debounce |
| **Hồ sơ cá nhân** | `ProfilePage` — xem thông tin, số followers/following, streak |
| **Hồ sơ người khác** | `OtherUserProfileActivity` — xem profile, danh sách bộ thẻ công khai |
| **Follow / Unfollow** | Nút Follow trên hồ sơ người khác, optimistic UI + rollback khi API lỗi |
| **Danh sách Follow** | `FollowListActivity` — tab *Following* / *Followers*, phân trang |
| **Bộ thẻ của người khác** | `OtherUserCollectionsFragment` — xem và mở study set từ profile |
| **Báo cáo người dùng** | Bottom sheet: báo cáo vi phạm |
| **Điều hướng xã hội** | Từ kết quả tìm kiếm, study set detail (tác giả) → hồ sơ người dùng |

Luồng follow điển hình:

```
Search (Users) → OtherUserProfileActivity → Follow/Unfollow
ProfilePage → FollowListActivity → OtherUserProfileActivity / ProfilePage
StudySetDetail → OtherUserProfileActivity (tác giả)
```

### Thông báo real-time
- WebSocket STOMP qua `WebSocketManager`
- Topic: `/user/queue/invitation`, `/user/queue/report`
- Badge số thông báo, push notification hệ thống (có thể bật/tắt trong Settings)
- Màn `NotificationActivity` — lời mời, báo cáo bộ thẻ, follow, báo cáo người dùng

### UX & cài đặt
- Dark mode (`ThemeManager`)
- Đa ngôn ngữ: `values/` (EN) và `values-vi/` (VI)
- Bottom navigation: Home, Search, Create, Library, Profile
- About dialogs trong **Settings → Policy**: Privacy Policy, Terms of Service, Open Source Licenses, Help Center

---

## Kiến trúc & stack kỹ thuật

### Stack

| Thành phần | Công nghệ |
|------------|-----------|
| Ngôn ngữ | Java |
| UI | XML Layout + ViewBinding, Material Components |
| DI | Dagger Hilt (KSP) |
| Networking | Retrofit 2.9 + OkHttp + Gson |
| Real-time | STOMP over WebSocket (`StompProtocolAndroid`) |
| Async UI state | LiveData + ViewModel |
| Background work | WorkManager + HiltWorker |
| Event bus | GreenRobot EventBus |
| Image | Glide, CircleImageView |
| Build | Gradle Kotlin DSL, AGP 9.0.1, `compileSdk 36`, `minSdk 27`, `targetSdk 34`, JDK 17 |

> AGP 9 dùng **built-in Kotlin** — không cần plugin `org.jetbrains.kotlin.android` riêng. Compose Compiler plugin được khai báo; UI chính vẫn dùng XML.

### Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────┐
│  Activities / Fragments / Dialogs / Adapters (UI)      │
├─────────────────────────────────────────────────────────┤
│  ViewModels (Home, Search, Login, Notification, …)    │
├─────────────────────────────────────────────────────────┤
│  Services: SessionManager, AuthInterceptor, Workers     │
│  Network: WebSocketManager, Retrofit API interfaces     │
├─────────────────────────────────────────────────────────┤
│  DTO (request/response) · Models · Util                 │
└─────────────────────────────────────────────────────────┘
         │ REST (Bearer JWT)          │ STOMP + Bearer
         ▼                            ▼
              Backend (Spring Boot — port 8080)
```

### Luồng dữ liệu điển hình

1. **API call:** Activity/Fragment → ViewModel → Retrofit `*Api` → `AuthInterceptor` gắn `Authorization: Bearer <token>` → Gson parse `ApiResponse<T>`
2. **Session:** `SessionManager` (Singleton, Hilt) đọc/ghi `SharedPreferences`
3. **Học tập:** `MemoryGameActivity` start session → thu thập `StudySessionDetailRequest` → `CompleteSessionWorker` retry khi lỗi mạng/5xx
4. **Follow:** `OtherUserProfileActivity` → `UserApi.followUser` / `unfollowUser` → cập nhật UI optimistic
5. **Notification:** Login/Main → `MainViewModel.connectStomp()` → `WebSocketManager` → LiveData badge; push notification tuân theo cài đặt `pushNotificationsEnabled`
6. **Gợi ý:** `HomeViewModel` gọi `recommendCollection` — backend dựa trên lịch sử recent để trả về danh sách study set đề xuất trên Home

### Cấu trúc package

```
com.example.mosquizto/
├── Activities/      # Màn hình full-screen (Profile, FollowList, OtherUserProfile, …)
├── Fragments/       # Home, Search, Library, OtherUserCollections, Settings
├── ViewModels/      # Logic + LiveData
├── Network/
│   ├── itf/         # UserApi, CollectionApi, StudyApi, FolderApi
│   └── WebSocketManager.java
├── Modules/         # Hilt NetworkModule
├── Services/        # SessionManager, AuthInterceptor, CompleteSessionWorker
├── Dto/             # request/response
├── Models/          # Domain model UI
├── Adapters/        # RecyclerView
├── Dialogs/         # Bottom sheet, dialog
├── Util/            # Enum, ThemeManager, AboutDialogHelper
└── Event/           # EventBus events
```

### API Backend (REST)

Base URL mặc định (emulator): `http://10.0.2.2:8080/`

| Nhóm | Endpoint ví dụ |
|------|----------------|
| Auth | `auth/login`, `auth/register`, `auth/forgot-password`, `auth/logout` |
| User | `user/profile`, `user/profile/{username}`, `user/search`, `user/streak` |
| Follow | `user/follow/{username}`, `user/followers`, `user/following` |
| Collection | CRUD, search, recent, recommend, share, report |
| Study | `study-session/start`, `complete-batch`, jump-back-in |
| Folder | quản lý folder |

WebSocket: `ws://10.0.2.2:8080` + `ws_endpoint` trong `res/values/strings.xml`.

**Backend repository (Docker):** [hcyk268/mosquizto-backend-api](https://github.com/hcyk268/mosquizto-backend-api/)

---

## Hướng dẫn cài đặt

### Yêu cầu

- Android Studio (Ladybug trở lên khuyến nghị)
- JDK 17+
- Docker (để chạy backend từ repo trên) hoặc backend Mosquizto chạy tại port **8080**

### Backend

1. Clone và chạy backend theo hướng dẫn trong [mosquizto-backend-api](https://github.com/hcyk268/mosquizto-backend-api/) (Docker).
2. Đảm bảo API lắng nghe port **8080**.

### Chạy app Android

1. Clone repository này và mở bằng Android Studio
2. Sync Gradle → Run `app`

### Cấu hình URL backend

URL mặc định (khi backend chạy trên **cùng máy** với emulator):

| File | Giá trị mặc định |
|------|------------------|
| `NetworkModule.java` | `baseUrl("http://10.0.2.2:8080/")` |
| `res/values/strings.xml` | `ws_base_url` → `ws://10.0.2.2:8080` |

> **`10.0.2.2`** là alias của emulator trỏ về `localhost` trên máy host.

**Nếu backend không chạy trên máy host** (ví dụ: deploy trên server khác, máy khác trong LAN, hoặc không dùng emulator mà dùng thiết bị thật), **bắt buộc đổi IP**:

1. Sửa `baseUrl` trong `app/src/main/java/.../Modules/NetworkModule.java`
2. Sửa `ws_base_url` trong `app/src/main/res/values/strings.xml`
3. Dùng IP có thể truy cập được từ emulator/thiết bị (vd: `http://192.168.1.x:8080/`, `ws://192.168.1.x:8080`)
4. Thiết bị thật: đảm bảo cùng mạng với máy chạy backend; `android:usesCleartextTraffic="true"` đã bật trong `AndroidManifest` cho môi trường dev

### Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

Các thư mục build artifact (`build/`, `app/release/`, `.gradle/`, …) đã được liệt kê trong `.gitignore` — không cần commit khi clone hoặc phát triển.

---

## Sơ đồ luồng người dùng

```mermaid
flowchart TD
    A[WelcomeActivity] -->|Chưa đăng nhập| B[Login / Register]
    A -->|Đã đăng nhập| C[MainActivity]
    B --> C
    C --> D[HomeFragment]
    C --> E[SearchFragment]
    C --> F[LibraryFragment]
    C --> G[ProfilePage]
    E -->|Tab Users| H[OtherUserProfileActivity]
    H --> I[Follow / Unfollow]
    H --> J[OtherUserCollectionsFragment]
    G --> K[FollowListActivity]
    K --> H
    G --> L[SettingsFragment]
    D --> M[StudySetDetailActivity]
    M --> N[FlashcardActivity]
    M --> O[MemoryGameActivity]
    M -->|Tác giả| H
    O --> P[CompleteSessionWorker]
    C --> Q[WebSocketManager STOMP]
    Q --> R[Badge + Push Notification]
```

---

## License & chính sách

Nội dung pháp lý và license được nhúng trong app, xem tại **Settings → Policy** (`SettingsFragment`):

- **Privacy Policy** — `AboutDialogHelper.showPrivacyPolicy()`
- **Terms of Service** — `AboutDialogHelper.showTermsOfService()`
- **Open Source Licenses** — danh sách thư viện bên thứ ba (Dagger Hilt, Retrofit, OkHttp, Glide, …) và license Apache 2.0 / tương đương — `AboutDialogHelper.showOpenSourceLicenses()`
- **Help Center** — `AboutDialogHelper.showSupportCenter()`

Nguồn chuỗi: `app/src/main/res/values/strings.xml` (`about_privacy_policy_*`, `about_terms_*`, `about_oss_*`).

---

## Repository

- **Android app:** [Dat-se40/Mosquizto](https://github.com/Dat-se40/Mosquizto)
- **Backend API (Docker):** [hcyk268/mosquizto-backend-api](https://github.com/hcyk268/mosquizto-backend-api/)
- **Video demo:** [Google Drive](https://drive.google.com/drive/folders/1gLWMnU3PYVKcDHFislVJVRVxg4zmWMp7?usp=sharing)
