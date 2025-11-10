# NourishFit: AI-Powered Fitness & Diet Tracker

NourishFit is a modern, all-in-one health application built with Kotlin and Jetpack Compose. It combines local-first data storage with powerful cloud and AI features, allowing users to track their diet, log their runs, and receive intelligent feedback to reach their fitness goals.

This project was built to satisfy requirements for an advanced mobile development course, incorporating local databases, cloud services, and on-device machine learning.

## Features

NourishFit is packed with features that are integrated into a seamless, multi-screen user experience.

### Core UI & Navigation

- Modern UI: Built entirely with Jetpack Compose and Material 3 design principles.

- Animated Splash Screen: A beautiful, animated app icon greets the user on a cold start.

- Bottom Navigation: A clean, 5-tab bottom navigation bar for easy access to all main features.

- Edge-to-Edge Display: The app gracefully draws behind the system status and navigation bars for an immersive, modern feel.

### 🍽️ Diet & Macro Tracking

- Daily Food Log: Users can add, view, and delete food items for any given day.

- Full Macro Tracking: Each food entry stores Calories, Protein, Carbs, and Fat.

- Preset Food Database: A built-in list of 50+ common foods (apples, chicken, protein shakes) allows for "quick add" auto-filling of all macro fields.

- Live Summary: An animated Calorie Goal Ring on the main screen provides instant, visual feedback on the user's progress toward their daily goal.

- Smart Grouping: The daily food list is automatically categorized by meal type (Breakfast, Lunch, Dinner) based on keywords in the food's name.

### 🤖 AI & Machine Learning Features

- ML Food Scanner (On-Device):

- Uses CameraX to provide a live camera feed.

- Integrates Firebase ML Kit's Image Labeling to perform real-time, on-device object recognition.

- The UI displays the "live label" (e.g., "Apple") as the user points their camera.

- A "Scan & Confirm" interface allows the user to edit the AI's guess before adding it to their log.

### AI Fitness Coach (Cloud AI):

- A "gemini.google.com-esque" chat interface (ChatScreen) allows users to ask for diet and workout plans.

- Powered by the Google Gemini API (gemini-2.5-flash-preview-09-2025) with a custom system prompt to act as a friendly fitness coach.

### AI-Based Alerts & Notifications:

- Uses Android's WorkManager to run a "smart" background task once a day.

- This worker queries the local Room database to check if the user has logged any food.

- If foodCount == 0, it sends a proactive, helpful notification to remind the user to log their meals. This is a data-driven alert, not a "dumb" timer.

### 🏃‍♂️ GPS Activity Tracking

- Open-Source Maps: Uses OSMDroid to display a fully functional, open-source map, avoiding any Google Maps billing.

- Live Route Tracing: When the user starts a run, the app tracks their GPS location and draws a live polyline of their route on the map, similar to Strava.

- Live Stats: Displays a real-time-updating dashboard for Distance, Time, and Pace.

- "My Location" & Centering: The map automatically centers on the user's current location when the screen first opens.

### 👤 User Profile & Data

- Full User Authentication:

- Email/Password: Complete sign-up and login flow using Firebase Authentication.

- Anonymous "Guest Mode": New users are automatically signed in anonymously so they can use the app immediately.

- Login to Sync: A prompt on the main screen encourages guest users to create a permanent account to sync their data.

- Personalized Profile Screen:

- Users can enter their Age, Weight, Height, and Activity Level.

- The app uses the Harris-Benedict equation to calculate the user's personal TDEE (Total Daily Energy Expenditure).

### Data Persistence & Syncing:

- Offline-First with Room: All food logs, run history, and weight entries are saved to a local Room Database, making the app fast and fully functional offline.

- Cloud Sync with Firestore: All data saved to Room is also backed up to a user-specific collection in Cloud Firestore. This fulfills the "data synchronization" requirement and allows data to be restored.

- Profile Sync: User profile data (height, weight, etc.) is saved directly to Firestore.

- Data-Driven UI: The DietTrackerScreen reads the user's TDEE from Firestore to update the calorie goal ring, creating a fully connected and personalized experience.

## Tech Stack & Architecture

- Language: 100% Kotlin

- UI: Jetpack Compose & Material 3

- Architecture: MVVM (Model-View-ViewModel)

- Asynchronous: Coroutines & Flow

- Navigation: Jetpack Navigation for Compose

- Local Database: Room

- Cloud Database: Cloud Firestore

- Authentication: Firebase Authentication (Email & Anonymous)

- AI (Chatbot): Google Gemini API

- AI (Alerts): WorkManager

- AI (ML Scanner): Firebase ML Kit & CameraX

- Maps: OSMDroid (Open Source)

- Image Loading: Coil

## 🚀 Getting Started

This guide is for future contributors who want to run the project locally.

### Prerequisites

- Android Studio: The latest stable version (e.g., "Hedgehog" or newer).

- Git: For cloning the repository.

- Physical Android Device (Recommended): Required for testing GPS and camera features.

### Installation

- Clone the repository:

```powershell
git clone [https://github.com/YOUR_USERNAME/NourishFit.git](https://github.com/YOUR_USERNAME/NourishFit.git)
```

- Open the project in Android Studio.

- Let Gradle sync and download all the dependencies.

### 🔥 Firebase Setup (Required)

- This app relies on Firebase for its core features.

- Go to the Firebase Console and create a new project.

- In the project dashboard, click the Android icon to add a new Android app.

- Use the package name: com.example.nourishfit

- Follow the setup steps and download the google-services.json file.

- Place this file in the app/ directory of the project.

- In the Firebase Console, go to the Authentication section and enable the Email/Password and Anonymous sign-in providers.

- Go to the Firestore Database section and create a new database. Start in Test Mode.

### 🤖 Gemini API Key Setup (Required)

- The AI Chatbot will not work without this.

- Go to Google AI Studio.

- Log in and click "Get API key".

- Create and copy your new API key.

- In the Android Studio project, navigate to app/src/main/res/values/.

- Create a new file named secrets.xml.

- Paste the following code into secrets.xml, replacing YOUR_API_KEY_HERE with the key you just copied:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="gemini_api_key" translatable="false">YOUR_API_KEY_HERE</string>
</resources>
```

## 🔒 Security

This project is set up to keep keys safe. The following files are included in .gitignore and must not be committed to version control:

```path
local.properties

/app/google-services.json

/app/src/main/res/values/secrets.xml
```

After completing these steps, you can build and run the app on your device.

## Contributions

Contributions are welcome! 
Follow the steps to clone the projects and work on your feature fork. 
PRs are welcome! 

- Andrew Fernandes 
