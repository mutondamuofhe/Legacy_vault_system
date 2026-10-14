# Legacy Vault System - Digital Legacy & Estate Management

**Legacy Vault System** is a secure, personal digital estate planning Android application designed to help users organize and manage their digital legacy. It enables users to safely record online account details, store critical documents, designate trusted executors, write prioritized instructions for loved ones, and convert financial digital assets with live REST API market rates.

---

## 🚀 Key Features

### 🔐 Authentication & Single Sign-On (SSO)
* **Firebase Authentication**: Email and password sign-in with server-side password encryption.
* **Single Sign-On (SSO)**: One-click "Sign in with Google" integration via Google Sign-In and Firebase Auth Google Provider.

### 📊 Home Dashboard & Live REST API Integration
* **Real-Time Overview**: High-level stat cards tracking Digital Assets, Documents, Executors, and Instructions.
* **Live REST API Integration**: Real-time financial exchange rate updates fetched from `Open Exchange Rates REST API` (`open.er-api.com`) to evaluate multi-currency financial/crypto estate assets (USD, ZAR, EUR, GBP).
* **Vault Health Progress Tracker**: Interactive checklist measuring estate completion status.
* **Activity Timeline**: Real-time audit log of all account changes.

### 🛡️ Core Vault Modules
1. **Digital Assets**: Record banking, crypto, social media, and email accounts with password hints and post-mortem actions (*Transfer*, *Delete*, *Memorialize*).
2. **Document Vault**: Store personal, legal, financial, and medical documents with category metadata and notes.
3. **Digital Executors**: Designate primary/secondary estate contacts with defined permission access levels (*Full Access*, *Legal & Financial*, *Documents Only*, *Limited*).
4. **Legacy Instructions**: Prioritized directives (*High*, *Medium*, *Low*) guiding executors through digital estate handling.

### ⚙️ App Settings & Sample Data Seeder
* **Profile Management**: Update user display names and profile records.
* **Account Security**: Password reset link generator dispatched via Firebase Auth.
* **App Preferences**: Persistent settings toggles for Email Notifications and Biometrics/PIN App Lock.
* **Database Seeder**: One-click sample data generator populating **10+ realistic records per table/collection** for demonstration and testing.

### ❓ User Support & Documentation
* **In-App User Guide**: Step-by-step user guide explaining all app surfaces.
* **Frequently Asked Questions (FAQ)**: Security, privacy, and executor access answers.
* **Contact Support**: Direct email support launcher (`support@legacyvaultsystem.com`).

---

## 🛠️ Tech Stack & Architecture

* **Language**: Kotlin
* **UI Framework**: Material Design 3, View Binding, ConstraintLayout, Custom Vector Graphics
* **Architecture**: MVVM (Model-View-ViewModel) with `LiveData` and `activityViewModels`
* **Database / Backend**: Firebase Cloud Firestore & Firebase Auth
* **REST API**: `HttpURLConnection`, `JSONObject`, and background thread execution for Open Exchange Rates API
* **CI/CD**: GitHub Actions workflow for automated testing and APK artifact generation

---

## 🧪 Automated Testing & CI/CD Pipeline

### Local Unit Tests
Run local unit tests via Gradle:
```bash
./gradlew testDebugUnitTest
```
Unit test suite covers:
* `AuthValidationTest`: Email and password strength criteria.
* `DataModelTest`: Object initialization and data mapping integrity.
* `CurrencyApiServiceTest`: Async REST API parsing and fallback handling.

### GitHub Actions Workflow
Continuous Integration is configured in `.github/workflows/android.yml`:
1. Automated environment setup with JDK 17.
2. Automated execution of unit test suite (`./gradlew testDebugUnitTest`).
3. Automated debug build generation (`./gradlew assembleDebug`).
4. Automatic upload of compiled APK artifact on pushes to `main`/`master`.

---

## 📦 Database Structure (DBMS - Cloud Firestore)

* `users/{userId}/assets`: Name, Platform, Category, Username, PasswordHint, URL, ActionAfterDeath, Timestamp
* `users/{userId}/documents`: Title, Category, FileName, Description, Notes, Timestamp
* `users/{userId}/executors`: Name, Relation, Email, Phone, AccessLevel, IsPrimary, Timestamp
* `users/{userId}/instructions`: Title, Content, Type, Priority, Timestamp
* `users/{userId}/activities`: Title, Subtitle, Date, IconType, Timestamp

---

## 📄 License
This project is developed for educational purposes under the IIE Module Guidelines.
