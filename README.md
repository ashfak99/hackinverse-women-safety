# 🛡️ Nari-Suraksha (Silent SOS)
> **"Network gayab, fir bhi help alive."**  
> Offline-First Women's Safety & Peer-to-Peer Distress Broadcast System over Bluetooth Low Energy (BLE) with Decoy Mode & 2-Way Responder ACK.

[![Live Demo Web App](https://img.shields.io/badge/Live%20Demo-Vercel%20App-ff2d78?style=for-the-badge&logo=vercel&logoColor=white)](https://hackinverse-women-safety.vercel.app/)
[![Download Android APK](https://img.shields.io/badge/Download-Android%20APK%20(18.4MB)-e91e63?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk)
[![GitHub Repo](https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Hasnainyt/hackinverse-women-safety)

---

## 🌐 Live Web Portal & Simulator

👉 **[Launch Nari-Suraksha Live Web Hub (Vercel)](https://hackinverse-women-safety.vercel.app/)**  
*(Experience the interactive 2-Phone BLE mesh simulation, Decoy Calculator PIN trigger, and offline architecture directly in your browser)*

---

## 📥 Direct APK Download & Quick Install (Method 1)

You can download and install the ready-to-run Android APK directly onto your phone without compiling code:

👉 **[📥 Download Nari-Suraksha Debug APK (`app-debug.apk`)](https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk)**

### 📲 How to Install (Method 1 - Easiest):
1. **Download `app-debug.apk`** on your Android phone using the direct download link above (or send it via WhatsApp, Telegram, or Google Drive).
2. Tap the downloaded file on your phone and select **Install**.
3. *If prompted:* Enable **"Install from unknown sources"** or **"Allow from this source"** in your phone settings.
4. Open **Nari-Suraksha**, complete the 1-minute permission setup, and you're protected 100% offline!

---

## 🌟 Overview
**Nari-Suraksha** is an offline-first Android safety application designed for situations where **cellular networks, internet connectivity, or cloud servers are unavailable or disabled**. 

Using autonomous peer-to-peer **Bluetooth Low Energy (BLE) broadcasting**, an emergency signal is transmitted directly to nearby smartphones running the app without relying on telecom towers, SIM data, or Wi-Fi.

---

## 🚀 Key Features

- 📴 **Works Without Cellular or Internet**: Autonomous BLE advertisement using compact 16-bit Service UUID data structures fitting within the standard legacy 31-byte BLE packet (15-byte payload: `senderId`, `timestamp`, `lat`, `lon`, `flags`).
- ⚡ **Multi-Trigger SOS**:
  - **1-Tap Pulsating Hero SOS Button** with tactile ring feedback.
  - **3-Shake Accelerometer Sensor**: Detects 3 rapid shakes (> 2.7g) within 2 seconds with a 3-second cooldown to prevent false alarms.
  - **5-Second Cancel Window**: Countdown overlay with audio/vibration cues to cancel accidental triggers.
- 🧮 **Decoy Mode (Calculator Disguise) & Anti-Forensics**:
  - Fully functional arithmetic calculator (`+`, `-`, `×`, `÷`, `%`) named **"Calculator"** with a neutral calculator icon.
  - **Forced Custom PIN Onboarding**: Users set their own secret 4-digit PIN during onboarding (no default 1234).
  - Entering the secret PIN followed by `=` covertly triggers the SOS broadcast.
  - Long-pressing `=` unlocks the full guardian interface.
  - **FLAG_SECURE Protection**: Prevents app screenshots and blanks out Android Recents thumbnail previews when in real emergency screens.
  - **Evidence & Log Wipe**: One-tap action to purge all incident history and audio recordings from the device.
- 👥 **2-Way Responder Acknowledgment (ACK) & Live Counter**:
  - Responders can tap **"I AM RESPONDING (SEND ACK)"** to transmit a BLE confirmation beacon back to the sender.
  - The sender's screen updates in real time: *"A nearby person has acknowledged your alert."*
- 🎙️ **Local Audio Evidence Recording**: Automatically records microphone audio to app-private storage upon SOS confirmation.
- 📍 **Offline GPS Location & Proximity Estimation**: Captures GPS coordinates with offline fallback, offering one-tap map navigation and RSSI distance estimation (`Near < 3m`, `Medium 3–10m`, `Far > 10m` — labeled approximate due to RF noise).
- 🚨 **Built-in Deterrent Siren & Flashlight**: High-decibel alarm siren and strobe torch to deter attackers or help responders locate the victim in darkness.
- 🏥 **Offline Safe Zones Directory**: Pre-cached directory of 24x7 Women Help Desks, Police Stations, and Hospitals with emergency dialers (112, 1091 — *Note: voice calls require active cellular connection*).
- 🛡️ **Always-On Background Guardian**: Persistent background service ("Calculator Service") with low-visibility notifications that continues listening for distress beacons even when the screen is locked.

---

## 📋 Prerequisites (For Developers)

If you are setting up the development environment from source:

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
📁 `app/build/outputs/apk/debug/app-debug.apk` and `app-debug.apk`

---

## 📱 Alternative Installation Methods

### Method 2: Via Android Studio (Direct Run)
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect your phone via USB or Wireless Debugging.
3. Select your device from the top toolbar dropdown in Android Studio.
4. Click the green **Run (▶)** button.

### Method 3: Via ADB (Command Line)
```bash
adb install -r app-debug.apk
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

---

## 🔒 Security Model & Known Limitations

| Aspect | Current Implementation (v1 MVP) | Roadmap / Future Work |
|---|---|---|
| **Packet Authentication** | Open binary broadcast without digital signatures for instant zero-handshake transmission. | Elliptic Curve (ECDSA) digital signatures with public-key exchange. |
| **Packet Privacy** | Raw anonymous payload without user identifying names or phone numbers. | Ephemeral Diffie-Hellman payload encryption for trusted contact rings. |
| **Spoofing & Spam Mitigation** | Per-sender rate limiting (10-second spam rejection window per sender ID). | Cryptographic proof-of-work or challenge-response verification. |
| **Relay Topology** | Single-hop direct broadcast + direct 2-way ACK. | Multi-hop mesh store-and-forward relay across intermediary nodes. |
| **Anti-Forensics** | Disguised launcher, neutral notifications, `FLAG_SECURE` window, and storage purge. | Secure biometric enclave key destruction and encrypted database. |

---

## 📡 BLE Advertisement Packet Specification (Legacy 31-Byte Structure)

To ensure universal compatibility across Android chipsets (Samsung, Xiaomi, Vivo/Oppo, Pixel, OnePlus), Nari-Suraksha packages all distress telemetry strictly within the legacy 31-byte Bluetooth advertising packet:

```
[ AD Record 1: 16-bit Service UUID ] (4 Bytes)
  ├── Length: 0x03 (3 bytes)
  ├── AD Type: 0x03 (16-bit Complete Service UUID List)
  └── Service UUID: 0xFDE1 (2 bytes)

[ AD Record 2: 16-bit Service Data ] (19 Bytes)
  ├── Length: 0x12 (18 bytes)
  ├── AD Type: 0x16 (Service Data - 16-bit UUID)
  ├── UUID: 0xFDE1 (2 bytes)
  └── SOS Payload: 15 Bytes
        ├── Magic Byte: 0x53 ('S') (1 byte)
        ├── Version: 0x01 (1 byte)
        ├── Sender ID: Short (2 bytes)
        ├── Timestamp: Int (4 bytes)
        ├── Latitude: Float (4 bytes)
        ├── Longitude: Float (4 bytes)
        └── Flags: Bit 0 = ACK, Bit 1 = Audio Recorded (1 byte)

Total AD Payload: 23 Bytes <= 31 Bytes (Safe on all chipsets)
```

---

## 👥 Team Broken Coders (Hackinverse 1.0)
- **Workstream A (Trigger & Capture):** Shake sensor, Countdown, Audio evidence, GPS fallback.
- **Workstream B (BLE Engine):** Packet codec, 16-bit BLE advertiser & scanner, foreground services.
- **Workstream C (UI & Navigation):** Compose theme, Home, Alert, Safe zones, History, Settings.
- **Workstream D (Decoy & Storage):** Calculator disguise, Room DB, Permission onboarding, DataStore PIN.

---

## 📄 License
This project was developed for **Hackinverse 1.0** by **Team Broken Coders**. Distributed for open safety and educational purposes.
