# Fitness Tracker App - Enhanced Requirements Document

## Overview
This document contains the complete, refined requirements for the fitness tracking Android app, incorporating all lessons learned during development. Use this as the authoritative specification to avoid common pitfalls.

---

## 1. Core Features

### 1.1 Exercise Database
**Schema:**
```kotlin
Exercise {
    id: Long (auto-generated)
    name: String
    bodyPart: String  // e.g., "Chest", "Legs", "Back", "Arms", "Shoulders"
}
```

**Requirements:**
- ✅ Full CRUD operations (Create, Read, Update, Delete)
- ✅ CSV import/export functionality
- ✅ Shared database across all users
- ✅ Foreign key CASCADE delete (deleting exercise deletes associated logs)

**Display Logic:**
- **< 20 exercises:** Flat alphabetical list
- **≥ 20 exercises:** Hierarchical grouped by body part
- Auto-switch between views based on count

**Critical Implementation Notes:**
- Use Room `@Entity` with proper foreign key relationships
- Index on frequently queried columns
- Validate body part from predefined list

---

### 1.2 User Management
**Schema:**
```kotlin
User {
    id: Long (auto-generated)
    name: String
    createdAt: Long (timestamp)
}
```

**Requirements:**
- ✅ Multi-user support (shared exercise database, individual logs)
- ✅ Create new users
- ✅ Switch between users
- ✅ Delete users with automatic log export

**User Deletion Flow:**
1. Trigger deletion
2. **Fetch all user's logs using `.first()` NOT `.collect{}`** ⚠️ CRITICAL
3. If logs exist:
   - Export to CSV via `CsvHelper`
   - Open email app with attachment
   - Subject: "Fitness Logs Export - [Username]"
   - Empty recipient (user enters address)
4. Delete user from database
5. Show confirmation toast

**Critical Implementation Notes:**
- **ALWAYS use `.first()` for one-time data fetch** ⚠️
- `.collect{}` creates infinite observer - causes app freeze
- Room foreign key CASCADE handles related log deletion
- Store current user ID in SharedPreferences/PreferencesManager

---

### 1.3 Workout Logging
**Schema:**
```kotlin
Log {
    id: Long (auto-generated)
    userId: Long (foreign key -> User.id)
    exerciseId: Long (foreign key -> Exercise.id)
    weight: Double  // ALWAYS stored in kg internally
    reps: Int
    date: Long (timestamp, auto-filled)
}
```

**Requirements:**
- ✅ Log single "best set" per workout session
- ✅ Track: exercise, weight, reps, date
- ✅ Auto-fill date (current timestamp)
- ✅ Show last 3 logs when logging new entry
- ✅ Edit existing logs (via dialog - not fully implemented yet)
- ✅ Delete logs with confirmation
- ✅ Personal Record (PR) detection and notification

**PR Logic:**
- Compare current weight with maximum weight for that exercise
- If current > previous max: New PR
- Show toast: "New PR! Log saved" vs "Log saved"
- **Note:** Current logic only compares weight, not reps

