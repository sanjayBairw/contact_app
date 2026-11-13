# Contactly — Pynexia Week 2 Contact Management App

**Assignment:** Pynexia Week 2 — *Lists, Adapters, RecyclerView & Local Data Persistence*  
**Package:** `com.snj.contactly`  
**Stack:** Native Android (Kotlin, XML Views, ViewBinding, Material Design 3, Room, MVVM, Navigation Component)

---

## Overview

**Contactly** is a native Android contact management application built strictly with Kotlin and XML Views (no Jetpack Compose, no Flutter/Dart). It demonstrates scalable list rendering with `RecyclerView`, a custom `ListAdapter` + `DiffUtil`, local persistence with **Room Database**, reactive UI updates using **Kotlin Coroutines & Flow**, and clean **MVVM + Repository** architecture.

---

## Features

- **Home / Contacts Dashboard (`ContactsFragment`)**
  - Contactly branding and live contact + favorite counter
  - Live search bar filtering across **name**, **phone**, and **email** with a one-tap clear button
  - Filter chips (**All** and **Favorites**)
  - Dedicated **Favorites** section and **All Contacts** alphabetical section inside a single `RecyclerView` using `ConcatAdapter`
  - Custom row cards showing circular photo or deterministic initials avatar, full name, phone number, favorite star toggle, and quick-call action
  - Contextual empty states for empty address book, empty favorites, and no search results
- **Add Contact (`AddContactFragment`) & Edit Contact (`EditContactFragment`)**
  - Full name (required), phone number (required + digit/format validation), email (optional + format validation), company, job title, notes, favorite toggle, and gallery profile photo picker with persistable URI permission
  - Inline `TextInputLayout` error feedback, keyboard `imeOptions` flow, and automatic list/details refresh on save
- **Contact Details (`ContactDetailsFragment`)**
  - Large avatar/photo header, role & company subtitle, favorite/sample chips, contact info card, work card, notes card, and last-updated timestamp
  - Implicit Android intents with graceful missing-app error handling (`Snackbar`):
    - **Call:** `Intent.ACTION_DIAL` (`tel:`) — no `CALL_PHONE` permission required
    - **Message:** `Intent.ACTION_SENDTO` (`sms:`)
    - **Email:** `Intent.ACTION_SENDTO` (`mailto:`)
  - Favorite toggle, Edit navigation, and Delete confirmation dialog
- **Optional Sample Data**
  - One-tap menu/empty-state option to load 8 realistic sample contacts (`isSample = true`), clearly badged as `Sample`, prevented from duplicating, and removable at any time without affecting user-created contacts

---

## Technology Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin (Java 17 bytecode target) |
| UI Toolkit | Android XML Views, ViewBinding, Material Components (Material 3) |
| List Rendering | `RecyclerView`, `ConcatAdapter`, `ListAdapter`, `DiffUtil`, `ContactViewHolder` |
| Architecture | MVVM (`ViewModel`, `StateFlow`, `SharedFlow`) + Repository Pattern |
| Local Persistence | Room Database (`@Entity`, `@Dao`, `@Database`) + KSP |
| Async & Reactivity | Kotlin Coroutines & `Flow` |
| Navigation | Jetpack Navigation Component (`NavHostFragment`, `nav_graph.xml`) |
| Build System | Gradle Kotlin DSL (`9.4.1` wrapper, AGP `9.0.1`) |

---

## Architecture

```text
com.snj.contactly
├── ContactlyApplication.kt
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── ContactEntity.kt
│   │   ├── ContactDao.kt
│   │   └── ContactDatabase.kt
│   └── repository/
│       └── ContactRepository.kt
├── ui/
│   ├── contacts/
│   │   ├── ContactsFragment.kt
│   │   ├── ContactAdapter.kt
│   │   ├── ContactViewHolder.kt
│   │   ├── SectionHeaderAdapter.kt
│   │   └── ContactsViewModel.kt
│   ├── details/
│   │   ├── ContactDetailsFragment.kt
│   │   └── ContactDetailsViewModel.kt
│   ├── form/
│   │   ├── ContactFormViewModel.kt
│   │   └── ContactFormBinder.kt
│   ├── add/
│   │   └── AddContactFragment.kt
│   └── edit/
│       └── EditContactFragment.kt
└── utils/
    ├── AvatarUtils.kt
    ├── IntentUtils.kt
    ├── SampleDataProvider.kt
    └── ValidationUtils.kt
```

---

## Database Structure (Room)

Table: `contacts` (`ContactEntity.kt`)

