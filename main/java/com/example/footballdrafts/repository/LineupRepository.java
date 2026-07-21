package com.example.footballdrafts.repository;


import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.footballdrafts.model.Lineup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // For merging


public class LineupRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private CollectionReference lineupsCollection = db.collection("lineups");

    private MutableLiveData<Boolean> saveStatusLiveData = new MutableLiveData<>();
    // This will hold the single lineup for the current user
    private MutableLiveData<Lineup> currentUserLineupLiveData = new MutableLiveData<>();

    /**
     * Saves or updates the current user's single lineup.
     * The document ID in Firestore will be the user's UID.
     * @param lineup The Lineup object to save. It should have playerPositions populated.
     *               The lineupName will also be saved/updated.
     */
    public void saveCurrentUserLineup(Lineup lineup) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("LineupRepo", "User not logged in, cannot save lineup.");
            saveStatusLiveData.postValue(false);
            return;
        }
        String userId = currentUser.getUid();
        lineup.setUserId(userId); // Ensure userId is set
        lineup.setLineupId(userId); // The document ID is the userId

        // The document path is directly the user's ID
        DocumentReference userLineupDocRef = lineupsCollection.document(userId);

        // Use set with Merge to update existing fields or create if not exists.
        userLineupDocRef.set(lineup, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    saveStatusLiveData.postValue(true);
                    currentUserLineupLiveData.postValue(lineup); // Optimistically update
                })
                .addOnFailureListener(e -> {
                    saveStatusLiveData.postValue(false);
                    Log.e("LineupRepo", "Error saving user's lineup", e);
                });
    }

    /**
     * Fetches the current logged-in user's single lineup.
     * Listens for real-time updates to this lineup.
     */
    public void fetchCurrentUserLineup() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("LineupRepo", "No user logged in, cannot fetch lineup.");
            currentUserLineupLiveData.postValue(null); // Post null if no user
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference userLineupDocRef = lineupsCollection.document(userId);

        userLineupDocRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("LineupRepo", "Listen failed for user's lineup.", e);
                currentUserLineupLiveData.postValue(null); // Error case
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Lineup lineup = snapshot.toObject(Lineup.class);
                if (lineup != null) {
                    lineup.setLineupId(snapshot.getId()); // Ensure ID is set (should be userId)
                    lineup.setUserId(snapshot.getId());   // Ensure userId is also set
                }
                currentUserLineupLiveData.postValue(lineup);
            } else {
                Log.d("LineupRepo", "Current user has no lineup document yet.");
                currentUserLineupLiveData.postValue(null); // No lineup exists for the user
            }
        });
    }

    /**
     * LiveData for observing the save/update operation status.
     * @return MutableLiveData<Boolean> true if successful, false otherwise.
     */
    public MutableLiveData<Boolean> getSaveStatusLiveData() {
        return saveStatusLiveData;
    }

    /**
     * LiveData for observing the current user's single lineup.
     * @return MutableLiveData<Lineup> containing the user's lineup, or null if none exists or error.
     */
    public LiveData<Lineup> getCurrentUserLineupLiveData() {
        return currentUserLineupLiveData;
    }

}
