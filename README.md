Legacy Vault System
Legacy Vault System is a secure, personal digital estate planning application designed to help users manage their digital legacy. It allows users to safely store information about their online accounts, upload critical documents, assign trusted executors, and leave clear instructions for their loved ones.
Key Features
User Features

Home Dashboard: A high-level overview of your vault, including real-time stat cards and a Vault Health progress tracker.
Digital Assets: Securely record banking, social media, and email accounts. Includes "Action After Death" directives (Close, Transfer, Memorialize).
Document Vault: Securely upload and manage important files like Wills, IDs, and property deeds with an integrated file picker.
Digital Executors: Assign trusted contacts to manage your estate. Supports primary executor designation (⭐) and tiered access levels.
Legacy Instructions: Write detailed, prioritized directives for your executors to ensure your wishes are followed.

Timeline Activity: A real-time log of all changes made to your vault.

 Admin Console

Secure Access: Only accessible via specialized administrative credentials.
System Overview: Global statistics for total users, total documents, and pending access requests.
User Management: Monitor, edit, or deactivate user accounts.
Audit Trail: High-level monitoring of system-wide activity and vault health.
Broadcast Notifications: Send system-wide alerts to all registered users.

Tech Stack

Platform: Android (Kotlin)

UI/UX: Material Design 3, XML Layouts, Custom Vector Graphics
Database: Firebase Cloud Firestore (Real-time syncing)
Authentication: Firebase Auth (Email/Password with role-based routing)
Architecture: MVVM (Model-View-ViewModel) with shared ViewModels for cross-fragment data sync.

Visual Identity
Primary Color
Accent Color
Admin Theme
Typography
Deep Navy #0A1931
Gold #D4AF37
Slate #2C3E50
Elegant Serif Headers

Setup & Installation
1.
Clone the Repository:
Shell Script
git clone https://github.com/yourusername/LegacyVaultSystem.git
2.
Add Firebase:
Create a project in the Firebase Console.
Enable Email/Password Authentication.
Initialize a Firestore Database in Test Mode.
Download your google-services.json and place it in the app/ directory.
3.Build & Run:
Open the project in Android Studio.
Sync Gradle and run the app on an emulator or physical device.
Security & Privacy
Data Isolation: Every user’s data is stored in a private Firebase silo. Users can only access data associated with their unique UID.
Role-Based Login: The app automatically distinguishes between regular users and system admins during the login process.

Roadmap
[ ] Implement real biometric (Fingerprint/FaceID) lock.
[ ] Develop the Web Application version (React/Tailwind).
[ ] Integrate end-to-end encryption for uploaded documents.

License
This project is licensed under the MIT License - see the LICENSE file for details.