| Column | Type | Notes |
| --- | --- | --- |
| `id` | `Int` | Primary key, `autoGenerate = true` |
| `name` | `String` | Required contact full name |
| `phone` | `String` | Required phone number |
| `email` | `String?` | Optional email address |
| `company` | `String?` | Optional company name |
| `jobTitle` | `String?` | Optional job title |
| `notes` | `String?` | Optional freeform notes |
| `photoUri` | `String?` | Optional persisted content URI |
| `isFavorite` | `Boolean` | Favorite status flag |
| `isSample` | `Boolean` | Distinguishes optional sample contacts |
| `createdAt` | `Long` | Creation epoch timestamp |
| `updatedAt` | `Long` | Last modified epoch timestamp |

`ContactDao` uses `Flow` for reactive queries (`getAllContacts`, `getContactById`, `searchContacts`, `getFavoriteContacts`, `countSampleContacts`) and `suspend` functions for mutations (`insertContact`, `updateContact`, `deleteContact`, `toggleFavorite`, `deleteSampleContacts`), keeping all database I/O off the main thread.

---

## RecyclerView & Custom Adapter Explained

- **`ContactAdapter` (`ListAdapter<ContactEntity, ContactViewHolder>`)**: Uses `DiffUtil.ItemCallback` (`areItemsTheSame` checks `id`, `areContentsTheSame` checks `ContactEntity` structural equality) so inserts, edits, deletes, favorites, and search filtering animate smoothly without calling `notifyDataSetChanged()`.
- **`ContactViewHolder`**: Binds `ItemContactBinding` directly via ViewBinding, handles circular photo vs. deterministic initials avatar rendering, updates favorite star state, and wires row click, favorite toggle, and quick-call callbacks.
- **`ConcatAdapter` + `SectionHeaderAdapter`**: Combines the Favorites section header, Favorites `ContactAdapter`, All Contacts header, and All Contacts `ContactAdapter` in a single `RecyclerView` so all rows share one scroll container and view recycling pool.

---

## CRUD Operations

- **Create:** `AddContactFragment` validates inputs with `ValidationUtils`, then `ContactFormViewModel` inserts a new `ContactEntity` via `ContactRepository.insertContact(...)`.
- **Read:** `ContactsViewModel` and `ContactDetailsViewModel` observe Room `Flow` streams and expose `StateFlow` UI states collected lifecycle-safely via `repeatOnLifecycle(Lifecycle.State.STARTED)`.
- **Update:** `EditContactFragment` loads the existing `ContactEntity`, prepopulates the form, preserves `id`, `createdAt`, and `isSample`, updates `updatedAt`, and calls `ContactRepository.updateContact(...)`.
- **Delete:** `ContactDetailsFragment` confirms deletion with a Material 3 `MaterialAlertDialogBuilder`, deletes the record through `ContactDetailsViewModel`, and returns to `ContactsFragment` with a `Snackbar` confirmation.

---

## Screenshots

*(Add device/emulator screenshots here after installing `app-debug.apk`)*

- Home / Contacts Dashboard (Light & Dark Mode)
- Add / Edit Contact Form
- Contact Details & Quick Intents
- Search & Favorites Filtering

---

## Installation & Build Instructions

1. Open `D:\Desktop\Pynexia\contact_app` in Android Studio (or build from terminal with Java 17 and Android SDK configured).
2. Build the debug APK using the Gradle Wrapper:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. Output APK path:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Install on a connected device or emulator (`minSdk 23`, `targetSdk 36`):
   ```powershell
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## Testing

Run the JVM unit test suite:
```powershell
.\gradlew.bat testDebugUnitTest
```

Unit tests cover:
- `ValidationUtilsTest`: Name, phone, email, and aggregated form validation
- `AvatarUtilsTest`: Initials extraction and deterministic palette mapping
- `ContactRepositoryTest`: CRUD operations, search queries, favorite toggling, and idempotent sample data insertion/removal
- `ContactsViewModelTest`: Alphabetical sorting, live search, favorites filtering, empty state transitions, and sample data actions
- `ContactFormAndDetailsViewModelTest`: Validation guards, add/edit preservation of `id`/`createdAt`/`isSample`, favorite toggle, and delete events
- `ContactDaoInstrumentedTest` (`androidTest`): In-memory Room database verification

---

## Learning Outcomes

- Implementing efficient list updates in `RecyclerView` using `ListAdapter`, `DiffUtil`, `ViewHolder`, and `ConcatAdapter`
- Persisting structured data locally with Room (`@Entity`, `@Dao`, `@Database`) and background Coroutines/`Flow`
- Structuring a maintainable multi-screen Android app with MVVM, Repository pattern, Jetpack Navigation, ViewBinding, and Material Design 3

