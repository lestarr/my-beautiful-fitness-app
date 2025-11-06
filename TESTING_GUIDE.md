# Fitness Tracker App - Testing Guide

## Critical Bugs Fixed

### 1. User Deletion Flow Bug (FIXED)
**Location:** MainActivity.kt:136
**Issue:** Used `.collect {}` instead of `.first()` causing infinite loop
**Status:** ✅ Fixed - Now uses `.first().let {}`

### 2. Log Export Flow Bug (FIXED)
**Location:** MainActivity.kt:344
**Issue:** Used `.collect {}` instead of `.first()` causing app hang
**Status:** ✅ Fixed - Now uses `.first().let {}`

### 3. Email Recipient Issue (KNOWN)
**Location:** MainActivity.kt:147, 354
**Issue:** Empty string `""` for recipient email
**Impact:** User must manually enter email address when sharing
**Status:** ⚠️ By design - allows user to choose recipient

### 4. CSV Import Error Handling (KNOWN)
**Location:** MainActivity.kt:215
**Issue:** No try-catch for malformed CSV files
**Impact:** App may crash on invalid CSV
**Status:** ⚠️ Needs improvement

---

## Test Plan by Feature

### Feature 1: Authentication (PIN)

#### Test 1.1: First Launch - Setup PIN
**Steps:**
1. Launch app for first time
2. Should see "Setup PIN" screen
3. Enter 4-digit PIN (e.g., 1234)
4. Confirm PIN
5. Should navigate to User Selection screen

**Expected Result:** PIN saved, authentication enabled

#### Test 1.2: Authentication Required
**Steps:**
1. Enable auth in Settings
2. Close and reopen app
3. Should see "Enter PIN" screen
4. Enter correct PIN

**Expected Result:** Access granted to app

#### Test 1.3: Wrong PIN
**Steps:**
1. Launch app with auth enabled
2. Enter wrong PIN

**Expected Result:** Error message, retry allowed

---

### Feature 2: User Management

#### Test 2.1: Create First User
**Steps:**
1. Launch app (after auth if enabled)
2. Should see User Selection screen
3. Click "Add User" button
4. Enter name: "John Doe"
5. Click Save
6. Select "John Doe" from list
7. Click "Continue"

**Expected Result:** User created, navigates to Exercise List screen

#### Test 2.2: Switch User
**Steps:**
1. Navigate to Settings
2. Click "Switch User"
3. Select different user
4. Click "Continue"

**Expected Result:** Current user changes, shows their logs in dashboard

#### Test 2.3: Delete User with Email Export
**Steps:**
1. Go to User Selection screen
2. Long-press or swipe user to delete
3. Confirm deletion
4. Email app should open with CSV attachment

**Expected Result:**
- User deleted from database
- CSV file attached to email (if user has logs)
- Email subject: "Fitness Logs Export - [Username]"
- Empty recipient field (user must enter address)

#### Test 2.4: Delete User with No Logs
**Steps:**
1. Create new user
2. Don't add any logs
3. Delete user

**Expected Result:** User deleted, no email export (no logs to export)

---

### Feature 3: Exercise Management

#### Test 3.1: View Empty Exercise List
**Steps:**
1. With no exercises in database
2. View Exercise List screen

**Expected Result:** Empty state with "Add Exercise" button

#### Test 3.2: Add Exercise Manually
**Steps:**
1. Click "Manage Exercises" from Exercise List
2. Click "Add Exercise" button
3. Enter name: "Bench Press"
4. Select body part: "Chest"
5. Click Save

**Expected Result:**
- Toast: "Exercise added"
- Exercise appears in list
- List shows alphabetically (if <20 exercises)

#### Test 3.3: Edit Exercise
**Steps:**
1. In Exercise Management screen
2. Click on existing exercise
3. Change name or body part
4. Click Save

**Expected Result:**
- Toast: "Exercise updated"
- Changes reflected in list

#### Test 3.4: Delete Exercise
**Steps:**
1. In Exercise Management screen
2. Long-press or swipe exercise
3. Confirm deletion

**Expected Result:**
- Toast: "Exercise deleted"
- Exercise removed from list
- Associated logs also deleted (CASCADE)

#### Test 3.5: Alphabetical List (<20 exercises)
**Steps:**
1. Have 15 exercises in database
2. View Exercise List screen

**Expected Result:**
- Flat alphabetical list
- All exercises visible

#### Test 3.6: Hierarchical List (≥20 exercises)
**Steps:**
1. Have 25 exercises in database
2. View Exercise List screen

