# Architecture

TubePreview is a single-module Android app built with Kotlin and Jetpack Compose. It has no backend and no account layer.

## Data flow

`SettingsRepository` exposes persisted preferences as a flow of `CreatorSettings`. `CreatorViewModel` combines settings with a `FeedLayout` to produce the home feed and Shorts state. Compose screens collect these flows with lifecycle awareness and send user actions back to the ViewModel.

```text
Preferences DataStore → SettingsRepository → CreatorViewModel → Compose screens
                                             ↑                  │
                                             └── user actions ──┘
```

## Domain state

- `FeedComposer` inserts the active preview among sample videos and reshuffles surrounding content on refresh.
- `ABPreviewSettings` owns comparison mode, readiness, and cycling. Option A shares the ordinary preview; options B and C are additional choices.
- `AvatarCrop` stores normalized position and zoom. Framing is shared between the editor, channel avatars, and navigation avatar.
- `PreviewTitleMigration` updates the old untouched sample title while preserving custom titles.

## Image imports

`ImageStorage` copies a selected URI into a unique private file, bounds actual bytes and decoded dimensions, and rejects invalid images. The ViewModel serializes import/reset operations with a mutex. Existing images remain selected until replacement succeeds; unused files are removed after persistence.

## UI

`MainActivity` owns navigation, theme, startup handoff, and image warnings. Onboarding, home, and settings are separate screens. Shared components provide consistent dialogs, controls, avatar framing, and comparison editing.

The feed's scroll offsets are read during layout, and the status-bar tint reads state during drawing. `FeedBackdrop` records the feed layer and samples only the navigation strip for blur on Android 12 or newer. Older versions use a translucent tint.

## Tests

Local tests use JUnit, coroutines test utilities, and Robolectric. Compose tests cover interaction, persistence, image validation, crop geometry, comparison cycling, and system-bar rendering. Native graphics are used where pixel output matters.

Keep domain calculations independent of Android UI where practical. Verify behavior changes with focused tests and UI changes in both themes.