**Display in Log Entry Screen:**
- Last 3 logs in reverse chronological order
- Format: Weight (in user's preferred unit), Reps, Date
- Current PR displayed at top

**Critical Implementation Notes:**
- **Store weight ONLY in kg** - convert to lbs for display only
- Use Room `@Relation` for JOIN queries (`LogWithExercise`)
- Index on `userId`, `exerciseId`, and `date` columns
- **Use `.first()` when fetching logs for export** ⚠️

---

### 1.4 Dashboard & Progress Tracking
**Requirements:**
- ✅ One card per exercise (only exercises with logs)
- ✅ Weight progression over time (chart/graph)
- ✅ Display current Personal Record
- ✅ Click exercise card to navigate to Log Entry screen
- ✅ Filter by current user's logs only

**Data Aggregation:**
```kotlin
// Group logs by exercise
val logsGroupedByExercise = allLogsForUser.groupBy { it.exercise.id }

// Calculate PR for each exercise
val personalRecords = logsGroupedByExercise.mapValues { (_, logs) ->
    logs.maxOfOrNull { it.log.weight } ?: 0.0
}
```

**Critical Implementation Notes:**
- Use `collectAsState(initial = emptyList())` NOT `collectAsStateWithLifecycle()` ⚠️
- Parameter name is `initial` NOT `initialValue` ⚠️
- Remember to convert weight to user's preferred unit for display
- Use `@OptIn(ExperimentalMaterial3Api::class)` for Card onClick ⚠️

---

### 1.5 Unit Conversion (kg / lbs)
**Requirements:**
- ✅ Toggle between kg and lbs in Settings
- ✅ **Store ONLY in kg in database**
- ✅ Convert to lbs for display when toggle enabled
- ✅ User preference saved in SharedPreferences

**Conversion:**
- 1 kg = 2.20462 lbs
- 1 lb = 0.453592 kg

**Implementation:**
```kotlin
object UnitConverter {
    fun kgToLbs(kg: Double): Double = kg * 2.20462
    fun lbsToKg(lbs: Double): Double = lbs / 2.20462

    fun formatWeight(kg: Double, useKg: Boolean): String {
        return if (useKg) {
            String.format("%.1f kg", kg)
        } else {
            String.format("%.1f lbs", kgToLbs(kg))
        }
    }
}
```

**Critical Implementation Notes:**
- Never store lbs in database
- Apply conversion in UI layer only
- User input: if using lbs, convert to kg before saving
- Consistency: all displays must use same conversion factor

---

### 1.6 CSV Import/Export

#### Exercise CSV Format:
```csv
name,bodyPart
Bench Press,Chest
Squat,Legs
```

#### Log CSV Format:
```csv
date,exercise,bodyPart,weight(kg),reps
2025-11-01T10:30:00,Bench Press,Chest,60.0,10
```

**Requirements:**
- ✅ Export exercises to CSV
- ✅ Import exercises from CSV (bulk operation)
- ✅ Export user logs to CSV (for deletion or manual export)
- ✅ Open email app with CSV attachment

**Critical Implementation Notes:**
- Use OpenCSV library for parsing
- **Add try-catch blocks for file operations** ⚠️ (currently missing)
- Validate CSV format before import
- Use FileProvider for sharing files via email
- Email helper: Allow empty recipient (user enters manually)

**Error Handling Needed:**
```kotlin
try {
    val exercises = CsvHelper.parseExercisesFromCsv(context, uri)
    // import exercises
} catch (e: IOException) {
    Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
} catch (e: CsvValidationException) {
    Toast.makeText(context, "Invalid CSV format", Toast.LENGTH_SHORT).show()
}
```

---

### 1.7 Authentication (Optional PIN)
**Requirements:**
- ✅ Optional 4-digit PIN authentication
- ✅ First launch: setup PIN screen
- ✅ Subsequent launches: enter PIN screen (if enabled)
- ✅ Toggle authentication on/off in Settings
- ✅ Store PIN securely in SharedPreferences (encrypted recommended)

**Flow:**
```
App Launch
  → Check if authEnabled
    → If NO: Navigate to User Selection or Exercise List
    → If YES: Navigate to Auth Screen
      → If PIN not set: Setup PIN
      → If PIN set: Enter PIN
        → On success: Navigate to User Selection or Exercise List
        → On failure: Show error, allow retry
```

**Critical Implementation Notes:**
- Store PIN in `PreferencesManager`
- Use `isAuthenticated` state to control navigation
- Clear `isAuthenticated` on app restart if auth enabled

---

## 2. Technical Stack (REQUIRED)

### 2.1 Languages & Frameworks
- **Kotlin** (primary language)
- **Jetpack Compose** (declarative UI)
- **Material Design 3** (UI components)

### 2.2 Architecture
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** (data abstraction)
- **Single Activity Architecture** (Navigation Compose)

### 2.3 Core Libraries

```kotlin
// build.gradle.kts (app level)
dependencies {
    // Compose
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")

    // ViewModels & Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // CSV
    implementation("com.opencsv:opencsv:5.8")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 2.4 Build Configuration

```kotlin
// build.gradle.kts (app level)
android {
    compileSdk = 34  // MUST BE 34 for dependency compatibility ⚠️

    defaultConfig {
        minSdk = 24  // Android 7.0+
        targetSdk = 34  // MUST BE 34 ⚠️
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")  // For Room
}
```

**Critical Notes:**
- ⚠️ **compileSdk and targetSdk MUST be 34** (androidx.activity:1.8.1+ requires it)
- Use Java 17 (NOT 11 or 21)
- Use KSP (NOT kapt) for Room annotation processing

---

## 3. Build System & CI/CD (REQUIRED)

### 3.1 Why GitHub Actions is Required
**Problem:** Windows has unresolvable jlink.exe compatibility issues with Android SDK 34

**Solution:** Build on Linux via GitHub Actions

### 3.2 GitHub Actions Workflow

Create `.github/workflows/build.yml`:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main, develop, claude/* ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '17'

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v3

    - name: Make gradlew executable
      run: chmod +x gradlew

    - name: Build debug APK
      run: ./gradlew assembleDebug --stacktrace

    - name: Upload APK artifact
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 3.3 Gradle Wrapper (REQUIRED)

**Files needed:**
- `gradlew` (Linux/Mac executable script)
- `gradlew.bat` (Windows script)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

**gradle-wrapper.properties:**
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

**How to generate if missing:**
```bash
gradle wrapper --gradle-version 8.2
```

### 3.4 gradle.properties (REQUIRED)

**CRITICAL:** Do NOT include environment-specific paths

```properties
# Gradle Settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# Android
android.useAndroidX=true
android.enableJetifier=false

# Kotlin
kotlin.code.style=official

# DO NOT ADD:
# org.gradle.java.home=C:\Program Files\...  ⚠️ BREAKS CI/CD
```

**Why:** Hardcoded paths break cross-platform builds. Let each environment auto-detect Java.

---

## 4. Database Design

### 4.1 Room Database Setup

```kotlin
@Database(
    entities = [User::class, Exercise::class, Log::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitness_tracker_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
```

### 4.2 Entities with Proper Relationships

```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String
)

@Entity(
    tableName = "logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE  // ⚠️ CRITICAL for auto-cleanup
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),      // ⚠️ REQUIRED for foreign key
        Index("exerciseId"),  // ⚠️ REQUIRED for foreign key
        Index("date")         // Performance optimization
    ]
)
data class Log(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val exerciseId: Long,
    val weight: Double,  // Always in kg
    val reps: Int,
    val date: Long = System.currentTimeMillis()
)
```

### 4.3 JOIN Queries with @Relation

```kotlin
data class LogWithExercise(
    @Embedded val log: Log,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: Exercise
)

// DAO method
@Transaction
@Query("SELECT * FROM logs WHERE userId = :userId ORDER BY date DESC")
fun getAllLogsWithExerciseForUser(userId: Long): Flow<List<LogWithExercise>>
```

---

## 5. Critical Kotlin/Compose Patterns

### 5.1 Flow Collection - CRITICAL ⚠️

**WRONG - Causes infinite loop:**
```kotlin
scope.launch {
    repository.getData().collect { data ->
        // This creates an observer that never stops!
        // App will freeze here
        processData(data)
    }
}
```

**CORRECT - One-time fetch:**
```kotlin
import kotlinx.coroutines.flow.first  // ⚠️ REQUIRED IMPORT

scope.launch {
    val data = repository.getData().first()
    // Executes once, then continues
    processData(data)
}

// Or with let:
repository.getData().first().let { data ->
    processData(data)
}
```

**When to use each:**
- `.first()` - One-time data fetch (export, calculations)
- `.collect{}` - Continuous observation (UI updates)
- `.collectAsState()` - Compose UI observation

### 5.2 State Management in Compose

**WRONG:**
```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle  // ❌ Not available

val data by flow.collectAsStateWithLifecycle(initialValue = emptyList())  // ❌
```

**CORRECT:**
```kotlin
import androidx.compose.runtime.collectAsState  // ✅

val data by flow.collectAsState(initial = emptyList())  // ✅
//                                ^^^^^^^ NOT initialValue!
```

**Critical Notes:**
- Parameter is `initial`, NOT `initialValue` ⚠️
- Use `collectAsState()` from `androidx.compose.runtime`
- NOT `collectAsStateWithLifecycle()` (not available in this setup)

### 5.3 Experimental APIs

**Material3 Card with onClick:**
```kotlin
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)  // ⚠️ REQUIRED
@Composable
fun MyCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick  // Experimental API
    ) {
        // Content
    }
}
```

### 5.4 ViewModel Initialization

```kotlin
class MainActivity : ComponentActivity() {
    // Correct way to initialize ViewModels
    private val userViewModel: UserViewModel by viewModels()
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private val logViewModel: LogViewModel by viewModels()
}
```

---

## 6. Navigation

### 6.1 Screen Routes

```kotlin
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object UserSelection : Screen("user_selection")
    object ExerciseList : Screen("exercise_list")
    object ExerciseManagement : Screen("exercise_management")
    object LogEntry : Screen("log_entry/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "log_entry/$exerciseId"
    }
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
}
```

### 6.2 Dynamic Start Destination

```kotlin
val startDestination = when {
    preferencesManager.authEnabled && !isAuthenticated -> Screen.Auth.route
    currentUser == null -> Screen.UserSelection.route
    else -> Screen.ExerciseList.route
}

