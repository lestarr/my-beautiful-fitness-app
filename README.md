# Fitness Tracker Android App

A lightweight, offline-first Android fitness tracking application built with Kotlin and Jetpack Compose.

## Features

### Exercise Management
- **Extendable Exercise Database**: Store exercises with name and body part
- **CRUD Operations**: Add, edit, and delete exercises
- **CSV Import/Export**: Import exercises from CSV files or export your database
- **Smart Display**:
  - Alphabetical list when < 20 exercises
  - Hierarchical grouped by body part when ≥ 20 exercises

### Multi-User Support
- **User Management**: Create, switch, and delete users
- **Shared Exercise Database**: All users share the same exercise database
- **User Logs**: Each user has their own workout logs
- **Safe Deletion**: Logs are automatically exported to CSV and sent via email before user deletion

### Workout Logging
- **Quick Entry**: Log weight and reps for each exercise
- **Auto-Date**: Date/time automatically filled on log creation
- **Recent History**: View last 3 logs when adding a new entry
- **Edit & Delete**: Modify or remove previous logs
- **Best Set Tracking**: Focus on tracking your best set per exercise

### Dashboard & Analytics
- **Weight Progression**: View weight over time per exercise
- **Personal Records**: Automatic PR tracking with celebration notifications
- **Exercise History**: Complete log history for each exercise

### Additional Features
- **Unit Toggle**: Switch between kg and lbs
- **Optional PIN Auth**: Secure app with 4-digit PIN (optional)
- **Data Export**: Export user logs as CSV
- **Offline-First**: All data stored locally using Room Database
- **Material Design 3**: Modern, clean UI following Material Design guidelines

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Navigation**: Jetpack Navigation Compose
- **Async Operations**: Kotlin Coroutines & Flow
- **CSV Handling**: OpenCSV
- **Design**: Material Design 3

## Requirements

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **Minimum Android Version**: API 24 (Android 7.0)
- **Target Android Version**: API 34 (Android 14)
- **JDK**: Java 17

## Build Instructions

### Using Android Studio

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd my-beautiful-fitness-app
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory and select it

3. **Sync Gradle**
   - Android Studio should automatically start syncing Gradle
   - If not, click "Sync Project with Gradle Files" in the toolbar

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your device/emulator

### Using Command Line

1. **Clone and navigate**
   ```bash
   git clone <repository-url>
   cd my-beautiful-fitness-app
   ```

2. **Build the APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

4. **Build release APK** (unsigned)
   ```bash
   ./gradlew assembleRelease
   ```

## Database Schema

### Users Table
```kotlin
{
  id: Long (Primary Key)
  name: String
  createdAt: Long (timestamp)
}
```

### Exercises Table
```kotlin
{
  id: Long (Primary Key)
  name: String
  bodyPart: String
}
```

### Logs Table
```kotlin
{
  id: Long (Primary Key)
  userId: Long (Foreign Key -> Users)
  exerciseId: Long (Foreign Key -> Exercises)
  weight: Double (stored in kg)
  reps: Int
  date: Long (timestamp)
}
```

## CSV Format

### Exercise Import/Export
```csv
Exercise Name,Body Part
Bench Press,Chest
Squats,Legs
Deadlift,Back
```

### Logs Export
```csv
Date,Exercise,Body Part,Weight (kg),Reps
2024-01-15 10:30:00,Bench Press,Chest,80.0,10
2024-01-15 10:35:00,Squats,Legs,100.0,8
```

## Usage Guide

### First Time Setup

1. **Launch the app**
2. **Optional**: Set up a 4-digit PIN for app security
3. **Create your user profile**
4. **Add exercises** to your database (or import from CSV)
5. **Start logging your workouts!**

### Adding an Exercise

1. From the Exercise List screen, tap the **+** button
2. Or go to **Manage Exercises** from the menu
3. Enter exercise name and body part
4. Tap **Save**

### Logging a Workout

1. Tap on an exercise from the list
2. View your last 3 logs and personal record
3. Enter weight and reps for your best set
4. Tap **Save Log**
5. Get notified if you hit a new PR!

### Viewing Progress

1. Tap the **Dashboard** icon from the Exercise List
2. View all exercises with logged data
3. See weight progression and personal records
4. Tap any exercise to view detailed history

### Switching Users

1. Go to **Settings** from the Exercise List
2. Tap **Switch** next to current user
3. Select a different user or create a new one

## Project Structure

```
app/src/main/java/com/fitness/tracker/
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── dao/           # Data Access Objects
│   │   └── entity/        # Database entities
│   └── repository/        # Repository layer
├── ui/
│   ├── screen/           # Composable screens
│   ├── theme/            # App theming
│   └── viewmodel/        # ViewModels
├── navigation/           # Navigation setup
├── util/                 # Utility classes
└── MainActivity.kt       # App entry point
```

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]

## Support

For issues, questions, or suggestions, please [open an issue](link-to-issues) on GitHub.

---

**Built with ❤️ for fitness enthusiasts**
