# 🛡️ Nari-Suraksha (Silent SOS)
> **"Network gayab, fir bhi help alive."**  
> Offline-First Women's Safety & Peer-to-Peer Distress Broadcast System over Bluetooth Low Energy (BLE) with Decoy Mode & 2-Way Responder ACK.

[![Live Demo Web App](https://img.shields.io/badge/Live%20Demo-Vercel%20App-ff2d78?style=for-the-badge&logo=vercel&logoColor=white)](https://hackinverse-women-safety.vercel.app/)
[![Download Android APK](https://img.shields.io/badge/Download-Android%20APK%20(18.4MB)-e91e63?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk)
[![GitHub Repo](https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Hasnainyt/hackinverse-women-safety)

---

## 🌐 Live Web Portal & Simulator

👉 **[Launch Nari-Suraksha Live Web Hub (Vercel)](https://hackinverse-women-safety.vercel.app/)**  
*(Experience the interactive 2-Phone BLE broadcast simulation, Decoy Calculator PIN trigger, and offline architecture directly in your browser. Note: This web portal is an illustrative demonstration — web browsers do not broadcast native BLE packets. Download the Android APK for real hardware radio testing).*

---

## 📥 Direct APK Download & Quick Install (Method 1)

You can download and install the ready-to-run Android APK directly onto your phone without compiling code:

👉 **[📥 Download Nari-Suraksha Debug APK (`app-debug.apk`)](https://github.com/Hasnainyt/hackinverse-women-safety/raw/main/app-debug.apk)**

### 📲 How to Install (Method 1 - Easiest):
1. **Download `app-debug.apk`** on your Android phone using the direct download link above (or transfer via WhatsApp, Telegram, or USB).
2. Tap the downloaded APK on your phone and select **Install**.
3. *If prompted by Google Play Protect:* Because this hackathon APK is distributed directly outside the Google Play Store, Play Protect may display an *"Unrecognized app"* warning. Tap **"More details"** and then **"Install anyway"**.
4. *If prompted by Android:* Enable **"Install from unknown sources"** or **"Allow from this source"** in your phone settings.
5. Open the app named **"Calculator"** (the decoy launcher icon for Nari-Suraksha), complete the 1-minute permission setup, and choose your custom 4-digit secret PIN.

---

## 🌟 Overview
**Nari-Suraksha** is an offline-first Android safety application designed for situations where **cellular networks, internet connectivity, or cloud servers are unavailable or disabled**. 

Using autonomous peer-to-peer **Bluetooth Low Energy (BLE) broadcasting**, an emergency signal is transmitted directly to nearby smartphones running the app without relying on telecom towers, SIM data, or Wi-Fi.

---

## 🚀 Key Features

- 📴 **Works Without Cellular or Internet**: Autonomous BLE advertisement using compact 16-bit Service UUID data structures fitting within the standard legacy 31-byte BLE packet (17-byte wire payload: `magic`, `version`, `senderId`, `timestamp`, `lat`, `lon`, `flags`).
- ⚡ **Multi-Trigger SOS**:
  - **1-Tap Pulsating Hero SOS Button** with tactile ring feedback.
  - **3-Shake Accelerometer Sensor**: Detects 3 rapid shakes (> 2.7g) within 2 seconds with a 3-second cooldown to prevent false alarms.
  - **5-Second Cancel Window**: Countdown overlay with audio/vibration cues to cancel accidental triggers.
- 🧮 **Decoy Mode (Calculator Disguise) & Discretion and Privacy Features**:
  - Fully functional arithmetic calculator (`+`, `-`, `×`, `÷`, `%`) named **"Calculator"** with a neutral calculator icon.
  - **Forced Custom PIN Onboarding**: Users set their own secret 4-digit PIN during onboarding (no default PIN).
  - Entering the secret PIN followed by `=` covertly triggers the SOS broadcast.
  - Long-pressing `=` unlocks the full guardian interface.
  - **FLAG_SECURE Protection**: Prevents app screenshots and blanks out Android Recents thumbnail previews when viewing sensitive emergency screens.
  - **User-Confirmed Evidence & Log Wipe**: Secure option to purge incident history and local audio recordings, requiring explicit dialog confirmation so critical legal evidence isn't erased accidentally.
- 👥 **2-Way Responder Acknowledgment (ACK) & Live Counter**:
  - Responders can tap **"I AM RESPONDING (SEND ACK)"** to transmit a targeted BLE confirmation beacon back to the victim.
  - The victim's screen updates in real time: *"A nearby person has acknowledged your alert."*
  - Responders are uniquely tracked and deduplicated: tapping twice increments the counter only once, and ACKs match only the sender who broadcast distress.
- 🎙️ **Local Audio Evidence Recording**: Automatically records microphone audio to app-private storage upon SOS confirmation (configured with `FOREGROUND_SERVICE_MICROPHONE` for Android 14/15 background compliance).
- 📍 **Offline GPS Location & Proximity Estimation**: Captures GPS coordinates with offline fallback, offering one-tap map navigation and RSSI distance estimation (`Near < 3m`, `Medium 3–10m`, `Far > 10m` — labeled approximate due to RF noise).
- 🚨 **Built-in Deterrent Siren & Flashlight**: High-decibel alarm siren and strobe torch to deter attackers or help responders locate the victim in darkness.
- 🏥 **Offline Safe Zones Directory**: Pre-cached directory of 24x7 Women Help Desks, Police Stations, and Hospitals with emergency dialers (112, 1091 — *Note: voice calls require active cellular connection*).
- 🛡️ **Always-On Background Guardian**: Persistent background service ("Calculator Service") with low-visibility notifications that continues listening for distress beacons even when the screen is locked.
- 🛑 **Dual-Tier Rate Limiting & Abuse Prevention**: 10-second per-sender spam rejection combined with a global cap (max 10 alerts/minute across all senders) to mitigate spoofed ID-rotation floods.

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
[Phone A: Sender / Victim]                       [Phone B: Responder]
Airplane Mode + Bluetooth ON                     Airplane Mode + Bluetooth ON
           |                                                |
Open Calculator (Decoy Mode)                      Screen Off / In Background
           |                                                |
Enter custom PIN (set during onboarding) + press `=`        |
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
                                                            • Responder taps "I AM RESPONDING (SEND ACK)"
                                                                    |
Live Responder Counter Updates! <-------------------------- Transmits targeted ACK Beacon
"A nearby person has acknowledged your alert"
```

---

## 🏗️ Project Architecture & Package Structure

```
com.brokencoders.narisuraksha
├── core/
│   ├── SosPacket.kt             # 17-byte binary packet contract with targeted ACK matching
│   ├── Constants.kt             # 16-bit Service UUID (0xFDE1), RSSI buckets, channel IDs, rate limit caps
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
│   ├── PacketCodec.kt           # Binary serializer/deserializer (17-byte spec + 15-byte legacy fallback)
│   ├── BleAdvertiser.kt         # Low-latency BLE advertiser with 60s auto-stop
│   ├── BleScanner.kt            # Continuous BLE scanner with deduplication & global rate limiting
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
| **Spoofing & Spam Mitigation** | Dual-tier mitigation: 10s per-sender cooldown + global rate limit cap (max 10 alerts/minute across all senders). Limitation: Attackers rapidly rotating spoofed sender IDs can exhaust the global alert window. | Cryptographic challenge-response and ephemeral ECDSA signatures. |
| **Relay Topology** | Single-hop direct broadcast + targeted 2-way ACK. | Multi-hop mesh store-and-forward relay across intermediary nodes. |
| **Discretion & Privacy Features** | Disguised launcher ("Calculator"), neutral background notifications ("Calculator Service"), `FLAG_SECURE` window, and user-confirmed evidence wipe. Limitation: Package name (`com.brokencoders.narisuraksha`), sideload traces, and Android system app lists will still reveal the app under deep forensic inspection. | Plausible deniability storage vaults, hardware-backed Keystore enclave key destruction. |

---

## 📡 BLE Advertisement Packet Specification (Legacy 31-Byte Structure)

To ensure universal compatibility across Android chipsets (Samsung, Xiaomi, Vivo/Oppo, Pixel, OnePlus), Nari-Suraksha packages all distress telemetry strictly within the legacy 31-byte Bluetooth advertising packet:

```
[ AD Record 1: 16-bit Complete Service UUID List ] (4 Bytes)
  ├── Length: 0x03 (3 bytes)
  ├── AD Type: 0x03 (16-bit Complete Service UUID List)
  └── Service UUID: 0xFDE1 (2 bytes)

[ AD Record 2: 16-bit Service Data ] (21 Bytes)
  ├── Length: 0x14 (20 bytes following: AD Type 1B + UUID 2B + SOS Wire Payload 17B)
  ├── AD Type: 0x16 (Service Data - 16-bit UUID)
  ├── UUID: 0xFDE1 (2 bytes)
  └── SOS Wire Payload: 17 Bytes
        ├── Magic Byte: 0x53 ('S') (1 byte)
        ├── Version: 0x01 (1 byte)
        ├── Sender ID: Short (2 bytes)
        ├── Timestamp / Target Sender ID: Int (4 bytes)
        ├── Latitude: Float IEEE 754 (4 bytes)
        ├── Longitude: Float IEEE 754 (4 bytes)
        └── Flags: Bit 0 = SOS, Bit 1 = Location Unavailable, Bit 2 = ACK (1 byte)

Total AD Structure: 4 (Record 1) + 21 (Record 2) = 25 Bytes <= 31 Bytes (Safe on all chipsets)
```

> [!NOTE]
> **Why AD Record 1 is Retained (Scan Filter Compatibility)**:  
> The 16-bit Service UUID (`0xFDE1`) is already contained within AD Record 2 (Service Data). Omitting AD Record 1 would save 4 bytes (reducing total AD payload from 25 to 21 bytes). However, Android's `BluetoothLeScanner` hardware filters match `ScanFilter.Builder().setServiceUuid()` against AD Record 1 (AD Type `0x03`). Retaining AD Record 1 ensures hardware-filtered background wakeups succeed reliably when the phone screen is locked across heterogeneous OEM chipsets, while remaining safely below the 31-byte limit (25 bytes <= 31 bytes).

> [!IMPORTANT]
> **Bluetooth SIG UUID Notice (`0xFDE1`)**:  
> `0xFDE1` falls within the Bluetooth SIG 16-bit member-allocated range (suitable for hackathon and prototyping). A commercial production deployment would register an official UUID with the Bluetooth SIG or adopt a registered 128-bit custom service UUID.

---

## 🔬 Rigorous Pre-Submission Verification & Testing Guide

Before submitting or evaluating the app, verify these 5 critical edge cases to ensure a seamless demonstration:

### 1. Shake Detection with Screen Locked (5+ Minutes)
- **Background Constraint**: Android strictly limits sensors for background applications, and OEM battery optimizers (Samsung OneUI, Xiaomi HyperOS/MIUI, Oppo/Vivo) often suspend accelerometer events when the screen has been turned off for several minutes.
- **Verification Procedure**:
  1. Open Calculator, complete onboarding, and ensure Background Service and Shake Detection are toggled ON.
  2. Disable battery optimization for the app (*Settings -> Apps -> Calculator -> Battery -> Unrestricted*).
  3. Turn off the phone screen and leave it on a table for **5+ minutes**.
  4. Without pressing the power button, firmly shake the phone 3 times.
  5. Verify that the haptic countdown and audible cue trigger, followed by the distress broadcast.

### 2. Android 14 & 15 Foreground Service Restrictions (Microphone & Location)
- **Platform Constraint**: Android 14 (API 34) and Android 15 (API 35) block background services from accessing the microphone or location unless started with explicit `foregroundServiceType` declarations.
- **Verification Procedure**:
  - The app declares `FOREGROUND_SERVICE_MICROPHONE` and `FOREGROUND_SERVICE_LOCATION` permissions.
  - `SosForegroundService` specifies `android:foregroundServiceType="connectedDevice|location|microphone"`.
  - When SOS is triggered from a locked screen, verify that audio evidence is successfully recorded to app-private storage and GPS coordinates are acquired without throwing a `SecurityException`.

### 3. Cross-Brand OEM Testing (Samsung, Xiaomi, Oppo/Vivo, Pixel)
- **Hardware Diversity**: Different manufacturers use different Bluetooth chipsets (Qualcomm, MediaTek, Exynos, Broadcom) with differing BLE advertising intervals and scan duty cycles.
- **Verification Procedure**:
  - Test the 2-phone demo across different brands (e.g. Samsung + Xiaomi or Pixel + Vivo).
  - Verify that advertisements broadcast via `BleAdvertiser` with `ADVERTISE_MODE_LOW_LATENCY` are received within 1–3 seconds on the scanning device.

### 4. 3-Phone Concurrency, Deduplication, and Targeted ACK Matching
- **Setup**: Phone A (Victim / SOS Broadcaster), Phone B (Responder 1), Phone C (Responder 2).
- **Verification Procedure**:
  1. Phone A initiates SOS broadcast (Device ID: e.g., `#1001`).
  2. Both Phone B and Phone C receive the alarm simultaneously.
  3. Phone B taps **"I AM RESPONDING (SEND ACK)"**. Phone B transmits an ACK targeted to `#1001`.
  4. Phone A's screen updates: *"A nearby person has acknowledged your alert"* (1 Responder).
  5. If Phone B taps the ACK button again, verify Phone A's counter **stays at 1** (deduplication ensures one responder tapping twice counts only once).
  6. Phone C taps **"I AM RESPONDING"**. Phone A's counter increments to **2 Responders**.
  7. Confirm that Phone B's screen does NOT treat Phone C's ACK as meant for itself because the ACK packet carries Phone A's original sender ID (`targetSenderId`).

### 5. Google Play Protect Warning & Building Signed Release APK
- **Play Protect Advisory**: Because `app-debug.apk` is sideloaded directly without Google Play Store signing, Google Play Protect may display an *"Unrecognized app"* warning prompt.
- **To install during testing**: Tap **"More details"** -> **"Install anyway"**.
- **Building a signed release APK**:
  ```bash
  ./gradlew assembleRelease
  ```
  *(Sign with your production keystore using `apksigner` or via Android Studio: Build -> Generate Signed Bundle / APK).*

---

## 👥 Team Broken Coders (Hackinverse 1.0)
- **Workstream A (Trigger & Capture):** Shake sensor, Countdown, Audio evidence, GPS fallback.
- **Workstream B (BLE Engine):** Packet codec, 16-bit BLE advertiser & scanner, foreground services.
- **Workstream C (UI & Navigation):** Compose theme, Home, Alert, Safe zones, History, Settings.
- **Workstream D (Decoy & Storage):** Calculator disguise, Room DB, Permission onboarding, DataStore PIN.

---

## 📄 License
This project was developed for **Hackinverse 1.0** by **Team Broken Coders**. Distributed for open safety and educational purposes.
