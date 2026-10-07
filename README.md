# Nari-Suraksha (Silent SOS)
> **"Network gayab, fir bhi help alive."**  
> Offline-First Women's Safety & Peer-to-Peer Distress Broadcast System over Bluetooth Low Energy (BLE) with Decoy Mode.

---

## 🌟 Overview
**Nari-Suraksha** is a mission-critical Android safety application designed to protect women in emergencies when **cell towers, internet connection, or cloud servers are unavailable or disabled**. 

Using autonomous peer-to-peer **Bluetooth Low Energy (BLE) broadcasting**, an SOS signal is transmitted immediately to all nearby smartphones running the app without relying on telecom networks or Wi-Fi.

---

## 🛡️ Key Features

### 1. 📴 100% Offline-First BLE Mesh Beacon
- **Zero Internet Required**: Works in basements, rural zones, network outages, and airplane mode.
- **Custom 128-Bit Service UUID**: Ultra-compact 15-byte binary packet payload (`senderId`, `timestamp`, `lat`, `lon`, `flags`).
- **Foreground Guardian Listener**: Constantly listens for distress signals with low battery consumption even when the screen is locked.

### 2. ⚡ Immediate Multi-Trigger System
- **1-Tap Hero SOS Button**: Pulsating animated interface with tactile feedback.
- **3-Shake Accelerometer Sensor**: Automatically detects 3 rapid shakes (> 2.7g) within 2 seconds with a 3-second cooldown to prevent accidental false alarms.
- **5-Second Cancel Window**: Countdown circle with audio/vibration cues to allow cancellation before broadcast.

### 3. 🎙️ Offline Evidence & Location Capture
- **GPS Coordinates**: Real-time high-accuracy fused location with fallback to last-known offline GPS.
- **Audio Evidence Recorder**: Automatically captures audio on confirmed SOS and stores it securely in app-private storage.
- **Incident History (Room DB)**: Complete persistent offline audit trail of sent and received alerts with audio playback.

### 4. 🧮 Decoy Mode (Calculator Disguise)
- **Fully Functional Calculator**: Performs real arithmetic operations (`+`, `-`, `×`, `÷`, `%`).
- **Secret PIN SOS Trigger**: Entering configured code (default `1234`) and pressing `=` covertly fires the SOS broadcast.
- **Secret Unlock**: Long-pressing `=` unlocks the full safety application.

### 5. 🚨 Responder & Proximity Tools
- **RSSI Proximity Categorization**: Near (< 3m), Medium (3–10m), Far (> 10m).
- **Direct Navigation**: Opens GPS coordinates directly in offline/online navigation apps.
- **Deterrent Siren & Strobe Torch**: Helps locate victims in the dark or deter attackers.
- **Offline Safe Zones**: Directory of 24x7 women help desks, police stations, and trauma centers with one-tap dialing and navigation.

---

## 🏗️ Architecture & Module Contracts

```
com.brokencoders.narisuraksha
├── core/
│   ├── SosPacket.kt             # 15-byte compact binary packet contract
│   ├── Constants.kt             # UUIDs, thresholds, channel configurations
│   ├── DeviceIdProvider.kt      # Anonymous 16-bit random device ID
│   └── PermissionHelper.kt      # Runtime permission & battery optimization checks
├── trigger/
│   ├── SosEvent.kt              # CountdownStarted, Cancelled, Confirmed
│   ├── SosTrigger.kt            # Flow<SosEvent> contract
│   ├── ShakeDetector.kt         # Accelerometer 2.7g spike detection
│   ├── SosManager.kt            # State machine coordinating countdown & capture
│   └── FakeSosTrigger.kt        # Mock trigger for previews
├── capture/
│   ├── AudioRecorder.kt         # MediaRecorder private storage capture
│   └── LocationProvider.kt      # Offline GPS coordinate provider
├── ble/
│   ├── PacketCodec.kt           # ByteBuffer binary serializer (15 bytes)
│   ├── BleAdvertiser.kt         # BluetoothLeAdvertiser with 60s auto-stop
│   ├── BleScanner.kt            # BluetoothLeScanner with deduplication
│   ├── BleTransport.kt          # Flow<ReceivedSos> contract
│   ├── BleTransportImpl.kt      # Concrete BLE transport
│   └── FakeBleTransport.kt      # Simulation transport for UI testing
├── service/
│   ├── ScanForegroundService.kt # Always-on background BLE listener
│   ├── SosForegroundService.kt  # Active distress broadcaster
│   └── NotificationHelper.kt    # Heads-up priority notifications
├── data/
│   ├── SosEventEntity.kt        # Room database entity
│   ├── SosDao.kt                # Data access object
│   ├── AppDatabase.kt           # Room singleton
│   └── UserPreferencesRepository# DataStore preferences
├── decoy/
│   ├── CalculatorEngine.kt      # Real arithmetic engine
│   ├── DecoyViewModel.kt        # Secret PIN validation
│   └── CalculatorScreen.kt      # Authentic calculator UI
└── ui/
    ├── theme/                   # Material3 safety color system
    ├── navigation/              # Compose Navigation Graph
    ├── components/              # SosPulseButton, CountdownOverlay, StatusBadge
    ├── screens/                 # HomeScreen, AlertScreen, HistoryScreen, SafeZoneScreen, OnboardingScreen
    └── viewmodels/              # MainViewModel, HistoryViewModel
```

---

## 🛠️ Build & Verification
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)
- **Build System**: Gradle 8.13 + Kotlin 2.0.21 + Jetpack Compose Material 3

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
