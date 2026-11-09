# Yeme Bingo (የሜ ቢንጎ) 🇪🇹

[![Android CI](https://github.com/hemenyemane/yeme-bingo/actions/workflows/android-ci.yml/badge.svg)](https://github.com/hemenyemane/yeme-bingo/actions/workflows/android-ci.yml)
[![Firestore Rules](https://github.com/hemenyemane/yeme-bingo/actions/workflows/rules-test.yml/badge.svg)](https://github.com/hemenyemane/yeme-bingo/actions/workflows/rules-test.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-FFA000.svg)](https://firebase.google.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A modern, real-time multiplayer Ethiopian Bingo Android game built using **Jetpack Compose**, **Material 3**, **Kotlin Coroutines/Flow**, and **Google Cloud Firestore**.

---

## 🌟 Key Highlights

- **Pure Amharic Male Caller Voice**: Crisp, calibrated, fast callouts for all 75 numbers in authentic Ethiopian bingo style (e.g. *"ቢ፣ አስራ አምስት"*, *"ጂ፣ ሃምሳ ዘጠኝ"*).
- **Scheduled Ethiopian Time Slots**: Synchronized game rounds operating on Ethiopian scheduled daytime hours (**UTC+3**: 10:00, 12:00, 14:00, 16:00, 18:00, 20:00).
- **Backend-Connected Cloud Multiplayer**:
  - Direct integration with **Google Cloud Firestore**.
  - Interactive **Google Sign-In** via Jetpack `CredentialManager` and standard player accounts.
  - Real-time ledger synchronization for wallet balances, cartela reservations, and win records.
- **4 Integrated Ethiopian Payment Providers**:
  - **Telebirr** (`*127#` / Telebirr SuperApp)
  - **Commercial Bank of Ethiopia (CBE)** (CBE Mobile Banking)
  - **CBE Birr** (`*847#` / CBE Birr App)
  - **E-Birr** (`*841#` / E-Birr App)
  - Direct payout & deposit processing to account holder **Yemane (0939383425)**.
- **Trilingual Interface**:
  - English
  - Amharic (አማርኛ)
  - Tigrinya (ትግርኛ)
- **Zero-Trust Hardened Security Rules**:
  - Full schema validation, ownership guards, and unauthenticated read/write prevention on all collections.

---

## 📱 Tech Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Declarative UI)
- **Design System**: Material Design 3 (M3) with Edge-to-Edge window insets
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Single Source of Truth
- **Asynchronous & Reactive**: Kotlin Coroutines & `StateFlow`
- **Backend & Auth**:
  - Firebase Authentication (Google Sign-In with Credential Manager)
  - Cloud Firestore (Live snapshot listeners, batch writes)
- **Audio & Haptics**: Android `TextToSpeech` + dynamic audio synthesizer & haptic feedback
- **Testing**:
  - Local JVM unit tests (`ExampleUnitTest.kt`)
  - Node.js rules unit test suite (`firestore.test.js`)

---

## 📂 Project Structure

```
├── .github/
│   └── workflows/
│       ├── android-ci.yml       # Automated build & unit tests on push/PR
│       └── rules-test.yml       # Emulator-backed Firestore security rules testing
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── audio/              # Amharic audio calling & synthesis
│   │   │   │   ├── data/               # Repository layer & Firestore sync
│   │   │   │   ├── game/               # Bingo card generator, pattern engine & time manager
│   │   │   │   ├── model/              # Data models (Cards, Goals, Payments, Profiles)
│   │   │   │   └── ui/                 # Screens, Components & Themes
│   │   │   └── res/
│   │   └── test/                       # Unit tests
│   └── build.gradle.kts
├── firebase-blueprint.json             # Firestore schema intermediate representation
├── firebase.json                       # Emulator & database configuration
├── firestore.rules                     # Zero-trust Firestore security rules
├── firestore.test.js                   # Sub-second automated security rules tests
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Project settings
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer
- **JDK**: Version 17 or higher
- **Android SDK**: `compileSdk = 36`, `minSdk = 24`

### Clone & Build

```bash
# Clone the repository
git clone https://github.com/hemenyemane/yeme-bingo.git
cd yeme-bingo

# Grant executable permission to the Gradle wrapper
chmod +x ./gradlew

# Build the debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔒 Firebase Configuration

1. Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
2. Enable **Firestore Database** and **Firebase Authentication** (Google provider).
3. Register your Android app with package name `com.aistudio.yemebingo.xkrpqt`.
4. Add your debug & release SHA-1 / SHA-256 fingerprints:
   ```bash
   keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
5. Place your downloaded `google-services.json` inside the `app/` folder.
6. Deploy security rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

---

## 🧪 Testing

### Local Kotlin Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Local Security Rules Unit Tests (JavaScript + Emulator)
```bash
# Run the Firestore emulator and execute test suite
firebase emulators:exec --only firestore "node --test firestore.test.js"
```

---

## ⚙️ GitHub Actions CI/CD

This repository includes pre-configured GitHub Actions workflows in `.github/workflows/`:

1. **`android-ci.yml`**: Automatically verifies builds, executes unit tests, and uploads the generated `app-debug.apk` as a downloadable artifact on every commit to `main` or pull request.
2. **`rules-test.yml`**: Spawns the Cloud Firestore emulator and runs the automated zero-trust security rules suite against `firestore.rules`.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
