🧠 QuizMaster – Mobile Trivia App
Your intelligent quiz companion for learning, fun, and challenge.


Overview

QuizMaster is a gamified Android trivia app built with Kotlin, Firebase, and Jetpack Architecture Components.
It’s designed to engage users through interactive quizzes, achievements, and live multiplayer matches — powered by real-time updates.

Key Features:

--Firebase Authentication (Email, Google Sign-In, Biometrics)

--Solo & Multiplayer Game Modes

--Real-time Leaderboard & XP Progression

--Quest Challenges & Reward System

--Live Firestore Integration

--Category-based Question Bank

--Coins, Levels, and XP system

--Bottom Navigation Dashboard

--Notifications and Biometric Login

--Cloud data persistence with Firestore

--Architecture

QuizMaster follows a modular MVVM (Model–View–ViewModel) architecture:

student.projects.quizmaster01/
│
├── ui/                 → Fragments & Activities (UI Layer)
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   ├── HomeFragment.kt
│   ├── PlayFragment.kt
│   ├── GameFragment.kt
│   ├── QuestsFragment.kt
│   ├── LeaderboardFragment.kt
│   └── ProfileFragment.kt
│
├── viewmodels/         → ViewModels managing state (e.g. HomeViewModel)
├── models/             → Data classes (UserStatus, Quest, PlayerRank, etc.)
├── adapters/           → RecyclerView Adapters for lists
└── utils/              → Utility helpers and constants


This structure ensures scalability, separation of concerns, and easy testing.

--Tech Stack
Layer	Technology
Language:Kotlin (Android SDK 34+)
UI	Jetpack Compose-ready XML layouts, Material Design 3
Architecture:MVVM + LiveData + ViewModel
Backend	Firebase Authentication & Firestore
Storage	Firebase Firestore & SharedPreferences
CI/CD:GitHub Actions
Testing	JUnit, Espress
Version Control:Git & GitHub

====================Getting Started=========================
1️⃣ Prerequisites

Android Studio Ladybug (or newer)

JDK 17

Gradle 8.0+

A configured Firebase project

--Clone the repository
git clone https://github.com/st10438146/QuizMaster0.1.git
cd QuizMaster

--Connect to Firebase

Go to Firebase Console

Create a project named QuizMaster

Add an Android app with your package name (e.g., student.projects.quizmaster01)

Download google-services.json

Place it in app/ folder

4️⃣ Build and Run
./gradlew clean assembleDebug


or in Android Studio → Run ▶

--Core Modules Explained
--HomeFragment

Displays user XP, coins, quests, and buttons for solo/multiplayer modes.

--PlayFragment

Lets users select categories and start solo or multiplayer games.

--GameFragment

Handles quiz questions, multiple-choice options, timers, and results.

--QuestsFragment

Real-time listener for user challenges and XP rewards from Firestore.

--LeaderboardFragment

Ranks players dynamically using Firestore snapshot updates.

--ProfileFragment

User settings, achievements, biometrics toggle, and logout button.

--Authentication Options
Method	Description
Email/Password	Standard Firebase Auth
Google Sign-In	OAuth via FirebaseAuth + Google API
Biometrics	Secure access using AndroidX BiometricPrompt
--Testing
Unit Tests
./gradlew testDebugUnitTest

Instrumentation Tests
./gradlew connectedAndroidTest

--CI/CD with GitHub Actions

The project includes an automated CI workflow located at:

.github/workflows/android-ci.yml


It runs on each push or pull request to:

✅ Build the APK

✅ Run unit tests

✅ Upload the built APK as an artifact

You can view it under your repository’s Actions tab.

--Firestore Data Structure
users/
  └── {userId}/
       ├── username: "Siyabonga"
       ├── xp: 1200
       ├── coins: 300
       └── activeQuestsCount: 2

quests/
  └── quest1/
       ├── title: "Win 5 Games"
       ├── description: "Complete 5 solo games"
       ├── progress: 60
       ├── rewardXP: 100
       └── isCompleted: false

==Folder Structure
app/
 ├── java/student/projects/quizmaster01/
 │   ├── ui/
 │   ├── viewmodels/
 │   ├── adapters/
 │   ├── models/
 │   └── utils/
 ├── res/
 │   ├── layout/
 │   ├── drawable/
 │   ├── values/
 │   └── navigation/
 └── AndroidManifest.xml

--Development Roadmap

 Firebase Email Authentication

 Google Sign-In

 Biometric Login

 Home Dashboard

 Real-time Leaderboard

 Quest System

 Multiplayer Matchmaking

 Push Notifications

 Play Store Deployment

==Environment Setup
Required Secrets for CI/CD 
Name	Purpose
FIREBASE_APP_ID	Firebase App Distribution
FIREBASE_TOKEN	Auth token for CI deploys

Add these in:

GitHub → Repo → Settings → Secrets → Actions

========================Author============================

PixelPartners