**Expected Result:**
- Grouped by body part
- Expandable sections
- Organized hierarchically

---

### Feature 4: CSV Import/Export

#### Test 4.1: Export Exercises to CSV
**Steps:**
1. Go to Exercise Management
2. Click "Export CSV" button
3. Choose save location
4. Save file

**Expected Result:**
- CSV file created with format: `name,bodyPart`
- Toast: "Exercises exported"

#### Test 4.2: Import Exercises from CSV
**Steps:**
1. Create CSV file with format:
   ```
   name,bodyPart
   Squat,Legs
   Deadlift,Back
   ```
2. Go to Exercise Management
3. Click "Import CSV" button
4. Select CSV file

**Expected Result:**
- Toast: "Imported 2 exercises"
- Exercises added to database
- Visible in Exercise List

#### Test 4.3: Import Invalid CSV
**Steps:**
1. Create malformed CSV (missing columns, wrong format)
2. Try to import

**Expected Result:**
- ⚠️ **KNOWN ISSUE**: May crash
- Should show error message (needs fix)

---

### Feature 5: Workout Logging

#### Test 5.1: Log First Set for Exercise
**Steps:**
1. Click on "Bench Press" from Exercise List
2. Enter weight: 60 (or 132 lbs)
3. Enter reps: 10
4. Click "Save"

**Expected Result:**
- Toast: "New PR! Log saved" (first log is always PR)
- Log entry created with auto-filled date
- Returns to Exercise List

#### Test 5.2: View Last 3 Logs
**Steps:**
1. Add 5 logs for "Bench Press"
2. Click on "Bench Press" again

**Expected Result:**
- Screen shows last 3 logs in chronological order
- Each log shows: weight, reps, date

#### Test 5.3: New Personal Record
**Steps:**
1. Previous PR: 60kg x 10 reps
2. Log new set: 65kg x 8 reps

**Expected Result:**
- Toast: "New PR! Log saved"
- PR updated in Dashboard

#### Test 5.4: Not a Personal Record
**Steps:**
1. Previous PR: 65kg
2. Log new set: 60kg x 10 reps

**Expected Result:**
- Toast: "Log saved" (no PR message)
- Log saved normally

#### Test 5.5: Delete Log
**Steps:**
1. In Log Entry screen
2. Long-press on one of last 3 logs
3. Click "Delete"
4. Confirm

**Expected Result:**
- Toast: "Log deleted"
- Log removed from list
- PR recalculated if necessary

#### Test 5.6: Edit Log
**Steps:**
1. In Log Entry screen
2. Click on one of last 3 logs

**Expected Result:**
- ⚠️ **KNOWN ISSUE**: Shows toast "Edit functionality - to be implemented via dialog"
- Not yet implemented (line 279)

---

### Feature 6: Dashboard & PR Tracking

#### Test 6.1: View Empty Dashboard
**Steps:**
1. User with no logs
2. Navigate to Dashboard

**Expected Result:** Empty state message

#### Test 6.2: View Exercise Progress
**Steps:**
1. User has logs for multiple exercises
2. Navigate to Dashboard
3. View each exercise card

**Expected Result:**
- One card per exercise (only exercises with logs)
- Shows weight progression over time
- Displays current PR
- Chart/graph of progress

#### Test 6.3: Click Exercise in Dashboard
**Steps:**
1. In Dashboard
2. Click on exercise card

**Expected Result:**
- Navigates to Log Entry screen for that exercise

#### Test 6.4: PR Calculation Accuracy
**Steps:**
1. Add logs: 50kg, 55kg, 60kg, 55kg, 65kg
2. Check Dashboard

**Expected Result:**
- PR shows 65kg (highest weight)
- Progress chart shows all entries

---

### Feature 7: Unit Conversion (kg/lbs)

#### Test 7.1: Toggle to lbs
**Steps:**
1. Settings screen
2. Toggle "Use kg" to OFF (use lbs)
3. Navigate to Log Entry screen

**Expected Result:**
- All weights displayed in lbs
- Input accepts lbs values
- Stored as kg in database (1kg = 2.20462lbs)

#### Test 7.2: Toggle to kg
**Steps:**
1. Settings screen
2. Toggle "Use kg" to ON
3. Navigate to Log Entry screen

**Expected Result:**
- All weights displayed in kg
- Input accepts kg values

#### Test 7.3: Conversion Consistency
**Steps:**
1. Log: 60kg
2. Switch to lbs mode
3. Check same log

