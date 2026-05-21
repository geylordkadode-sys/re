# Sdd Marketplace 🛍️

A complete production-ready Android marketplace app built with Kotlin, Jetpack Compose, and Supabase backend.

## 📱 Features

- **Authentication**: Email/password, phone/OTP, guest sign-in
- **Home Feed**: Featured products, categories, infinite scroll with pagination
- **Product Listings**: Full CRUD — create, edit, delete, mark sold
- **Product Detail**: Image gallery, color selector, ratings/reviews, seller info
- **Real-time Chat**: Text, images, location messages (Google Maps), typing indicator, read receipts
- **Inbox**: Chat list, unread counts, search, filters
- **Profile**: Seller shop, follow/unfollow, verification badge
- **Search**: Unified search across products and sellers
- **Notifications**: Real-time FCM push notifications
- **Wishlist**: Save and manage favorite products
- **Post Product**: 3-step listing wizard with photo upload

## 🏗️ Architecture

```
app/
├── core/
│   ├── di/              DI modules (Hilt)
│   ├── navigation/      Compose Navigation graph
│   ├── notification/    FCM + BootReceiver
│   ├── ui/
│   │   ├── components/  Reusable Compose components
│   │   └── theme/       Material 3 pink theme
│   ├── utils/           Image compression, etc.
│   └── worker/          Background sync (WorkManager)
├── data/
│   ├── local/           Room database, DAOs, entities
│   ├── mappers/         DTO ↔ Domain mappers
│   ├── remote/dto/      Supabase API DTOs
│   └── repository/      Repository implementations
├── domain/
│   ├── model/           Domain models
│   └── repository/      Repository interfaces
└── feature/
    ├── auth/            Login, Register, OTP, Forgot Password
    ├── chat/            Inbox, Chat Detail (real-time)
    ├── home/            Home feed
    ├── notifications/   Notifications screen
    ├── product/         Detail, Post, Wishlist
    ├── profile/         Profile, Edit Profile
    └── search/          Search screen
```

## 🔧 Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Backend | Supabase (Postgres + Auth + Realtime + Storage) |
| Local DB | Room + SQLCipher encryption |
| Networking | Ktor (via Supabase SDK) |
| Images | Coil |
| Paging | Paging 3 |
| Maps | Google Maps Compose |
| Push Notifications | Firebase Cloud Messaging |
| Background Sync | WorkManager |

## ⚙️ Setup

### 1. Clone and open in Android Studio

```bash
git clone https://github.com/geylordkadode-sys/gfgf.git
cd gfgf
```

### 2. Configure API Keys

In `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "MAPS_API_KEY", "\"YOUR_GOOGLE_MAPS_API_KEY\"")
```

Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/).

### 3. Setup Supabase Database

Run `supabase_schema.sql` in your Supabase SQL editor:
- Supabase Project: [fkeuioagahwqgpqjuwqj](https://supabase.com/dashboard/project/fkeuioagahwqgpqjuwqj)
- Go to **SQL Editor** → paste and run the schema

### 4. Create Storage Buckets

In Supabase Dashboard → Storage, create:
- `product-images` (public)
- `avatars` (public)
- `chat-images` (private)

Then uncomment and run the storage policy SQL at the bottom of `supabase_schema.sql`.

### 5. Add `google-services.json`

Place your Firebase `google-services.json` in `/app/` directory (for FCM push notifications).

### 6. Build and Run

```bash
./gradlew assembleDebug
```

## 🗄️ Supabase Configuration

- **Project URL**: `https://fkeuioagahwqgpqjuwqj.supabase.co`
- **Anon Key**: Already configured in `build.gradle.kts`
- **Realtime**: Enabled for `messages`, `chats`, `notifications`, `products`

## 🚀 Release Build

```bash
./gradlew assembleRelease
```

The release build uses:
- R8 minification + ProGuard
- Resource shrinking
- SQLCipher database encryption

## 📊 Database Schema

See `supabase_schema.sql` for the complete schema including:
- Tables: users, products, chats, messages, reviews, favorites, followers, notifications, blocked_users, reports
- Row Level Security policies
- Auto-triggers (user creation, rating updates, counters)
- Realtime subscriptions
- Storage bucket setup

## 🎨 Design

- Pink pastel UI theme (`#E91E8C` primary)
- Material 3 design system
- Custom bottom navigation with center FAB for posting
- Shimmer loading states
- Pull-to-refresh
- Animated transitions

## 📄 License

MIT License — Copyright (c) 2025 Sdd Marketplace
