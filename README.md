# 🛡️ Nari-Suraksha (Silent SOS)
> **"Network gayab, fir bhi help alive."**  
> Offline-First Women's Safety & Peer-to-Peer Distress Broadcast System over Bluetooth Low Energy (BLE) with Decoy Mode & 2-Way Responder ACK.

---

## 🌟 Overview
**Nari-Suraksha** is a mission-critical Android safety application built for emergencies where **cellular networks, internet connectivity, or cloud servers are unavailable, jammed, or disabled**. 

Using autonomous peer-to-peer **Bluetooth Low Energy (BLE) broadcasting**, an SOS signal is transmitted directly to all nearby smartphones running the app without relying on telecom infrastructure, SIM cards, or Wi-Fi.

---

## 🚀 Key Features

- 📴 **100% Offline BLE Distress Broadcast**: Custom 128-bit Service UUID broadcasting a compact 15-byte binary packet payload (`senderId`, `timestamp`, `lat`, `lon`, `flags`).
- ⚡ **Multi-Trigger SOS**:
  - **1-Tap Pulsating Hero SOS Button** with tactile ring feedback.
  - **3-Shake Accelerometer Sensor**: Detects 3 rapid shakes (> 2.7g) within 2 seconds with a 3-second cooldown to prevent false alarms.
  - **5-Second Cancel Window**: Countdown circle with audio/vibration cues to allow cancellation before broadcast.
- 🧮 **Decoy Mode (Calculator Disguise) & Custom PIN**:
  - Fully functional arithmetic calculator (`+`, `-`, `×`, `÷`, `%`).
  - Entering a secret PIN (default `1234` or custom-configured) followed by `=` covertly triggers SOS.
  - Long-pressing `=` unlocks the full safety application.
  - **In-App Safety Settings**: Change the 4-digit secret PIN at any time.
- 👥 **2-Way Responder Acknowledgment (ACK) & Live Counter**:
  - Responders can tap **"I AM RESPONDING (SEND ACK)"** to transmit a BLE confirmation beacon back to the victim.
  - The victim's device displays a live counter: *"👥 N Nearby Responder(s) Alerted! Help acknowledged your beacon and is heading your way."*
- 🎙️ **Offline Audio Evidence Capture**: Automatically records microphone evidence to secure app-private storage upon SOS confirmation.
- 📍 **Offline GPS Location & Proximity Tracking**: Captures GPS coordinates with offline fallback, offering one-tap navigation and proximity estimation via RSSI (`Near < 3m`, `Medium 3–8m`, `Far > 10m`).
- 🚨 **Responder Assistance Tools**: Built-in loud deterrent siren and flashlight strobe to locate victims in the dark.
- 🏥 **Offline Safe Zones Directory**: Pre-cached directory of 24x7 Women Help Desks, Police Stations, Metro Security Posts, and Trauma Centers with direct emergency dialers (112, 1091).
- 🛡️ **Always-On Background Guardian**: Persistent foreground service that keeps scanning for distress beacons even when the screen is locked.

---

## 📋 Prerequisites

Before setting up the project, make sure you have:

