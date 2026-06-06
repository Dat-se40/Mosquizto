# Mosquizto

Mosquizto is an Android application built with Kotlin and Java, designed as a study platform featuring flashcards, collections, memory games, and collaborative study tools.

## Features

- **User Authentication**: Login, registration, and password reset functionality
- **Study Collections**: Create, edit, and manage study collections and folders
- **Flashcards**: Create and study with flashcard sets
- **Memory Game**: Interactive memory matching game for learning
- **Real-time Collaboration**: STOMP protocol support for real-time features
- **Notifications**: Push notifications for study reminders and updates
- **Profile Management**: User profile and settings management

## Tech Stack

- **Language**: Kotlin & Java
- **UI Framework**: Jetpack Compose, Material Design 3
- **Architecture**: MVVM with Hilt Dependency Injection
- **Networking**: 
  - Retrofit for REST API calls
  - OkHttp for HTTP client
  - Gson for JSON parsing
  - STOMP Protocol for WebSocket communication
- **Image Loading**: Glide
- **Reactive Programming**: RxJava2, RxAndroid
- **Event Bus**: EventBus for component communication
- **Background Tasks**: WorkManager
- **Navigation**: Jetpack Navigation Component
- **Maps**: Google Maps SDK (3D)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/mosquizto/
│   │   ├── Activities/          # All activity classes
│   │   ├── Models/              # Data models
│   │   ├── Services/            # Background services and workers
│   │   ├── Network/             # API interfaces and network utilities
│   │   ├── MainActivity.java    # Main entry point
│   │   └── MosquiztoApp.java    # Application class
│   ├── res/                     # Resources (layouts, strings, etc.)
│   └── AndroidManifest.xml      # App manifest
```

## Requirements

- **Minimum SDK**: 27 (Android 8.1 Oreo)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 36
- **Java Version**: 11

## Build Configuration

### Plugins Used

- Android Application Plugin
- Kotlin Compose Plugin
- Dagger Hilt (Dependency Injection)
- KSP (Kotlin Symbol Processing)

### Key Dependencies

- `androidx.compose.*` - Jetpack Compose UI toolkit
- `com.google.dagger:hilt-android` - Dependency injection
- `com.squareup.retrofit2:retrofit` - REST API client
- `com.github.bumptech.glide:glide` - Image loading
- `io.reactivex.rxjava2` - Reactive programming
- `org.greenrobot:eventbus` - Event bus pattern
- `androidx.work:work-runtime` - Background work scheduling

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK with API levels 27-36

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Mosquizto
   ```

2. Sync Gradle files:
   - Open the project in Android Studio
   - Let Gradle sync automatically, or click "Sync Now"

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run on emulator or device:
   ```bash
   ./gradlew installDebug
   ```

## Development

### Running Tests

```bash
./gradlew test          # Run unit tests
./gradlew connectedCheck # Run instrumented tests
```

### Building Release APK

```bash
./gradlew assembleRelease
```

## Configuration

### API Integration

The app connects to a backend API for:
- User authentication
- Study materials management
- Real-time collaboration features

Update API endpoints in the network configuration files as needed.

### Notifications

The app uses WorkManager for scheduled notifications and session tracking. The default WorkManager initializer has been disabled in favor of custom initialization.

## License

This project is proprietary software. All rights reserved.

## Support

For issues and questions, please contact the development team.
