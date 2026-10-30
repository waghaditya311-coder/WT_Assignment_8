# WT Assignment 8 — Real-Time Cricket Score Management System

## 📌 Overview
A full-stack Spring Boot application that simulates a **live cricket scoring platform** with real-time score updates, ball-by-ball commentary, player statistics, and match summaries.

---

## 🏗️ Tech Stack
| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| REST API | Spring Web (MVC) |
| Real-time | Server-Sent Events (SSE) |
| Frontend | HTML5, Vanilla CSS, Vanilla JS |
| Build | Apache Maven |
| Storage | In-Memory (no DB needed) |

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Steps
```bash
# From the project root (WT_Assignment 8/)
mvn spring-boot:run
```
Then open: **http://localhost:8080**

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/matches` | List all matches |
| GET | `/api/matches/{id}` | Full match data |
| GET | `/api/matches/{id}/summary` | Lightweight score summary |
| GET | `/api/matches/{id}/commentary?limit=25` | Recent ball commentary |
| POST | `/api/matches/{id}/simulate` | Simulate next ball |
| POST | `/api/matches/{id}/over` | Simulate a full over |
| POST | `/api/matches/{id}/start` | Start an upcoming match |
| GET | `/api/live/stream` | SSE real-time event stream |

---

## 🎯 Features

### Backend
- **Ball Simulation Engine** — Realistic probability distribution (dot 30%, single 25%, 2 runs 12%, 3 runs 3%, boundary 13%, six 7%, wicket 6%, extras 4%)
- **Full Innings Lifecycle** — Wicket handling, over rotation, innings transition, match result calculation
- **Player Statistics** — Per-player batting (runs, balls, 4s, 6s, SR) and bowling (overs, wickets, economy, maidens)
- **SSE Push** — All simulate calls broadcast match updates to connected clients
- **3 Sample Matches** — Pre-loaded at startup (IND vs AUS Live, ENG vs PAK Upcoming, SA vs NZ Live)

### Frontend
- **Live Score Dashboard** — Animated scoreboard with team flags and scores
- **Batsmen Cards** — Striker & non-striker stats with visual indicators
- **Current Bowler Card** — Economy, wickets, overs
- **This Over Visualization** — Color-coded ball dots (dot=gray, 4=blue, 6=gold, W=red, extra=orange)
- **Scorecard Tables** — Full batting and bowling scorecards
- **Commentary Feed** — Color-coded ball-by-ball commentary
- **Auto Simulate** — Configurable speed (2s / 1s / 0.5s per ball)
- **SSE Integration** — Real-time push updates without polling

---

## 📁 Project Structure
```
src/main/java/com/cricket/
├── CricketApplication.java
├── model/
│   ├── Match.java
│   ├── Innings.java
│   ├── Over.java
│   ├── Ball.java
│   ├── BattingStats.java
│   └── BowlingStats.java
├── service/
│   ├── MatchService.java       ← match management + sample data
│   └── SimulationService.java  ← ball-by-ball engine
└── controller/
    └── MatchController.java    ← REST + SSE

src/main/resources/
├── application.properties
└── static/
    ├── index.html
    ├── style.css
    └── app.js
```

---

## 👨‍💻 Author
**Aditya Wagh** — WT Assignment 8
