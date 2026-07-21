# Football Drafts — Project Summary

**Author:** Mohammed ZainUlAbadien
**Type:** Android Mobile Application


---

## 1. Summary

Football Drafts is an Android app that helps football lovers get into a match as quickly and simply as possible. Users build a "lineup" (their squad), search for matches other people have created nearby, and send/receive match requests — similar in spirit to a matchmaking or booking platform. Under the hood, it's a good showcase of **structured data modeling, querying, and data integrity** — all directly relevant to data engineering fundamentals (schema design, relationships, transactions, filtering/querying at scale).

# Screenshots

## Login/SignUp
![Create Match](/images/login.png)
## Home Page
![Create Match](/images/HomePage.png)
## Line up
![Create Match](/images/Lineup.png)
## Create Match
![Create Match](/images/CreateMatch.png)
## Search Match
![Create Match](/images/SearchMatch.png)
## Social
![Create Match](/images/Social.png)



---

## 2. Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java) |
| Architecture | MVVM (Model-View-ViewModel) |
| Database | Cloud Firestore (NoSQL, document-based) |
| Auth | Firebase Authentication |
| Location data | Google Places API |
| Data flow | LiveData + Repository pattern |

**Why Firestore over MySQL?** Chosen deliberately for its simple, intuitive "collection/document" data model, tight integration with Firebase Auth, and easier API integration via Google Cloud — a good example of evaluating trade-offs between relational and NoSQL storage for the use case.

---

## 3. Architecture — MVVM

```
View (Activity/UI) → ViewModel → Repository → Firestore/Firebase
      
```

- **Model** — plain data classes: `User`, `Lineup`, `Player`, `Match`, `MatchRequest`
- **Repository** — `AuthRepository`, `LineupRepository`, `MatchRepository` — the only layer that talks to Firebase directly
- **ViewModel** — exposes `LiveData` to the UI, orchestrates calls between repositories
- **View** — Activities (`Login`, `Register`, `LineupView`, `CreateMatchView`, `SearchMatchView`, `RequestsView`)

This separation was a deliberate design decision to improve **testability, maintainability, and code reuse** — the same principles that matter when building maintainable data pipelines or services.

---

## 4. Data Model (Firestore Collections)

```
users/{uid}
  ├─ email, name, preferences, uid

lineups/{uid}
  ├─ lineupName
  └─ playerPositions: { "ST": Player, "GK": Player, ... }

matches/{matchId}
  ├─ hostUserId, hostName, hostLineupId
  ├─ dateTime (Firestore Timestamp)
  ├─ locationName, locationAddress, locationGeoPoint
  ├─ status: "pending_opponent" | "confirmed"
  ├─ opponentUserId, opponentLineupId, opponentName
  └─ matchRequests/{requestingUserId}   ← subcollection
        ├─ requesterName, requesterLineupId, status
```

**Key design decisions:**
- The **document ID doubles as a foreign key** (e.g., `lineups/{uid}` = user's UID), enforcing a "one lineup per user" business rule at the schema level rather than through application logic alone.
- `matchRequests` is a **subcollection**, keeping requests scoped to their parent match while still queryable globally via `collectionGroup()`.
- Using the requester's UID as the request document ID **naturally prevents duplicate requests** (no need for a manual uniqueness check).

---

## 5. Key Features & the Engineering Behind Them

### a) Authentication
- Firebase Authentication handles sign-up/login with validation (empty fields, password length, email format).
- On successful signup, a corresponding `User` document is written to Firestore — auth identity and application data are kept in sync across two systems, a common real-world integration pattern.

### b) Lineup (Squad) Management
- One lineup per user, enforced via document ID = UID.
- `SetOptions.merge()` is used so a single method (`saveCurrentUserLineup`) can **both create and update** — avoiding duplicate write logic.
- `addSnapshotListener` gives real-time updates: if the underlying data changes, the UI reflects it live.

### c) Match Creation
- Match documents store a **Firestore `Timestamp`** (not a string) for `dateTime`, enabling accurate range queries (e.g., "only future matches").
- Google Places API is used to resolve a typed location into a structured address + `GeoPoint`.

### d) Search & Filtering (the most "data" heavy part)
```java
matchesCollection
  .whereEqualTo("status", "pending_opponent")
  .whereGreaterThanOrEqualTo("dateTime", new Timestamp(new Date()))
  .orderBy("dateTime", Query.Direction.ASCENDING)
  .limit(20)
```
- Server-side filtering (status + future date) combined with a **client-side filter** to exclude matches hosted by the current user — a practical example of splitting query logic between the database and the application layer for cases NoSQL can't filter natively (`!=` on the host field).

### e) Match Requests & Data Consistency
- Incoming requests are fetched using a **`collectionGroup` query** — querying across *all* `matchRequests` subcollections at once, filtered by `hostUserId` and `status`. This is a good example of understanding Firestore's indexing/query model beyond basic single-collection reads.
- Accepting a request uses a **Firestore Transaction**, updating both the `MatchRequest` status and the parent `Match` document (status, opponent fields) atomically — ensuring the two writes succeed or fail together, which protects against partial/inconsistent state (a core data integrity concept).


## Screen
---


## 6. Problems Encountered & Solutions
- **Firestore auth failures:** despite enabling the right APIs (Identity Toolkit, Token API) and configuring OAuth, access was still denied. Resolved by regenerating the Firebase config JSON and allowing propagation time (~20 min) — a reminder that cloud service configuration changes aren't always instant.
- **Expired Places API key:** diagnosed via the error log, resolved by generating a new key and updating `strings.xml`.
- **Cross-layer debugging:** tracing bugs through the MVVM layers (View → ViewModel → Repository) reinforced the importance of clear separation of concerns and consistent naming/referencing across layers.

---

## 7. What I'd Improve / Extend
- Reference actual user documents in lineups instead of embedded player objects (better normalization vs. current denormalized approach).
- Add filtering/search parameters to match search.
- Add a messaging feature between matched users.
- Integrate a weather API to flag whether a scheduled outdoor match is weather-appropriate.
- Surface a user's own upcoming/confirmed matches on the home screen.

---
