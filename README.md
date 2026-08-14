# Smart Home Monitoring

An Android-based smart home monitoring and control system developed using Kotlin, Jetpack Compose, Firebase Realtime Database, Node.js and a web-based hardware simulator.

## Main Features

- Multi-floor smart home dashboard
- Interactive device grid
- Real-time Firebase synchronization
- Device states: ON, OFF, ERROR and DISCONNECTED
- Individual and multi-switch device controls
- Automatic light scheduling
- Iron safety timer and automatic cutoff
- Safety alert generation
- Mock security camera display
- Device usage history and reports
- Web-based hardware simulator
- Android APK for testing

## Technologies

- Kotlin
- Jetpack Compose
- Android Studio
- Firebase Realtime Database
- Firebase Admin SDK
- Node.js
- HTML, CSS and JavaScript
- Git and GitHub

## Project Structure

```text
smart-home-monitoring/
├── app/          Android application
├── backend/      Node.js safety and scheduling worker
├── simulator/    Web-based hardware simulator
└── README.md     Project documentation
```

## Firebase Database Structure

```text
smartHome/
├── devices/
├── floors/
├── alerts/
└── usageEvents/
```

## Android Application Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Create or select a Firebase project.
4. Register an Android app using this package name:

```text
com.example.smart_home_monitoring
```

5. Download `google-services.json`.
6. Place it inside:

```text
app/google-services.json
```

7. Sync the Gradle project.
8. Run the application using an emulator or Android device.

The `google-services.json` file is excluded from GitHub and must be downloaded separately.

## Backend Setup

The backend handles:

- Iron safety cutoff
- Automatic light schedules
- Safety alert creation
- Device usage-event recording

Open PowerShell inside the `backend` folder:

```powershell
cd backend
npm install
```

Generate a Firebase Admin SDK private key from:

```text
Firebase Console
→ Project settings
→ Service accounts
→ Generate new private key
```

Rename the downloaded file to:

```text
serviceAccountKey.json
```

Place it inside the `backend` folder, then start the worker:

```powershell
npm start
```

The service-account file is private and is excluded from GitHub. Never publish or share it.

## Web Simulator Setup

Open another PowerShell window:

```powershell
cd simulator
npx --yes http-server . -p 8080 -c-1
```

Open the simulator at:

```text
http://127.0.0.1:8080
```

The simulator and Android application synchronize device changes through Firebase Realtime Database.

## Testing

The following functions were tested:

- Dashboard and floor navigation
- Android-to-Firebase synchronization
- Firebase-to-Android synchronization
- Web simulator synchronization
- Device ON and OFF controls
- ERROR and DISCONNECTED states
- Multi-switch controls
- Iron automatic safety cutoff
- Safety-alert creation
- Automatic light schedules
- Usage-event recording
- Usage Reports screen
- Generated Android APK

## APK

A debug APK can be generated in Android Studio using:

```text
Build
→ Generate App Bundles or APKs
→ Generate APKs
```

The generated file is normally located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Security Notice

The demonstration database rules restrict writes to the required project paths. Device controls remain publicly accessible because Firebase Authentication is outside the scope of this prototype.

For a production deployment, Firebase Authentication, App Check and stricter user-based database rules should be implemented.

## Important Notes

- The backend worker must remain running for safety cutoffs and automatic schedules.
- The web simulator requires an internet connection to communicate with Firebase.
- Generated build folders, Firebase Admin credentials and local configuration files are excluded from Git.