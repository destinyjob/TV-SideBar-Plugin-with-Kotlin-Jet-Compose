# Android TV SideBar Navigation Framework 📺🚀

A professional-grade, modular navigation framework for Android TV and Firestick applications built with **Jetpack Compose**. This plugin implements cinematic standards for focus management, spatial navigation, and performance optimization out of the box.

## 🌟 Purpose

Most Android TV navigation solutions are either too rigid or lack the polished "cinematic" feel of premium streaming services. This framework provides a pluggable, content-agnostic sidebar system that ensures your app feels state-of-the-art on any device.

## ✨ Key Features

- **Cinematic Focus Management**: Implementation of "Safe Snap" focus resets (automatic Index 0 focus on downward movement) and `focusRestorer` for stateful navigation memory.
- **Audio Feedback Integration**: Native system sound triggers (`playSoundEffect`) for D-Pad interactions and category selections, ensuring your app sounds like a native TV application.
- **Modular Architecture**: The core logic is isolated in the `:tv-nav-core` module, making it easy to drop into any new or existing project.
- **MVI Pattern**: Clean separation of Intent, Model, and State using Kotlin Flow and `@Stable` state models.
- **Performance "Baked In"**: Includes baseline profile generation scripts to eliminate JIT stutters and ensure a smooth 60FPS experience even on low-end hardware.
- **Image Optimization**: Ready-to-use `TvPosterCard` component with Coil optimization (downsampling) for memory-efficient loading.

## 🏗 Project Structure

- **`:app`**: A reference implementation demonstrating how to consume the framework with MVI architecture.
- **`:tv-nav-core`**: The standalone navigation library containing the sidebar and focus teleportation logic.
- **`:baselineprofile`**: Performance benchmarking module for generating AOT compilation profiles.

## 🚀 How to Use & Reuse

### 1. Porting to Your Project
1. Copy the `:tv-nav-core` directory to your project root.
2. Include it in your `settings.gradle`:
   ```gradle
   include ':tv-nav-core'
   ```
3. Add the dependency to your app's `build.gradle`:
   ```gradle
   implementation project(':tv-nav-core')
   ```

### 2. Integration
Implement the `TvNavItem` interface for your routes and pass them into the `TvNavigationFramework`:

```kotlin
TvNavigationFramework(
    items = listOf(Home, Movies, Settings),
    currentItemId = currentRoute,
    onItemSelected = { item -> /* Handle Navigation */ }
) { contentModifier ->
    // Your NavHost or Screen Content here
}
```

## 📈 Performance Tuning (Baseline Profiles)

This project uses **Baseline Profiles** to ensure the best possible performance on TV hardware. 
- **Pre-baked Profiles**: The project comes with a generated `baseline-prof.txt` in `app/src/main/`. This means the app is already optimized for standard navigation paths.
- **Regenerating Profiles**: If you modify the UI or navigation flow significantly, you should regenerate the profile using the `:baselineprofile` module:
  ```bash
  ./gradlew :app:generateBaselineProfile
  ```
- **Why it matters**: On Android TV (especially lower-end devices), JIT (Just-In-Time) compilation can cause "jank" during initial scrolls. Baseline Profiles pre-compile these paths, ensuring a smooth 60FPS experience from the first launch.

## 📖 Development Rules

The [rules.md](file:///rules.md) file in the root directory contains the "Development Bible" for this project. It outlines the specific coding standards, focus management patterns, and UI principles used to maintain cinematic quality. Please refer to it before making major architectural changes.

## 🛠 How to Improve

We welcome contributions! To improve this framework:
1. **Create a Feature Branch**: Always develop on a new branch (e.g., `feature/improved-animations`).
2. **Focus Areas**:
   - Customizable sidebar themes (Glassmorphism, etc.).
   - Voice navigation support.
   - Enhanced accessibility triggers.
3. **Pull Requests**: Submit a PR into the `main` branch with a clear description of the enhancement.

## 📜 License
MIT License - feel free to use this in your commercial or personal projects!

---
*Created by [Destiny Job](https://github.com/destinyjob)*
