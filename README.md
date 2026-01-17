# 🚗 Cone Crash – Android Game

**Cone Crash** is a simple **Endless Runner** game developed in **Kotlin**, where the player drives a car in a 5-lane road, avoids traffic cones, collects coins, and tries to achieve the longest distance.

All scores are saved **locally on the device** and shared across **all players** who played on the same device.

---

## 📱 Overview

The goal is to survive as long as possible by:
- Avoiding traffic cones 🚧 (losing a life on collision)
- Collecting coins 💰
- Keeping your lives ❤️ (3 lives total)

The score is based on **distance traveled (km)**.

---

## 🖼️ Screenshots

### Menu Screen
![Menu Screen](menu_photo.png)

### Gameplay Screen
## 🎮 Control Modes

| Buttons Mode                 | Sensors Mode                  |
|------------------------------|-------------------------------|
| ![Buttons](Arrows_photo.png) | ![Sensors](Sensors_photo.png) |


---

## 🔄 Application Flow

### 1️⃣ Menu Screen (`MenuActivity`)
- Enter player name (required)
- Choose control mode:
    - **Arrows** (Buttons)
    - **Sensors** (Tilt)
- Open the **Top 10** screen (shows global device scores)
- Includes a **Fast Mode toggle** (UI exists)

---

### 2️⃣ Gameplay (`MainActivity` + `GameManager`)
- Game runs in a loop using a `Handler` and `Runnable` (`tick()` each cycle)
- 5 lanes
- Items move downward each tick:
    - `CONE` = obstacle
    - `COIN` = collectible
- Player starts with **3 lives**
- UI updates each tick:
    - Distance (km)
    - Lives
    - Coins collected

**Coins**
- Coin total is tracked in `GameManager.coinsCollected`
- UI shows the live coin count (`binding.lblCoins`)

**Distance**
- Distance is updated every tick using time delta:
    - speed = `30 m/s`
    - displayed as km

---

### 3️⃣ Game Over
The game ends when lives reach 0.

On game over:
- Score is saved locally via `TopTenStore.addRun(...)`
- Each record includes:
    - Player name
    - Distance (km)
    - Coins collected
    - GPS location (lat/lon)
    - Timestamp (`atMillis`)
- App navigates automatically to the Top 10 screen

---

### 4️⃣ Top 10 + Map (`TopTenManager`, `ListFragment`, `FragmentMap`)
Top Ten shows the **Top 10 runs across ALL players on the device**.

**Storage logic**
- Scores are saved in `SharedPreferences` (`top_ten_prefs`)
- Stored under one global key: `scores_all`
- Loaded via: `TopTenStore.loadTop10Global(context)`
- Sorted by **distance (km)** and trimmed to 10 entries

**List**
Each entry shows:
- Rank
- Player name
- Distance (km)
- Coins collected

**Map**
- A Google Map is shown beside the list
- Clicking a score item moves the map camera to that run’s saved location
- Default map focus is Tel Aviv if nothing else is available

### Top 10 Screen
![Top 10 Screen](Top_Ten_photo.png)
---

## 🎮 Controls

### Arrows Mode
- On-screen left/right buttons move the car one lane at a time

### Sensors Mode
- Uses accelerometer via `CarTiltController`
- Includes:
    - Dead zone
    - Tilt threshold
    - Cooldown between moves

---


## 📍 Location
- Uses `FusedLocationProviderClient` via `LocationHelper`
- Requests location permission
- Retrieves one high-accuracy update and stops updates
- Saves last known location with the score

---

## 🧠 Project Structure

- **data**
    - `ScoreEntry` – model for a run result
    - `TopTenStore` – local persistence (SharedPreferences + Gson)
- **logic**
    - `GameManager` – core game rules, tick loop data, collisions, distance, coins
    - `CarTiltController` – sensor-based lane movement
- **ui**
    - `MenuActivity` – name input + mode selection + top10 navigation
    - `MainActivity` – renders gameplay and runs game loop
    - `TopTenManager` – displays top10 list + map
- **fragments**
    - `ListFragment` – RecyclerView leaderboard
    - `FragmentMap` – Google Map + camera movement
- **utilities**
    - `LocationHelper`, `BackgroundSoundPlayer`, `SoundEffectsManager`, `VibrationHelper`
- **app**
    - `MyApp` – initializes and preloads sounds

---

## 🛠️ Tech Stack
- Kotlin
- XML layouts + ViewBinding
- Google Maps SDK
- Fused Location Provider (Location Services)
- SharedPreferences + Gson
- Accelerometer Sensors
- MediaPlayer + SoundPool

---

## ▶️ How to Run
1. open the project in Android Studio
2. Make sure Google Maps API key is configured (if needed)
3. Run on a physical device or emulator (location features work best on a real device)
4. Enter your name
5. Choose control mode (Arrows / Sensors)

---

## 📌 Notes
- Scores are stored **locally only** (no server/backend)
- Top 10 is **global per device**, not per player

---