| Tool | Recommended Version | Download Link |
|---|---|---|
| **Git** | Latest | [git-scm.com](https://git-scm.com/) |
| **JDK (Java Development Kit)** | **JDK 17** (or JDK 21) | [Adoptium OpenJDK 17](https://adoptium.net/) |
| **Android Studio** | Ladybug / Koala / Iguana | [developer.android.com/studio](https://developer.android.com/studio) |
| **Android SDK** | API 26 (Min) to API 35 (Target) | Installed via Android Studio SDK Manager |
| **Android Build Tools** | `34.0.0` or `35.0.0` | Installed via Android Studio SDK Manager |

> [!TIP]
> **Hardware Testing**: For full end-to-end BLE testing, **two physical Android phones** (Android 8.0+) with Bluetooth enabled are recommended.

---

## 💻 Step-by-Step Local Setup Guide

### 1. Clone the Repository
Open your terminal (PowerShell, Command Prompt, or Bash) and clone the repository:

```bash
git clone https://github.com/Hasnainyt/hackinverse-women-safety.git
cd hackinverse-women-safety
```

---

### 2. Configure `local.properties` (Android SDK Path)
In the project root folder, create a file named `local.properties` (if it does not already exist) and specify your Android SDK path:

#### On Windows:
```properties
sdk.dir=C\:\\Users\\<YOUR_USERNAME>\\AppData\\Local\\Android\\Sdk
```
*(Replace `<YOUR_USERNAME>` with your Windows user name)*

#### On macOS:
```properties
sdk.dir=/Users/<YOUR_USERNAME>/Library/Android/sdk
```

#### On Linux:
```properties
sdk.dir=/home/<YOUR_USERNAME>/Android/Sdk
```

---

### 3. Open in Android Studio
1. Launch **Android Studio**.
2. Click **Open** (or `File -> Open`).
3. Select the `hackinverse-women-safety` root folder.
4. Allow Android Studio to complete the **Gradle Sync** (it will automatically download all dependencies listed in `gradle/libs.versions.toml`).

---

### 4. Build and Test via Command Line

#### Run Unit Tests:
```bash
# Windows (PowerShell / CMD)
.\gradlew.bat testDebugUnitTest

# macOS / Linux
./gradlew testDebugUnitTest
```

#### Build Debug APK:
```bash
# Windows (PowerShell / CMD)
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The generated APK will be available at:
📁 `app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Installing & Testing on Real Devices

### Option A: Via Android Studio
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect your phone via USB or Wireless Debugging.
3. Select your device from the top toolbar dropdown in Android Studio.
4. Click the green **Run (▶)** button.

### Option B: Via ADB (Command Line)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 2-Phone Offline Demo Walkthrough (Hackathon Guide)

Follow this 2-minute demo flow to showcase the full offline 2-way communication:

```
[Phone A: Sender]                                [Phone B: Responder]
Airplane Mode + Bluetooth ON                     Airplane Mode + Bluetooth ON
           |                                                |
Open Calculator (Decoy Mode)                      Screen Off / In Background
           |                                                |
Enter PIN `1234` + press `=`                                |
           |                                                |
5s Animated Countdown Circle                                |
(Option to cancel if false alarm)                           |
           |                                                |
SOS Confirmed!                                              |
• Records local audio evidence                              |
• Fetches GPS coordinates                                   |
• Broadcasts BLE beacon for 60s -------------------------> Receives BLE Packet!
                                                            • High-priority alarm sound & vibration
                                                            • Distance bucket: "< 3m away" (RSSI)
                                                            • Shows GPS Coordinates & Map intent
                                                            • Responder taps "I AM RESPONDING"
                                                                    |
Live Responder Counter Updates! <-------------------------- Transmits ACK Beacon
"👥 1 Nearby Responder Alerted!"
```

---

## 🏗️ Project Architecture & Package Structure

```
com.brokencoders.narisuraksha
├── core/
│   ├── SosPacket.kt             # 15-byte compact binary packet contract with ACK flag
│   ├── Constants.kt             # Custom 128-bit UUID, RSSI buckets, channel IDs
│   ├── DeviceIdProvider.kt      # Anonymous 16-bit random device ID provider
│   └── PermissionHelper.kt      # Runtime permission & battery optimization checks
├── trigger/
│   ├── SosEvent.kt              # Sealed interface: CountdownStarted, Cancelled, Confirmed
│   ├── SosTrigger.kt            # Flow<SosEvent> contract
│   ├── ShakeDetector.kt         # Accelerometer 2.7g spike detection (3 spikes in 2s)
│   ├── SosManager.kt            # Central SOS state machine coordinating countdown & capture
│   └── FakeSosTrigger.kt        # Mock trigger for UI previews and testing
├── capture/
│   ├── AudioRecorder.kt         # MediaRecorder AAC evidence capture in private storage
│   └── LocationProvider.kt      # Offline GPS coordinate provider with fallback
├── ble/
│   ├── PacketCodec.kt           # ByteBuffer binary serializer/deserializer (15 bytes)
│   ├── BleAdvertiser.kt         # Low-latency BLE advertiser with 60s auto-stop
│   ├── BleScanner.kt            # Continuous BLE scanner with deduplication
│   ├── BleTransport.kt          # BLE transport interface
│   ├── BleTransportImpl.kt      # Concrete BLE transport implementation
│   └── FakeBleTransport.kt      # Fake BLE transport for testing
├── service/
│   ├── ScanForegroundService.kt # Always-on background BLE listener
│   ├── SosForegroundService.kt  # Active distress broadcaster
│   └── NotificationHelper.kt    # Heads-up priority notifications
├── data/
│   ├── SosEventEntity.kt        # Room database entity (Sent/Received)
│   ├── SosDao.kt                # Room Data Access Object
│   ├── AppDatabase.kt           # Room Database singleton
│   └── UserPreferencesRepository# DataStore preferences (PIN, Scan, Shake)
├── decoy/
│   ├── CalculatorEngine.kt      # Real arithmetic engine (+, -, *, /, %)
│   ├── DecoyViewModel.kt        # Secret PIN validation & unlock triggers
│   └── CalculatorScreen.kt      # Authentic calculator UI
└── ui/
    ├── theme/                   # Material 3 safety color system (Red/Teal/Dark)
    ├── navigation/              # Compose NavHost graph
    ├── components/              # SosPulseButton, CountdownOverlay, SettingsDialog, StatusBadge
    ├── screens/                 # HomeScreen, AlertScreen, HistoryScreen, SafeZoneScreen, OnboardingScreen
    └── viewmodels/              # MainViewModel, HistoryViewModel
```

---

## 🔧 Troubleshooting

| Issue | Solution |
|---|---|
| **Gradle JDK Version Mismatch** | Ensure your Gradle JDK is set to **JDK 17** in Android Studio (`Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`). |
| **Bluetooth Scan not receiving packets** | Ensure **Location** and **Nearby Devices (Bluetooth)** permissions are granted in the Onboarding screen. Make sure Bluetooth is toggled **ON**. |
| **Background scanning killed on Xiaomi/Samsung/Oppo** | Tap **"Disable Battery Optimization"** in the Onboarding screen and select *Unrestricted / No Restrictions*. |
| **Audio recording location** | Grant the `RECORD_AUDIO` permission during onboarding. Audio files are saved privately in `Android/data/com.brokencoders.narisuraksha/files/sos_recordings/`. |

---

## 👥 Team Broken Coders (Hackinverse 1.0)
- **Workstream A (Trigger & Capture):** Shake sensor, Countdown, Audio evidence, GPS fallback.
- **Workstream B (BLE Engine):** Packet codec, BLE advertiser & scanner, foreground services.
- **Workstream C (UI & Navigation):** Compose theme, Home, Alert, Safe zones, History, Settings.
- **Workstream D (Decoy & Storage):** Calculator disguise, Room DB, Permission onboarding, DataStore PIN.

---

## 📄 License
This project was developed for **Hackinverse 1.0** by **Team Broken Coders**. Distributed for open safety and educational purposes.
