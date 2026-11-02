# JARVIS — ULTIMATE PERSONAL AI VOICE ASSISTANT

> "Always Listening. Always Understanding. Always Ready."

JARVIS is a complete, production-grade native Android personal AI voice assistant engineered with Kotlin, Jetpack Compose, Material 3, Google Gemini reasoning intelligence, ElevenLabs Ultra-Realistic Voice & Voice Cloning Engine, an on-device hands-free wake word engine ("Hey Jarvis"), live weather telemetry, and an accessibility-based UI automation service.

---

## 🌟 Key Architecture & Features

### 1. Signature JARVIS AI Orb
- **Harmonic Moiré Mesh:** Custom Compose Canvas implementation rendering multi-layered sinusoidal soundwave geometry, harmonic frequency perturbation, and blue/cyan radiant energy glow.
- **Nine Distinct States:**
  - `IDLE`: Gentle breathing and floating motion.
  - `WAKE`: Dynamic activation pulse when the user says "Hey Jarvis".
  - `LISTENING`: Audio RMS-reactive expansion synchronized to decibel input.
  - `THINKING`: High-speed orbital particles with Gemini reasoning.
  - `SPEAKING`: Spoken audio waveform expansion with speech amplitude.
  - `EXECUTING`: Live command execution feedback.
  - `SUCCESS`: Cyan-emerald confirmation bloom.
  - `ERROR`: Subtle warning indicator.
  - `INTERRUPTED`: Instant, smooth relaxation back to idle.

### 2. Hands-Free Background Wake Word Engine ("Hey Jarvis")
- **Foreground Service:** Compliant Android `JarvisVoiceService` with persistent microphone notification and quick control actions.
- **Acoustic Keyword Detection:** On-device listening loop detecting "Hey Jarvis" and "Jarvis" to wake the assistant hands-free.
- **Privacy First:** User-controlled privacy switch, battery optimization tips, and transparent status indicators.

### 3. Conversational Intelligence & Google Gemini API
- Multilingual conversational capabilities (বাংলা, English, Hindi, العربية).
- Full conversational memory factoring in user-approved preferences, routines, and facts saved in the local Room database.
- Natural companion personality:
  - *"Jarvis, তুমি কী করছ?"* → *"তোমার অপেক্ষায় আছি! বলো, কী করতে হবে?"*
  - *"আমার জন্য একটা গান চালাও"* → *"অবশ্যই! ইউটিউবে কী ধরনের গান শুনতে চাও?"*
  - *"আজ আবহাওয়া কেমন?"* → Real-time weather telemetry from Open-Meteo.

### 4. Complete ElevenLabs Voice Capabilities Matrix
- **Text to Speech (TTS):** High-fidelity human-like speech streaming.
- **Voice Library:** Category filtering (*Conversational*, *Narration*, *Professional*), accent metadata, and live preview audio.
- **Voice Cloning Studio:** Sample recording, legal consent confirmation, and instant voice cloning profile generation.
- **Extended Matrix:** Voice Design, Speech-to-Text, Sound Effects (SFX), Audio Isolation/Denoiser, AI Dubbing, and Pronunciation Dictionaries.
- **Android TTS Fallback:** Guaranteed instant speech synthesis when offline or before setting up API keys.

### 5. Modular Phone Control Engine
- **Applications:** YouTube, WhatsApp, Google Chrome, Telegram, Instagram, Facebook, Google Maps, Camera, Settings.
- **Hardware Utilities:** Flashlight control via `CameraManager`, battery capacity percentage (`BatteryManager`), and disk storage stats (`StatFs`).
- **Alarms & Timers:** System clock intent triggers (`AlarmClock.ACTION_SET_ALARM`, `ACTION_SET_TIMER`).
- **Calendar & Schedule:** Calendar launcher and event planner (`content://com.android.calendar/time`).
- **Settings Shortcuts:** Direct links to Wi-Fi, Bluetooth, Display, and Sound settings.
- **Autonomous Calling & Script Studio:** Dialer launch, contact search, and pre-recorded voice call script generator with AI caller disclosure.
- **Messaging Assistant:** WhatsApp direct intent and native SMS intent with templates.
- **Web & AI Research:** Web search with Gemini summarization and browser fallback.
- **Social Media Assistant:** Caption and hashtag generator for Facebook, Instagram, YouTube, and Telegram with native Android ShareSheet integration.
- **File & Document Assistant:** Storage Access Framework (SAF) document picker with AI summarization and voice reading.

### 6. Accessibility-Based UI Automation Service
- `JarvisAccessibilityService` declared with `accessibility_service_config.xml`.
- Screen reading and node inspection (`readVisibleScreenText()`).
- Automated UI node clicking (`clickNodeByText()`) and scrolling (`scrollForward()`).
- **Emergency Stop Button:** Hard kill-switch to immediately abort all automated actions.

### 7. Local Data Persistence & Zero-Leak Privacy
- **Room Database:** Stores complete chat history (`chat_messages`), user memories (`user_memories`), and voice scripts (`voice_scripts`).
- **DataStore:** Manages API keys, selected voices, speech speed, and background service preferences.
- **Permissions Center:** Detailed status for Microphone, Notifications, Contacts, Phone Calls, Calendar, Location, and Camera.
- **One-Click Data Wipe:** Complete erasure of all on-device history, keys, and memories.

---

## 🛠️ Tech Stack

- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Architecture:** Clean MVVM (Model-View-ViewModel)
- **Local Persistence:** Room Database 2.7.0 (KSP) + DataStore Preferences
- **Networking:** Retrofit 2.12.0 + OkHttp + Moshi (KSP)
- **Voice & Speech:** Android `SpeechRecognizer` + ElevenLabs REST API + Android `TextToSpeech`
- **Automation:** Android `AccessibilityService`

---

## 🚀 Build & Verification

```bash
# Compile and build the debug APK
gradle :app:assembleDebug

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest
```
