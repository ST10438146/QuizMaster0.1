📱 QuizMaster App — README

A mobile quiz game built using Kotlin, Android Studio, Firebase Authentication, 
Firestore, and supports multilingual UI (English & Afrikaans).
Players can complete quizzes, earn XP, unlock badges, complete 
daily/weekly quests, and compete on the global leaderboard.

==============================✨ Features ==============================
----------🎮 Gameplay ----------

#Multiple quiz categories

#Real-time progress tracking

#Score and XP calculation

#Game Over summary with XP earned and badges

----------🏆 Leaderboard ----------

#Shows top players, XP, levels, badges

#Medal icons for Top 3

#Daily / Weekly leaderboard mode

----------🎯 Quests System ----------

#Daily & weekly tasks (auto-updated from Firestore)

#Includes goal, progress, reward XP

#Swipe-to-refresh to fetch new quests

----------👤 Player Profile ----------

XP and badges summary

#Biometric login toggle

#Notifications toggle

#Manual language switcher (English/Afrikaans)

----------🌍 Multilingual Support ----------

##All UI text translated into:

#English

#Afrikaans

#Language chosen manually from Profile → Settings → Language

=============================🛠️ Prerequisites =============================

##Before running the project, ensure you have:

     Requirement	                   Version
   Android Studio	             Ladybug / Koala (2024+)
Android Gradle Plugin	                8.x
       Kotlin	                        1.9+
    Minimum SDK	                        21+
  Firebase Account	                  Required
Google Services JSON	              Required


=============================🚀 How to Compile & Run the App==============================

##Follow these steps exactly:

#1️.Clone the Repository
   git clone https://github.com/your-username/quizmaster.git
   cd quizmaster

#2.️Open in Android Studio

   #Open Android Studio

   #Click Open an Existing Project

   #Select this project folder

   #Let Gradle build automatically (first launch may take a few minutes)

#3.Add google-services.json

##You must enable Firebase for the app to run.

Steps:

#Go to: https://console.firebase.google.com

#Create a Firebase project

#Add an Android app with your app package name:

Example: student.projects.quizmaster01

Download the file
google-services.json

Place the file here:

/app/google-services.json

#4️.Enable Required Firebase Services

#Make sure these are turned ON in the Firebase console:

--🔑 Authentication

-Email & Password

--🔥 Firestore Database

-Set Firestore rules (example included below):

rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    match /questions/{docId} {
      allow read: if request.auth != null;
    }

    match /quests/{questId} {
      allow read: if request.auth != null;
    }
  }
}

#5️.Run the App

In Android Studio:

--Select an emulator (Pixel recommended)

--OR connect a physical Android device with USB debugging enabled

--Click Run ▶️

The app should install and load automatically.

=============================📚 How to Use the App =============================
#🔐 Login / Register

--Users must create an account

--Email + password authentication

--Firestore creates a profile document automatically

#🎮 Play Game

--Navigate to Play from bottom navigation:

--Choose category

--Start Solo game

--Answer questions

--XP and score calculated automatically

##🏁 Game Over

#After completing the quiz:

--See final score

--XP earned for the round

--Category summary

--“Retry” or “Back to Play”

##🏆 Leaderboard

#Shows:

--Player ranking

--XP

--Level

--Medals for top 3

--Badges earned

#Modes:

--Daily

--Weekly

##🎯 Quests

#In the Quests tab:

--Daily + weekly quests

--Automatically pulled from Firestore

#Shows:

--Title

--Description

--Progress bar

--XP reward

##👤 Profile

#Options include:

--XP and badges overview

--Enable/Disable:

--Biometric Login

--Notifications

--Manual language selector (English/Afrikaans)

--Logout

=============================🌍 Language Switching ===========================

#The app supports:

        Language	           Code
        English	                en
       Afrikaans	            af

#User selects the language via:

--Profile → Settings → Select Language

#App reloads UI using:

--setLocale("af")  // Afrikaans
--setLocale("en")  // English

===================📂 Project Structure (Folders) ===================
app/
 ├── java/student/projects/quizmaster01/
 │    ├── PlayFragment.kt
 │    ├── GameFragment.kt
 │    ├── GameOverFragment.kt
 │    ├── LeaderboardFragment.kt
 │    ├── QuestsFragment.kt
 │    ├── ProfileFragment.kt
 │    ├── adapters/
 │    ├── viewmodels/
 │    └── models/
 ├── res/
 │    ├── layout/
 │    ├── drawable/
 │    ├── values/strings.xml
 │    ├── values-af/strings.xml   ← Afrikaans translations
 │    ├── menu/
 │    └── navigation/
 └── google-services.json

==============================🧪 Testing =============================
##Manual Testing

--Login/Register flow

--Play Solo game

--Correct/incorrect answers

--Quests progress

--Leaderboard sorting

--Language switching
