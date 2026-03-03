# Android TV Engineering Rules & Cinematic Standards

This document defines the core principles and standards for the **Pop** project. All contributions must adhere to these rules to ensure a premium, performant, and reliable TV experience.

## 1. Architecture: The MVI Core
*   **Unidirectional Data Flow (MVI)**: Always use the MVI pattern. Consolidate screen state into a single `@Stable` or `@Immutable` data class.
*   **Atomic Intents**: User actions must be dispatched as specific `Intent` objects to the ViewModel.
*   **Stateless UI**: Composables should be as stateless as possible, relying on state hoisting and callbacks to the ViewModel.

## 2. Interaction & Focus Management
*   **Predictable Spatial Navigation**:
    *   **Vertical Reset**: When moving vertically (UP/DOWN) between content rows, always reset focus to the first item (Index 0) of the target row.
    *   **Sidebar Teleport**: Use `focusProperties` to explicitly wire focus from the content edge to the sidebar anchor.
*   **Focus Restoration**: Use `focusRestorer` on containers to remember the last focused child when returning to a group.
*   **Zero-Blind Navigation**: Every screen must have a clear initial focus set via `LaunchedEffect` and `FocusRequester`.
*   **Initialization Safety**: When using `focusProperties { enter = { ... } }`, ensure the returned `FocusRequester` is **ALWAYS** attached to a visible node (e.g., via `Modifier.focusRequester()`) to avoid `IllegalStateException`.

## 3. Performance & Stability
*   **Baseline Profiles**: A mandatory manual generation (`./gradlew :app:generateBaselineProfile`) is required before any production release to ensure zero-jank startup and scrolling.
*   **Image Stewardship**:
    *   Use **Coil** for all remote images.
    *   Always downsample images to the exact container size.
    *   Never use raw large Bitmaps in the composition thread.
*   **Stability Annotations**: Every UI data model must be annotated with `@Stable` to prevent redundant recompositions on low-power TV hardware.

## 4. Visual Design (The 10-Foot Experience)
*   **Luminance Safety**: Never use pure white (`#FFFFFF`) for background or large UI surfaces. Use **Muted White (`#F1F1F1`)** for focus outlines and highlights to prevent eye strain and "blooming."
*   **Typography**:
    *   Minimum body text size: **24sp**.
    *   Headlines: **32sp – 48sp**.
    *   Line spacing: **120% – 130%**.
*   **Safe Areas**: Inset all primary interactive content by a 5% margin (48px sides, 27px top/bottom at 1080p) to avoid overscan cropping.

## 5. Modular Framework Principles
*   **`:tv-nav-core` isolation**: Keep the navigation engine generic. It should never have hardcoded references to app-specific routes or screens.
*   **`TvNavItem` contract**: All app-level routes must implement the library's `TvNavItem` interface.
*   **Backtracking**: Always provide a cinematic "exit strategy" via `BackHandler` (Sidebar -> Home -> Exit).
