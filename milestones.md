# Milestones

## Completed Milestones

This section confirms the requirements that have already been successfully implemented in the application.

- [x] Req. #1: Basic Mobile Application with UI Basics: COMPLETE. The app has a multi-screen structure, a theme, and uses fundamental Jetpack Compose UI components.

- [x] Req. #2: Multi-Screen Navigation & UI Components: COMPLETE. The app uses a multi-level navigation graph (Home/Login -> Main App) and a bottom navigation bar for its main features, demonstrating multiple navigation patterns.

- [x] Req. #3: Local Database (Room): COMPLETE. The app successfully uses a Room database to store, retrieve, and delete user-specific food data locally on the device.

## 🔜 Next Steps: In-Progress & Planned Features

This is the prioritized list of features to be implemented.

### Priority 1: Complete Core GPS Functionality

This is the most important next step as the UI is already in place.

- [ ] 1. Implement GPS Run Tracker (Req. #6 & #7 - GPS)

Requirement: "Apply Google Maps for campus navigation" and incorporate "GPS services."

Status: IN PROGRESS. The map is visible, and permissions are handled. The next step is to implement the timer and stat calculations.

Procedure:

Implement Timer: In StepTrackerViewModel, create a coroutine-based timer (stopwatch) that starts and stops with tracking.

Calculate Distance: As new GeoPoint locations are received, calculate the distance traveled between points.

Calculate Pace: Use the live time and distance to calculate the user's average pace.

Update UI: Expose the live stats to the StepTrackerScreen to replace the placeholders.

### Priority 2: Implement Cloud & Network Features

- [ ] 1. Implement Data Synchronization with Firestore (Req. #4 & #5 - Cloud Sync & Network)

Requirement: "Perform data synchronization with Firebase/Cloud" and "Implement appropriate network connectivity for data exchange."

Status: Not started.

Procedure:

Setup Firestore: Enable the Cloud Firestore database in your Firebase console.

Modify Repository: Update your FoodRepository and create a RunRepository. When data is saved locally to Room, also save a copy to a user-specific collection in Firestore.

Implement Offline Sync: Enable Firestore's offline persistence to handle automatic syncing when the network is available.

- [ ] 2. Save Completed Runs (Req. #7 - Multimedia)

Requirement: Incorporate "multimedia" (a static map image of a completed run is a form of multimedia).

Status: Not started.

Procedure:

Create RunEntity: Define a new table in Room to store run details (distance, time, date, userId, and the route).

Save on Stop: In StepTrackerViewModel, when a run is stopped, save the completed RunEntity to Room and Firestore.

Display in Progress: Create a new section in the ProgressScreen to show a list of past runs, each linking to a detail view with a map image of the route.

### Priority 3: Integrate AI & Machine Learning

- [ ] 1. Implement ML Food Scanner (Req. #9 - ML Features)

Requirement: "Apply ML-based features in the given application."

Status: UI placeholder is complete.

Procedure:

Add Dependencies: Integrate CameraX for camera access and Firebase ML Kit's Image Labeling.

Launch Camera: Make the camera FAB functional by launching a camera view.

Process Image: Send the captured picture to the Firebase ML model to get labels (e.g., "Apple").

Pre-fill Dialog: Use the best label to pre-fill the name in the AddFoodDialog.

- [ ] 2. Build the AI Chatbot (Req. #8 - AI Chatbot)

Requirement: "Develop an AI chatbot application."

Status: UI placeholder is complete.

Procedure:

Integrate Gemini API: Add the Google AI SDK to your project.

Ground the Prompt: In a ProgressViewModel, when a user asks a question, first query the local Room database for relevant data (e.g., their food logs).

Call Gemini: Send a prompt to the Gemini API that includes both the user's question and the data you fetched, asking it to provide a conversational answer.

- [ ] 3. Create AI-Powered Alerts (Req. #10 - AI Alerts)

Requirement: "Build an AI-based alerts and notifications system."

Status: UI placeholder is complete.

Procedure:

Use WorkManager: Schedule a periodic background task (e.g., daily).

Implement Logic: The worker will query the Room database and apply simple logic (e.g., IF breakfast was missed THEN send notification).

Show Notification: Use the Android notification system to display the helpful alert.