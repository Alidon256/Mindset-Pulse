# Mindset Pulse 
**Kotlin Multiplatform | Compose Multiplatform | Google Gemini AI**

> **Mindset Pulse**  
> An intelligent, empathetic early-warning system designed to combat burnout and stress, with a specific focus on the unique pressures facing the African youth and professional workforce.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)  
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.6.1-purple.svg?style=flat&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)  
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg?style=flat&logo=firebase)](https://firebase.google.com/)  
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-red.svg?style=flat&logo=google-gemini)](https://deepmind.google/technologies/gemini/)

---

## 🌍 The Problem
In Africa's fast-growing tech and professional hubs, "hustle culture" often masks a serious mental health crisis. Burnout is frequently ignored until it results in severe clinical exhaustion. **Mindset Pulse** uses Kotlin Multiplatform to deliver an accessible, low-friction tool that acts as a mental health "check-engine" light.

---

## 🚀 Key Features
* **AI-Curated Daily Check-ins** – Gemini AI dynamically generates empathetic questions based on stress, sleep, and workload.  
* **Responsible AI Engine** – Text responses are analyzed by Gemini for sentiment, but the final **Risk Score (0-100)** is calculated by a transparent, shared KMP Risk Engine.  
* **Relaxation Rhythms** – A cross-platform audio player for focus and mindfulness, featuring an industry-standard floating mini-player. Recommendations adapt to the time of day.  
* **Safe Spaces** – Real-time community hubs for peer support. Users can create spaces, upload images with metadata, chat, react to posts, and share memories. Important stories can be saved to profiles.  
* **Mindful Actions** – Guided **Breathing, Yoga, and Meditation** sessions with real-time timers and animations. Users select durations, complete exercises, and earn streaks + XP points stored in the database.  
* **Profile Tracking** – A personalized dashboard that logs all user activities: analytics, saved posts, uploaded posts, liked posts, and progression levels.  
* **Settings & Personalization** – Switch between Dark/Light themes, choose immersive color schemes (Nature, Ocean, etc.), and manage account preferences including Sign Out.  
* **Gamified Growth** – 20 levels of "Mindset Progression" (from Initiate to Pulse Master) to reward consistent mental health maintenance.  
* **Responsive Master-Detail UI** – A high-performance web dashboard layout that adapts seamlessly to mobile devices.  
* **Profile Editing & Connections** – Users can edit profiles, update avatars, and connect with people sharing meaningful content.  
* **Theme Persistence** – Implemented via **local storage on Web (JS)** and **SharedPreferences on Android** using multiplatform settings.
* **99%+ Shared UI & Logic** – Compose Multiplatform + MVVM ensures near‑total code reuse across Android and Web.  

---

## 🏗️ Technical Architecture
Mindset Pulse is built using **Clean Architecture** to ensure maximum code reuse (90%+) across Android and Web.

### Shared Logic (`:composeApp:commonMain`)
* **Domain Layer** – Rule-based `RiskEngine` and `TrendAnalyzer` written in pure Kotlin.  
* **Data Layer** – Cloud-first approach using `GitLive Firebase KMP` for Firestore, Auth, and Storage.  
* **Dependency Injection (DI)** – Settings Factory for platform‑specific configuration, including theme persistence.  
* **Presentation Layer** – Shared `MVVM` using Moko ViewModels to drive identical UI state on all platforms.  

---

## 🛠️ Installation & Launch Instructions

### Prerequisites
* Android Studio Jellyfish+ or IntelliJ IDEA 2024.1+  
* JDK 17  

### 📱 Launching Android
1. Open the project in Android Studio.  
2. Ensure the `google-services.json` is located in the `composeApp` folder.  
3. Select `composeApp` in run configurations.  
4. Click **Run** on an Emulator or Physical Device (API 24+).  

### 🌐 Launching Web (Browser)
1. Open the terminal in the project root.  
2. Run: `./gradlew :composeApp:jsBrowserDevelopmentRun`  
3. The app will open at `http://localhost:8080`.  

**Note:** On Web, users should **sign in using Email authentication only**. Google Sign-In is **not working for now**.  

---

## 🧪 How to Use Key Features
1. **Onboarding** – Experience the responsive *Ocean* and *Nature* themed onboarding flow.  
2. **The Pulse** – Tap the FAB on Home, answer questions. In the text field, type: *“I am feeling extremely exhausted and can't focus on work anymore.”*  
3. **The Analysis** – Watch Gemini detect sentiment and the Risk Engine classify the state as **Burnout Risk**.  
4. **Analytics** – Visit the Analytics tab to see a full Markdown report generated by Gemini.  
5. **Spaces** – Create a space, upload images, chat, post, react, and save important stories to profiles.  
6. **Rhythms** – Test the audio player on Web + Android. Observe time-of-day recommendations and seamless playback.  
7. **Mindful Actions** – Select durations for Breathing, Yoga, or Meditation. Complete sessions and verify streaks + XP updates.  
8. **Profile** – Check the Profile screen to see logged activities: analytics, saved posts, uploaded posts, liked posts, and progression levels.  
9. **Settings** – Switch between Dark/Light themes, try different color schemes (Nature, Ocean, etc.), and test the Sign Out feature.  

---

## 📸 Screenshots

### Home
![Home Screen](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FScreenshot%20(1366).png?alt=media&token=0184d16c-6761-432b-be05-ebfede36543c)

### Spaces
![Spaces Screen](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FScreenshot%20(1373).png?alt=media&token=b9e58251-b714-4e98-8f5e-a66f16174f6e)

### Mindful Actions
![Mindful Actions Screen](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FScreenshot%20(1372).png?alt=media&token=1d5fbfca-c768-43cf-a2ac-7f9a2953b38a)

### Profile
![Profile Screen](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FScreenshot%20(1365).png?alt=media&token=ab1bc094-e5fa-48ca-ad3a-286966cb693a)

---

## 🎥 Demo Video

Watch the full demo of **Mindset Pulse** in action:  
[▶️ Click here to view the demo](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FMindset%20Pulse%20Video.mp4?alt=media&token=ddb883f7-2374-4436-8199-a1ca996d2fa2)

[![Watch the Demo](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FScreenshot%20(1362).png?alt=media&token=f3dd592c-8b1d-4f83-8046-ca4df4545e41)](https://firebasestorage.googleapis.com/v0/b/tija-a7b75.firebasestorage.app/o/My%20videos%2FMindset%20Pulse%20Video.mp4?alt=media&token=ddb883f7-2374-4436-8199-a1ca996d2fa2)

*(Additional screenshots included in `/screenshots` folder for Spaces, Analytics, and Mindful Actions.)*
**Disclaimer:** The Windows recorder could not capture the **image picker** during recording. This feature **works correctly in the app**, even though it is not visible in the demo video. 
---

## 📚 Libraries & Dependencies 
Mindset Pulse leverages a rich ecosystem of libraries for cross-platform development: 

### 🔹 Core Plugins 
- **Kotlin Multiplatform** – Enables shared logic across Android, Web, and (future) WASM.
- **Compose Multiplatform** – Declarative UI framework for Android + Web.
- **Compose Compiler** – Optimized compiler for Jetpack Compose.
- **Kotlinx Serialization** – JSON serialization for structured data exchange.

### 🔹 Android-Specific
- **AndroidX Activity Compose** – Lifecycle-aware integration with Compose.
- **Google Play Services Auth** – Secure authentication flows.
- **Firebase BOM (Auth, Firestore, Storage)** – Unified backend services.
- **Coil (OkHttp)** – Image loading and caching.
- **Ktor CIO Client** – High-performance networking.
- **Media3 (ExoPlayer, UI, Session, DASH, HLS)** – Advanced audio/video playback.

### 🔹 Common Multiplatform 
- **Compose Runtime, Foundation, Material3, UI** – Core UI building blocks.
- **Lifecycle ViewModel + Runtime Compose** – MVVM state management.
- **Material Icons Extended** – Rich iconography.
- **Coil Compose + Ktor** – Image loading across platforms.
- **Navigation Compose** – Declarative navigation.
- **MVVM Core** – Shared architecture for ViewModels.
- **Firebase (Auth, Firestore, Storage, Database)** – Cloud-first data handling. - **Kotlinx Serialization Core + Datetime** – Data modeling and time utilities.
- **Ktor (Core, Content Negotiation, JSON, Logging)** – Networking + structured API calls.
- **Multiplatform Markdown Renderer (M3)** – Rich text rendering for analytics reports.
- **Multiplatform Settings** – Persistent theme and preference storage across platforms. 
  
### 🔹 Testing 
- **Kotlin Test** – Unit testing across platforms.
---

## 👨‍💻 Developed By
**Anthony Mugumya**  
*Built with ❤️ using Kotlin Multiplatform, Compose Multiplatform, and Gemini AI.*

---

## 📜 License

This project is licensed under the **MIT License** – you are free to use, modify, and distribute it with proper attribution.

---
## 🔑 Configuring Your Own Gemini API Key

By default, the `GeminiService` class includes a placeholder API key for demonstration.  
If you want to use your own **Google Gemini API key**, follow these steps:

### 1. Obtain an API Key
- Go to the [Google AI Studio](https://ai.google.dev/) or your Google Cloud Console.
- Create a new project (if you don’t already have one).
- Enable the **Generative Language API**.
- Generate an API key under **Credentials**.

### 2. Update the Code
In `GeminiService.kt` (located under `package org.vaulture.project.data.remote`), replace the placeholder key:

```kotlin
// Replace this line with your own key
private val apiKey = "YOUR_REAL_API_KEY_HERE"
---

