package com.example.footballdrafts.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.footballdrafts.model.Match;
import com.example.footballdrafts.model.MatchRequest;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class MatchRepository {
    private static final String TAG = "MatchRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private CollectionReference matchesCollection = db.collection("matches");

    private MutableLiveData<Boolean> matchCreationStatus = new MutableLiveData<>();
    private MutableLiveData<String> matchCreationError = new MutableLiveData<>();

    private MutableLiveData<List<Match>> availableMatchesLiveData = new MutableLiveData<>();
    private MutableLiveData<String> fetchMatchesErrorLiveData = new MutableLiveData<>();

    // LiveData for Match Request Operations
    private MutableLiveData<Boolean> requestSentStatusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> requestSentErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<List<MatchRequest>> incomingRequestsLiveData = new MutableLiveData<>();
    private MutableLiveData<String> fetchIncomingRequestsErrorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> requestResponseStatusLiveData = new MutableLiveData<>();

    public void createMatch(Match match) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            matchCreationError.postValue("User not logged in.");
            matchCreationStatus.postValue(false);
            return;
        }
        // Host lineup ID is the host's user ID
        match.setHostLineupId(currentUser.getUid());
        match.setHostUserId(currentUser.getUid());

        // Generate a new ID for the match document
        String matchDocId = matchesCollection.document().getId();
        match.setMatchId(matchDocId);

        matchesCollection.document(matchDocId).set(match)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Match created successfully: " + matchDocId);
                    matchCreationStatus.postValue(true);
                    matchCreationError.postValue(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating match. Message: " + e.getMessage(), e);
                    matchCreationStatus.postValue(false);
                    matchCreationError.postValue("Firestore write failed: " + e.getMessage());
                });
    }


    public void fetchAvailableMatches(String currentUserIdToExclude) {
        matchesCollection
                .whereEqualTo("status", "pending_opponent")
                .whereGreaterThanOrEqualTo("dateTime", new Timestamp(new Date())) // Only show future matches
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Match> matches = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Match match = doc.toObject(Match.class);
                            // Ensure we don't show matches hosted by the current user
                            if (!match.getHostUserId().equals(currentUserIdToExclude)) {
                                matches.add(match);
                            }
                        }
                    }
                    availableMatchesLiveData.postValue(matches);
                    fetchMatchesErrorLiveData.postValue(null); // Clear previous error
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching available matches", e);
                    availableMatchesLiveData.postValue(null); // Post null on error
                    fetchMatchesErrorLiveData.postValue(e.getMessage());
                });
    }




    public MutableLiveData<Boolean> getMatchCreationStatus() {
        return matchCreationStatus;
    }
    public MutableLiveData<String> getMatchCreationError() {
        return matchCreationError;
    }

    public MutableLiveData<List<Match>> getAvailableMatchesLiveData() { return availableMatchesLiveData; }
    public MutableLiveData<String> getFetchMatchesErrorLiveData() { return fetchMatchesErrorLiveData; }


    // --- Send Match Request ---
    public void sendMatchRequest(MatchRequest request) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            requestSentErrorLiveData.postValue("User not logged in to send request.");
            requestSentStatusLiveData.postValue(false);
            return;
        }

        // Use requestingUserId as the document ID for the request to prevent duplicates by the same user for the same match
        DocumentReference requestDocRef = matchesCollection.document(request.getMatchId())
                .collection("matchRequests")
                .document(request.getRequestingUserId());

        // Check if a request already exists from this user for this match to avoid overwriting an accepted/declined one
        requestDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    MatchRequest existingRequest = document.toObject(MatchRequest.class);
                    if (existingRequest != null && !"pending".equals(existingRequest.getStatus())) {
                        requestSentErrorLiveData.postValue("You have already sent a request for this match which was " + existingRequest.getStatus() + ".");
                        requestSentStatusLiveData.postValue(false);
                        return;
                    } else if (existingRequest != null && "pending".equals(existingRequest.getStatus())) {
                        requestSentErrorLiveData.postValue("Request already pending.");
                        requestSentStatusLiveData.postValue(false); // Or true, depending on desired UX
                        return;
                    }
                }

                // If no existing request or it's fine to overwrite/send new one
                requestDocRef.set(request)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Match request sent successfully to match " + request.getMatchId() + " by user " + request.getRequestingUserId());
                            requestSentStatusLiveData.postValue(true);
                            requestSentErrorLiveData.postValue(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error sending match request for match " + request.getMatchId(), e);
                            requestSentStatusLiveData.postValue(false);
                            requestSentErrorLiveData.postValue(e.getMessage());
                        });

            } else {
                Log.e(TAG, "Error checking for existing request", task.getException());
                requestSentStatusLiveData.postValue(false);
                requestSentErrorLiveData.postValue("Error checking existing request: " + task.getException().getMessage());
            }
        });
    }

    // --- Respond to a Match Request (Accept/Decline) ---
    public void respondToMatchRequest(String matchId, String requestId, String requestingUserId, String requestingUserLineupId, String opponentName, boolean accepted) {
        FirebaseUser currentHostUser = firebaseAuth.getCurrentUser();
        if (currentHostUser == null) {
            Log.e(TAG, "Host not logged in to respond to request.");
            requestResponseStatusLiveData.postValue(false);
            return;
        }

        DocumentReference matchDocRef = matchesCollection.document(matchId);
        DocumentReference requestDocRef = matchDocRef.collection("matchRequests").document(requestId); // requestId is the document ID (which is requestingUserId)

        db.runTransaction(transaction -> {
            Match match = transaction.get(matchDocRef).toObject(Match.class);
            if (match == null) {
                throw new FirebaseFirestoreException("Match not found: " + matchId, FirebaseFirestoreException.Code.NOT_FOUND);
            }

            // Ensure the current user is indeed the host of this match
            if (!match.getHostUserId().equals(currentHostUser.getUid())) {
                throw new FirebaseFirestoreException("User is not the host of this match.", FirebaseFirestoreException.Code.PERMISSION_DENIED);
            }

            if (accepted) {
                // Only accept if match is still pending
                if (!"pending_opponent".equals(match.getStatus())) {
                    Log.w(TAG, "Attempted to accept request for a match that is no longer pending. Match status: " + match.getStatus());
                    throw new FirebaseFirestoreException("Match is no longer available to accept requests.", FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                }

                transaction.update(requestDocRef, "status", "accepted");
                transaction.update(matchDocRef, "status", "confirmed");
                transaction.update(matchDocRef, "opponentUserId", requestingUserId);
                transaction.update(matchDocRef, "opponentLineupId", requestingUserLineupId);
                transaction.update(matchDocRef, "opponentName", opponentName);


            } else {
                transaction.update(requestDocRef, "status", "declined");
            }
            return null; // Transaction success
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Successfully responded to request " + requestId + " for match " + matchId + ". Accepted: " + accepted);
            requestResponseStatusLiveData.postValue(true);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error responding to match request " + requestId + " for match " + matchId, e);
            requestResponseStatusLiveData.postValue(false);
        });
    }



    // --- Fetch Incoming Requests for a Host ---
    public void fetchIncomingRequestsForHost(String hostId) {
        fetchIncomingRequestsErrorLiveData.postValue(null); // Clear previous error

        // Query across all "matchRequests" subcollections where the hostUserId matches and status is "pending"
        db.collectionGroup("matchRequests")
                .whereEqualTo("hostUserId", hostId)
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest requests first
                .addSnapshotListener((snapshots, e) -> { // Real-time listener
                    if (e != null) {
                        Log.e(TAG, "Error fetching incoming requests for host " + hostId, e);
                        incomingRequestsLiveData.postValue(null);
                        fetchIncomingRequestsErrorLiveData.postValue(e.getMessage());
                        return;
                    }
                    List<MatchRequest> requests = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            MatchRequest req = doc.toObject(MatchRequest.class);
                            req.setRequestId(doc.getId()); // The document ID is the requestingUserId
                            requests.add(req);
                        }
                        Log.d(TAG, "Fetched " + requests.size() + " incoming requests for host " + hostId);
                    } else {
                        Log.d(TAG, "Snapshots was null fetching incoming requests for host " + hostId);
                    }
                    incomingRequestsLiveData.postValue(requests);
                });
    }
    public MutableLiveData<List<MatchRequest>> getIncomingRequestsLiveData() { return incomingRequestsLiveData; }
    public MutableLiveData<String> getFetchIncomingRequestsErrorLiveData() { return fetchIncomingRequestsErrorLiveData; }

    public MutableLiveData<Boolean> getRequestResponseStatus() { return requestResponseStatusLiveData; }
    public MutableLiveData<Boolean> getRequestSentStatus() { return requestSentStatusLiveData; }
    public MutableLiveData<String> getRequestSentError() { return requestSentErrorLiveData; }


}
