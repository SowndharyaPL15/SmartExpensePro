# 📱 SmartExpensePro

<div align="center">

![SmartExpensePro Banner](https://img.shields.io/badge/SmartExpensePro-Android%20%26%20Web-6366F1?style=for-the-badge&logo=android&logoColor=white)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-Render-10B981?style=for-the-badge&logo=render&logoColor=white)](https://smartexpensepro.onrender.com/)
[![Download APK](https://img.shields.io/badge/Download-Release%20APK-3B82F6?style=for-the-badge&logo=google-play&logoColor=white)](https://smartexpensepro.onrender.com/SmartExpensePro-release.apk)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

### 🚀 **[SmartExpensePro - Interactive Live Demo](https://smartexpensepro.onrender.com/)**

**A Smart, Automated Android Expense Tracker & AI SMS Debit Detection Engine.**  
Track your debited spending, analyze expenses by category, enforce monthly budgets, and auto-log transactions directly from bank SMS alerts.

</div>

---

## 🌟 Live Demo & Web Simulator

Experience the application live in your browser without installing anything:

👉 **[Launch SmartExpensePro Live Demo](https://smartexpensepro.onrender.com/)**

- **Interactive Mobile Device Simulator** with dynamic Dark UI
- **Live Bank SMS Parser** with instant regex detection
- **Real-time Chart.js Analytics** & Category Breakdown
- **Instant CSV/Excel Export** generator
- **One-Click Signed APK Download**

---

## 💡 Key Features

### 1. 🛡️ Strict Debited-Only Calculation Policy
- Accurately tracks and calculates **only debited amounts** (expenses, payments, purchases, bills).
- **Credit SMS Filter:** Automatically rejects and ignores credited SMS (e.g. salary deposits, refunds, cashbacks, transfers in) to prevent skewing your expense totals.

### 2. 🤖 Intelligent Bank SMS Expense Detection
- Background broadcast receiver (`SmsReceiver`) that automatically parses bank transaction alerts in real time.
- Supported banks & gateways: **HDFC, SBI, ICICI, Axis, UPI (GPay, PhonePe, Paytm), NetBanking, and Card transactions**.
- Automatic merchant & category detection:
  - 🍔 **Food:** Swiggy, Zomato, Starbucks, McDonald's, Dominos, KFC, Restaurants
  - 🚗 **Travel:** Uber, Ola, Rapido, Petrol/Fuel, Metro, IRCTC, Flights
  - 💡 **Bills & Utilities:** Electricity, WiFi, Airtel, Jio, Bescom, Gas, Water
  - 🛍️ **Shopping:** Amazon, Flipkart, Myntra, Zara, DMart, Blinkit
  - 💊 **Health:** Apollo Pharmacy, Hospitals, Clinics, 1mg
  - 🎬 **Entertainment:** Netflix, PVR Cinemas, BookMyShow, Spotify
  - 📚 **Education:** Tuition, Courses, Books, College fees
  - 📦 **Others:** General debit transactions

### 3. 📊 Visual Analytics & Spending Limits
- **Interactive Pie/Donut Chart:** Visual distribution of spending by category.
- **Weekly & Monthly Trend Comparisons:** Monitor weekly expense surges.
- **Budget Progress Indicator:** Live tracking against your monthly spending limit.

### 4. 📁 Instant CSV / Excel Export
- Generate and download structured `.csv` reports of all debited transactions with a single tap.

### 5. 🔒 Secure Local SQLite Storage & Sessions
- All user data and transaction logs are stored locally on device using SQLite (`DatabaseHelper`).
- No external tracking or unauthorized data transmission.

---

## 📲 Download & Installation

### Option 1: Direct APK Download (Android)
Download the signed, production-ready release APK directly to your phone:
- **[⬇️ Download SmartExpensePro-release.apk](https://smartexpensepro.onrender.com/SmartExpensePro-release.apk)** *(4.6 MB)*

### Option 2: Build from Source
1. **Clone the repository:**
   ```bash
   git clone https://github.com/SowndharyaPL15/SmartExpensePro.git
   cd SmartExpensePro
   ```

2. **Open in Android Studio:**
   - Open Android Studio &rarr; *Open an Existing Project* &rarr; Select `SmartExpensePro`.
   - Allow Gradle to sync dependencies.

3. **Build & Run:**
   - Assemble Debug APK:
     ```powershell
     .\gradlew assembleDebug
     ```
   - Assemble Signed Release APK & Bundle:
     ```powershell
     .\gradlew assembleRelease bundleRelease
     ```

---

## 🗂️ Project Structure

```
SmartExpensePro/
├── app/
│   ├── src/main/java/com/smartexpensepro/
│   │   ├── activities/
│   │   │   ├── SplashActivity.java        # Animated Splash screen
│   │   │   ├── LoginActivity.java         # User Authentication
│   │   │   ├── RegisterActivity.java      # User Registration
│   │   │   ├── DashboardActivity.java     # Main Expense Dashboard & Budget
│   │   │   ├── AddExpenseActivity.java    # Manual Expense Entry
│   │   │   └── AnalyticsActivity.java     # MPAndroidChart Visual Analytics
│   │   ├── database/
│   │   │   └── DatabaseHelper.java        # SQLite Database (Users & Transactions)
│   │   ├── models/
│   │   │   ├── Transaction.java           # Transaction Model
│   │   │   └── User.java                  # User Model
│   │   ├── receivers/
│   │   │   └── SmsReceiver.java           # Background SMS Broadcast Receiver
│   │   └── utils/
│   │       ├── SmsParser.java             # Regex Bank SMS & Debit Keyword Parser
│   │       ├── ExcelExporter.java         # CSV/Excel Exporter
│   │       └── SessionManager.java        # SharedPreferences Session Handler
│   └── src/main/res/                      # UI Layouts, Icons, Themes
├── demo_live/                             # Interactive Web Demo & Simulator
│   ├── index.html                         # Live Simulator HTML
│   ├── style.css                          # Modern Dark Glassmorphism CSS
│   ├── app.js                             # Logic Engine & Chart.js Integration
│   └── server.js                          # Node.js Static & APK Server
├── release_artifacts/                     # Pre-compiled Signed Release Binaries
│   ├── SmartExpensePro-release.apk        # Signed Release APK (4.6 MB)
│   └── SmartExpensePro-release.aab        # Google Play Bundle (4.1 MB)
├── render.yaml                            # Render 1-Click Deployment Blueprint
└── build.gradle                           # Top-level Gradle Configuration
```

---

## ☁️ Deployment on Render

This repository includes a pre-configured `render.yaml` blueprint:
1. Fork or push this repository to GitHub.
2. Sign in to **[Render.com](https://render.com)**.
3. Click **New +** &rarr; **Web Service** &rarr; Select `SmartExpensePro`.
4. Set:
   - **Root Directory:** `demo_live`
   - **Build Command:** `npm run build`
   - **Start Command:** `npm start`
5. Click **Deploy Web Service** &rarr; Live at `https://smartexpensepro.onrender.com/`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
