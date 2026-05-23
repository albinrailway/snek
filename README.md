# Snek 🐍

A modern, dark-themed Snake game built with **JavaFX** and **Supabase (PostgreSQL)**.

## Features
- **Secure Auth**: SHA-256 password hashing and session management.
- **Modern UI**: Custom dark-material arcade interface with smooth transitions.
- **Real-time Leaderboards**: Live database integration to track high scores.
- **Cross-Platform**: Built for modern desktops using Java 21.

## Tech Stack
- **Language**: Java 21 (Corretto)
- **UI Framework**: JavaFX
- **Database**: Supabase (PostgreSQL)
- **Build Tool**: Maven

## Setup
1. **Clone the repo**: `git clone <your-repo-url>`
2. **Setup Env**: Create a `.env` file in the root with your Supabase credentials:
   ```env
   DB_URL=jdbc:postgresql://your-host:6543/postgres
   DB_USER=your-user
   DB_PASSWORD=your-password