**Expected Result:**
- Should show ~132.3 lbs
- Conversion accurate across all screens

---

### Feature 8: Export User Logs

#### Test 8.1: Export via Settings
**Steps:**
1. User has 20 logs across multiple exercises
2. Navigate to Settings
3. Click "Export My Logs"

**Expected Result:**
- Email app opens
- CSV file attached
- Subject: "My Fitness Logs Export"
- Body: "Please find attached your fitness logs."
- Empty recipient (user enters address)

#### Test 8.2: Export Format Validation
**Steps:**
1. Export logs as CSV
2. Open CSV file

**Expected Result:**
- Format: `date,exercise,bodyPart,weight(kg),reps`
- All user's logs included
- Properly formatted CSV

---

## Performance Testing

### Test P1: Large Dataset (100 exercises)
**Steps:**
1. Import CSV with 100 exercises
2. Navigate Exercise List

**Expected Result:**
- Hierarchical view displayed
- Smooth scrolling
- No lag

### Test P2: Heavy Log History (500 logs)
**Steps:**
1. User has 500 logs
2. Open Dashboard

**Expected Result:**
- Loads within 2 seconds
- Charts render smoothly

---

## Edge Cases

### Edge 1: Same Weight, Different Reps
**Steps:**
1. Previous: 60kg x 10 reps
2. Log: 60kg x 12 reps

**Expected Result:**
- ⚠️ **NEEDS VERIFICATION**: Is this a PR?
- Current logic: Only compares weight, not reps

### Edge 2: Network/Storage Permissions
**Steps:**
1. Deny storage permission
2. Try to export CSV

**Expected Result:**
- Should show permission denied error
- Graceful failure

### Edge 3: Empty Fields in Log Entry
**Steps:**
1. Try to save log without entering weight or reps

**Expected Result:**
- Validation error
- Cannot save incomplete log

---

## Automated Testing Notes

**Unit Tests Needed:**
- `UnitConverter.kgToLbs()` and `lbsToKg()`
- PR calculation logic in `LogViewModel`
- CSV parsing in `CsvHelper`

**Integration Tests Needed:**
- User CRUD operations
- Exercise CRUD operations
- Log creation and deletion
- Flow collection for Dashboard

**UI Tests Needed:**
- Navigation flows
- Form validation
- Authentication flow

---

## Testing on Physical Device

### Prerequisites
1. Download APK from GitHub Actions artifacts
2. Enable "Install from Unknown Sources" on Android device
3. Transfer APK to device
4. Install app

### Recommended Test Order
1. Authentication setup
2. Create first user
3. Add 5 exercises manually
4. Import exercise CSV (20+ exercises)
5. Log workouts for 3 different exercises
6. Test unit conversion
7. View dashboard
8. Export logs
9. Switch user
10. Delete user with export

### Test Data Files

**Sample exercises.csv:**
```csv
name,bodyPart
Bench Press,Chest
Incline Press,Chest
Squat,Legs
Deadlift,Back
Shoulder Press,Shoulders
Barbell Row,Back
Bicep Curl,Arms
Tricep Extension,Arms
Leg Press,Legs
Lat Pulldown,Back
```

**Sample logs.csv (for validation):**
```csv
date,exercise,bodyPart,weight(kg),reps
2025-11-01,Bench Press,Chest,60.0,10
2025-11-03,Bench Press,Chest,62.5,8
2025-11-05,Bench Press,Chest,65.0,6
```

---

## Bug Reporting Template

If you find issues during testing, please note:

**Bug Title:**
**Severity:** Critical / High / Medium / Low
**Steps to Reproduce:**
1.
2.
3.

**Expected Result:**
**Actual Result:**
**Screenshots:** (if applicable)
**Device Info:** Android version, device model
**App Version:** Check GitHub commit hash

---

## Summary of Known Issues

| Issue | Severity | Status | Location |
|-------|----------|--------|----------|
| User deletion infinite loop | Critical | ✅ Fixed | MainActivity.kt:136 |
| Export logs hang | Critical | ✅ Fixed | MainActivity.kt:344 |
| Empty email recipient | Low | By design | MainActivity.kt:147,354 |
| CSV import no error handling | Medium | Open | MainActivity.kt:215 |
| Edit log not implemented | Medium | Open | MainActivity.kt:279 |
| PR logic (weight only) | Low | To verify | LogViewModel |

---

**Last Updated:** 2025-11-06
**Tested Version:** Latest commit on `claude/fitness-tracking-app-011CUpoo8MYG4Z1XktP5Y3Ga`
