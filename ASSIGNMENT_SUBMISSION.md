# Assignment Submission — Contactly

**Assignment:** Pynexia Week 2 — Lists, Adapters, RecyclerView & Local Data Persistence  
**Application Name:** Contactly  
**Package Name:** `com.snj.contactly`  

---

## Project Description

Contactly is a native Android contact management application built in Kotlin using XML Views, Material Design 3, `RecyclerView`, Room Database, and MVVM architecture. Users can create, view, search, favorite, edit, and delete contacts with local SQLite persistence via Room, and trigger native Call (`ACTION_DIAL`), SMS (`ACTION_SENDTO`), and Email (`ACTION_SENDTO`) intents.

---

## Implemented Features

- **Contacts Dashboard:** Branded home screen with live contact/favorite summary, alphabetical sorting, Favorites section, All Contacts section, filter chips (`All` / `Favorites`), and contextual empty states.
- **Custom RecyclerView & Adapter:** `ContactAdapter` (`ListAdapter` + `DiffUtil`), `ContactViewHolder`, `SectionHeaderAdapter`, and `ConcatAdapter` with row click, favorite toggle, and quick-call actions.
- **Full CRUD with Room Database:**
  - **Create:** Add contact screen with required name/phone validation, optional email validation, company, job title, notes, favorite toggle, and gallery photo picker.
  - **Read:** Reactive Room `Flow` queries driving `ContactsFragment` and `ContactDetailsFragment`.
  - **Update:** Edit contact screen prepopulated from Room, preserving contact `id` and `createdAt` while refreshing `updatedAt`.
  - **Delete:** Confirmation dialog before deleting from Room, returning to the dashboard with a `Snackbar`.
- **Live Search:** Real-time filtering by name, phone number, or email with a clear button and dedicated no-results state.
- **Favorites Management:** Persistent favorite status toggle from both the contacts list and the details screen, plus Favorites section and chip filtering.
- **Implicit Android Intents:** Safe `ACTION_DIAL`, `ACTION_SENDTO` (`sms:`), and `ACTION_SENDTO` (`mailto:`) launchers with graceful `Snackbar` handling when no compatible app exists.
- **Optional Sample Data:** Load or remove 8 distinguishable sample contacts without duplicating entries or affecting user-created contacts.

---

## Technologies Used

- Kotlin & Java 17
- Android XML Views & ViewBinding
- Material Components for Android (Material 3 Light & Dark themes)
- `RecyclerView` (`ListAdapter`, `DiffUtil`, `ConcatAdapter`)
- Room Database + KSP
- Jetpack Navigation Component (`NavHostFragment`)
- Lifecycle `ViewModel`, Kotlin Coroutines, `StateFlow`, `SharedFlow`
- JUnit 4 & `kotlinx-coroutines-test`

---

## Concepts Demonstrated

1. **RecyclerView & Custom Adapter Design:** Separating binding (`ContactViewHolder`), diffing (`DiffUtil.ItemCallback`), and multi-section composition (`ConcatAdapter`).
2. **Local Persistence with Room:** Defining entities, DAOs with reactive `Flow` queries and `suspend` mutations, and a thread-safe database singleton without main-thread blocking.
3. **MVVM + Repository Separation:** Keeping UI controllers (`Fragment`s) thin while centralizing business logic, validation, and state transformation in `ViewModel`s and `ContactRepository`.
4. **Defensive Android Development:** Input validation, non-crashing implicit intents, persistable photo URI handling, and duplicate-navigation guards.

---

## Submission Summary

Contactly delivers a complete, tested, Material 3 native Android Contacts application for the Pynexia Week 2 assignment. It demonstrates `RecyclerView` list virtualization with `ListAdapter` and `DiffUtil`, reactive local data persistence with Room and Kotlin Coroutines, clean MVVM + Repository architecture, full CRUD workflows, live search, favorites filtering, and implicit Call/SMS/Email intents.

