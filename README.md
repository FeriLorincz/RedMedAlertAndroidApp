# RedMedAlert - Health Monitoring Mobile App (Part)

RedMedAlert is an automated health monitoring system designed to detect medical emergencies for elderly users without requiring conscious intervention. This repository contains the Android mobile application that connects to Samsung Galaxy Watches, collects sensor data, and communicates with the backend system.

## 📱 Overview

The RedMedAlert mobile app serves as the bridge between wearable devices and the emergency detection backend. It:

- Connects to Samsung Galaxy Watches via the Samsung Health SDK
- Collects and processes sensor data in real-time
- Detects potential emergency situations locally
- Communicates with the backend for advanced processing
- Provides a user-friendly interface for elderly users and caregivers
- Manages emergency contacts and medical profiles

## 🏗️ Architecture

The application follows a layered architecture:

```
com.redmedalert
├── ui              # User interface components
├── data            # Data handling and repositories
├── sensor          # Sensor data collection and processing
├── service         # Background services
├── util            # Utility classes
├── auth            # Authentication components
├── health          # Samsung Health SDK integration
└── emergency       # Emergency detection components
```

![architecture-diagram](https://github.com/user-attachments/assets/738636e6-30cf-4035-a4b7-9badd360e9d9)


## 📱 Screens

The app includes several key screens:

- **Login/Registration**: User authentication
- **Home**: Dashboard with current health status
- **Profile**: User information and medical profile
- **Contacts**: Emergency contact management
- **Medical Profile**: Health information and medication management
- **Settings**: App configuration and notification preferences
- **Emergency**: Manual emergency trigger and status

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 21+
- Samsung Health SDK
- Google Play Services

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/redmedalert-android.git
   cd redmedalert-android
   ```

2. Open the project in Android Studio

3. Configure Samsung Health SDK:
   - Register your app in the Samsung Developer Portal
   - Add your API keys to `local.properties`:
     ```
     samsung.health.key=YOUR_API_KEY
     ```

4. Configure the backend URL in `app/src/main/res/values/config.xml`:
   ```xml
   <string name="api_base_url">https://your-backend-url.com/api/</string>
   ```

5. Build and run the application on your device or emulator

## 🔍 Key Features

### Samsung Health Integration

The app integrates with Samsung Health to collect data from:

- Heart Rate Sensor
- Blood Oxygen Sensor
- Accelerometer/Gyroscope
- Temperature Sensor
- Bioactive Sensors

### Background Monitoring

The app includes:

- A foreground service for continuous monitoring
- Battery-optimized sensor polling
- Offline data buffering when connectivity is lost
- Adaptive sampling rates based on user activity

### Emergency Detection

Local emergency detection capabilities include:

- Fall detection using accelerometer data
- Abnormal heart rate detection
- Oxygen saturation monitoring
- Movement pattern analysis

### User Management

The app provides:

- User profile management
- Medical history records
- Medication tracking
- Emergency contact management

## 📡 Integration with Backend

The mobile app communicates with the RedMedAlert backend:

- Authentication using JWT tokens
- Secure transfer of sensor data
- Receiving emergency alerts and notifications
- Synchronization of user profiles and medical data

## 🛠️ Technologies Used

- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **Network**: Retrofit, OkHttp
- **Local Storage**: Room Database
- **Async Processing**: Kotlin Coroutines
- **UI Components**: Material Design
- **Dependency Injection**: Hilt/Dagger
- **Image Loading**: Glide

## 🔒 Privacy & Security

The app includes:

- Encrypted local storage of sensitive data
- Secure communication with the backend
- Runtime permissions management
- Data minimization practices

## 📝 Project Status

This project is currently under development. Core features are implemented, but full integration with Samsung Watch devices has limitations due to current sensor capabilities.

## 📱 Supported Devices

- **Smartphones**: Android 5.0 (API 21) and higher
- **Smartwatches**: Samsung Galaxy Watch 4, 5, 6, and 7 series

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributors

- Feri Lorincz:
- https://github.com/FeriLorincz