NavHost(
    navController = navController,
    startDestination = startDestination
) {
    // composable destinations
}
```

---

## 7. File Management & Permissions

### 7.1 AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_DOCUMENTS" />

    <application
        android:icon="@drawable/ic_launcher_foreground"
        android:roundIcon="@drawable/ic_launcher_foreground"
        android:label="@string/app_name"
        android:theme="@style/Theme.FitnessTracker">

        <!-- FileProvider for sharing files -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.FitnessTracker">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 7.2 file_paths.xml

Create `app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path name="cache" path="." />
    <files-path name="files" path="." />
</paths>
```

### 7.3 Email Helper

```kotlin
object EmailHelper {
    fun sendEmailWithAttachment(
        context: Context,
        recipientEmail: String,  // Can be empty - user enters manually
        subject: String,
        body: String,
        file: File
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Send email"))
    }
}
```

---

## 8. UI Design Guidelines

### 8.1 Screens Required

1. **AuthScreen** - PIN setup and entry
2. **UserSelectionScreen** - Create, select, delete users
3. **ExerciseListScreen** - View exercises (alphabetical or grouped)
4. **ExerciseManagementScreen** - CRUD operations, CSV import/export
5. **LogEntryScreen** - Add workout log, view last 3 logs, PR display
6. **DashboardScreen** - Progress charts, PR tracking per exercise
7. **SettingsScreen** - Switch user, toggle kg/lbs, toggle auth, export logs

### 8.2 Common UI Patterns

**Loading State:**
```kotlin
if (isLoading) {
    CircularProgressIndicator()
} else {
    // Content
}
```

**Empty State:**
```kotlin
if (items.isEmpty()) {
    Text(
        text = "No exercises found. Add your first exercise!",
        modifier = Modifier.padding(16.dp)
    )
} else {
    LazyColumn { /* items */ }
}
```

**Confirmation Dialog:**
```kotlin
AlertDialog(
    onDismissRequest = { /* close */ },
    title = { Text("Delete Exercise?") },
    text = { Text("This will delete all logs for this exercise.") },
    confirmButton = {
        TextButton(onClick = { /* delete */ }) {
            Text("Delete")
        }
    },
    dismissButton = {
        TextButton(onClick = { /* cancel */ }) {
            Text("Cancel")
        }
    }
)
```

---

## 9. Testing Strategy

### 9.1 Manual Testing Priorities
1. User creation/deletion with email export
2. Exercise CRUD operations
3. CSV import/export
4. Workout logging and PR detection
5. Dashboard accuracy
6. Unit conversion consistency
7. Authentication flow

### 9.2 Edge Cases to Test
- Same weight, different reps (is it a PR?)
- Delete user with 0 logs
- Import malformed CSV
- Delete exercise with many logs
- Switch users with different log histories
- Toggle units mid-session

### 9.3 Performance Testing
- 100+ exercises (hierarchical view)
- 500+ logs (dashboard load time)
- Rapid navigation between screens

**Full testing guide available in:** `TESTING_GUIDE.md`

---

## 10. Common Pitfalls & Solutions

| Pitfall | Why It Happens | Solution |
|---------|----------------|----------|
| `.collect{}` causes freeze | Creates infinite observer | Use `.first()` for one-time fetch ⚠️ |
| `collectAsStateWithLifecycle` not found | Not available in this setup | Use `collectAsState()` instead ⚠️ |
| `initialValue` parameter error | Wrong parameter name | Use `initial` ⚠️ |
| Windows build fails (jlink) | SDK 34 incompatibility | Use GitHub Actions (Linux) ⚠️ |
| Gradle wrapper missing | Not committed to repo | Generate and commit wrapper files ⚠️ |
| SDK 33 dependency error | Libraries require SDK 34 | Use `compileSdk = 34` ⚠️ |
| Foreign key constraint fails | Missing indices | Add `@Index` on foreign key columns ⚠️ |
| Card onClick error | Experimental API | Add `@OptIn(ExperimentalMaterial3Api::class)` ⚠️ |
| CSV import crashes | No error handling | Wrap in try-catch blocks ⚠️ |
| gradle.properties breaks CI | Hardcoded paths | Remove environment-specific paths ⚠️ |

---

## 11. Project Structure

```
fitness-tracker/
├── .github/
│   └── workflows/
│       └── build.yml                 # ⚠️ REQUIRED for CI/CD
├── app/
│   ├── build.gradle.kts              # App-level build config
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/fitness/tracker/
│           │   ├── MainActivity.kt
│           │   ├── data/
│           │   │   ├── database/
│           │   │   │   ├── AppDatabase.kt
│           │   │   │   ├── dao/
│           │   │   │   │   ├── UserDao.kt
│           │   │   │   │   ├── ExerciseDao.kt
│           │   │   │   │   └── LogDao.kt
│           │   │   │   └── entity/
│           │   │   │       ├── User.kt
│           │   │   │       ├── Exercise.kt
│           │   │   │       ├── Log.kt
│           │   │   │       └── LogWithExercise.kt
│           │   │   └── repository/
│           │   │       ├── UserRepository.kt
│           │   │       ├── ExerciseRepository.kt
│           │   │       └── LogRepository.kt
│           │   ├── ui/
│           │   │   ├── screen/
│           │   │   │   ├── AuthScreen.kt
│           │   │   │   ├── UserSelectionScreen.kt
│           │   │   │   ├── ExerciseListScreen.kt
│           │   │   │   ├── ExerciseManagementScreen.kt
│           │   │   │   ├── LogEntryScreen.kt
│           │   │   │   ├── DashboardScreen.kt
│           │   │   │   └── SettingsScreen.kt
│           │   │   ├── viewmodel/
│           │   │   │   ├── UserViewModel.kt
│           │   │   │   ├── ExerciseViewModel.kt
│           │   │   │   └── LogViewModel.kt
│           │   │   └── theme/
│           │   │       └── Theme.kt
│           │   ├── navigation/
│           │   │   └── Screen.kt
│           │   └── util/
│           │       ├── CsvHelper.kt
│           │       ├── EmailHelper.kt
│           │       ├── UnitConverter.kt
│           │       └── PreferencesManager.kt
│           └── res/
│               ├── drawable/
│               │   └── ic_launcher_foreground.xml
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── xml/
│                   └── file_paths.xml
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar        # ⚠️ REQUIRED
│       └── gradle-wrapper.properties # ⚠️ REQUIRED
├── build.gradle.kts                  # Project-level build config
├── settings.gradle.kts
├── gradle.properties                 # ⚠️ NO environment-specific paths
├── gradlew                           # ⚠️ REQUIRED for CI/CD
├── gradlew.bat                       # ⚠️ REQUIRED for Windows
├── TESTING_GUIDE.md
├── REQUIREMENTS_ENHANCED.md          # This document
└── README.md
```

---

## 12. Development Workflow

### Initial Setup
1. Clone repository
2. Verify Gradle wrapper exists
3. Check gradle.properties (no hardcoded paths)
4. Sync project in IDE (or run `./gradlew build`)
5. Push to trigger GitHub Actions build
6. Download APK from Actions artifacts

### Making Changes
1. Create feature branch
2. Make code changes
3. Test locally if possible (Linux recommended)
4. Commit and push
5. GitHub Actions builds automatically
6. Download APK to test on device

### Before Every Commit
- Check for `.collect{}` usage - should it be `.first()`? ⚠️
- Verify `collectAsState(initial = ...)` NOT `initialValue` ⚠️
- Confirm SDK versions are 34 ⚠️
- No environment-specific paths in config files ⚠️
- Experimental APIs have `@OptIn` annotations ⚠️

---

## 13. Future Enhancements (Optional)

- [ ] Log edit dialog implementation (currently shows toast)
- [ ] CSV import error handling with user-friendly messages
- [ ] PR logic: consider both weight AND reps
- [ ] Backup/restore entire database
- [ ] Exercise categories beyond body part
- [ ] Progress photos
- [ ] Workout plans/templates
- [ ] Rest timer between sets
- [ ] Export to Google Fit / Apple Health
- [ ] Dark mode toggle
- [ ] Multi-language support

---

## 14. Resources

**Official Documentation:**
- Android Developers: https://developer.android.com/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Room Database: https://developer.android.com/training/data-storage/room
- GitHub Actions: https://docs.github.com/en/actions

**Critical Reading:**
- Kotlin Flow: https://developer.android.com/kotlin/flow
- Compose State: https://developer.android.com/jetpack/compose/state
- Material 3: https://m3.material.io/

---

## 15. Summary Checklist

Before starting development, ensure:

- [x] GitHub Actions workflow configured
- [x] Gradle wrapper files committed
- [x] gradle.properties has no hardcoded paths
- [x] compileSdk and targetSdk set to 34
- [x] Room entities have proper foreign keys and indices
- [x] Using `.first()` for one-time data fetches (NOT `.collect{}`)
- [x] Using `collectAsState(initial = ...)` (NOT `initialValue`)
- [x] Experimental APIs have `@OptIn` annotations
- [x] Weight stored only in kg, converted for display
- [x] FileProvider configured for email attachments
- [x] All screens implemented per requirements
- [x] Testing guide created and followed

---

**Document Version:** 2.0
**Last Updated:** 2025-11-06
**Author:** Development team with lessons learned
**Status:** Production-ready specification

---

## 16. Quick Reference Card

**Most Critical Rules:**

1. ⚠️ **Flow collection:** `.first()` for one-time, `.collect{}` for continuous
2. ⚠️ **State parameter:** `collectAsState(initial = ...)` NOT `initialValue`
3. ⚠️ **SDK versions:** MUST be 34
4. ⚠️ **Build system:** GitHub Actions on Linux (Windows jlink broken)
5. ⚠️ **Weight storage:** ONLY kg in database
6. ⚠️ **Foreign keys:** MUST have `@Index` on columns
7. ⚠️ **Gradle config:** NO environment-specific paths
8. ⚠️ **Experimental APIs:** Need `@OptIn` annotation

**When in doubt, refer to this document!**
