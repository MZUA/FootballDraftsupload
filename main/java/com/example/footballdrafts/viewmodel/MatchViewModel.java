package com.example.footballdrafts.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.footballdrafts.model.Lineup;
import com.example.footballdrafts.model.Match;
import com.example.footballdrafts.model.MatchRequest;
import com.example.footballdrafts.repository.LineupRepository;
import com.example.footballdrafts.repository.MatchRepository;

import java.util.List;

public class MatchViewModel extends ViewModel {
    private MatchRepository matchRepository;
    private LineupRepository lineupRepository;

    private LiveData<Boolean> matchCreationStatus;
    private LiveData<String> matchCreationError;
    private LiveData<Lineup> currentUserLineupLiveData; // This will be observed by the UI

    // For Fetching Available Matches
    private LiveData<List<Match>> availableMatchesLiveData;
    private LiveData<String> fetchMatchesErrorLiveData;

    // LiveData for Match Request Operations
    private LiveData<Boolean> requestSentStatusLiveData;
    private LiveData<String> requestSentErrorLiveData;
    private LiveData<List<MatchRequest>> incomingRequestsLiveData;
    private LiveData<String> fetchIncomingRequestsErrorLiveData;
    private LiveData<Boolean> requestResponseStatusLiveData;

    public MatchViewModel() {
        matchRepository = new MatchRepository();
        lineupRepository = new LineupRepository();

        // Get the LiveData object from lineup the repository
        currentUserLineupLiveData = lineupRepository.getCurrentUserLineupLiveData();

        // Initialize LiveData for fetching matches
        availableMatchesLiveData = matchRepository.getAvailableMatchesLiveData();
        fetchMatchesErrorLiveData = matchRepository.getFetchMatchesErrorLiveData();
        matchCreationStatus = matchRepository.getMatchCreationStatus();
        matchCreationError = matchRepository.getMatchCreationError();

        // Initialize request-related LiveData
        requestSentStatusLiveData = matchRepository.getRequestSentStatus();
        requestSentErrorLiveData = matchRepository.getRequestSentError();
        incomingRequestsLiveData = matchRepository.getIncomingRequestsLiveData();
        fetchIncomingRequestsErrorLiveData = matchRepository.getFetchIncomingRequestsErrorLiveData();
        requestResponseStatusLiveData = matchRepository.getRequestResponseStatus();
    }

    public void createMatch(Match match) {
        matchRepository.createMatch(match);
    }


    // Method to trigger fetching available matches
    public void triggerFetchAvailableMatches(String currentUserIdToExclude) {
        matchRepository.fetchAvailableMatches(currentUserIdToExclude);
    }

    public LiveData<Boolean> getMatchCreationStatus() {
        return matchCreationStatus;
    }

    public LiveData<String> getMatchCreationError() {
        return matchCreationError;
    }

    public void triggerFetchCurrentUserLineup() {
        lineupRepository.fetchCurrentUserLineup();
    }


    /**
     * The UI will observe this LiveData to get updates about the current user's lineup.
     * @return LiveData<Lineup>
     */
    public LiveData<Lineup> getCurrentUserLineup() { // Renamed for clarity
        return currentUserLineupLiveData;
    }



    // Getters for available matches
    public LiveData<List<Match>> getAvailableMatchesLiveData() { return availableMatchesLiveData; }
    public LiveData<String> getFetchMatchesErrorLiveData() { return fetchMatchesErrorLiveData; }


    // --- Match Request Operations ---
    /**
     * Sends a request to join a match.
     * @param request The MatchRequest object to be sent.
     */
    public void sendMatchRequest(MatchRequest request) {
        matchRepository.sendMatchRequest(request);
    }

    /**
     * Triggers fetching of incoming match requests for the specified host.
     * @param hostId The UID of the host whose requests are to be fetched.
     */
    public void triggerFetchIncomingRequests(String hostId) {
        matchRepository.fetchIncomingRequestsForHost(hostId);
    }

    /**
     * Responds to an incoming match request (accepts or declines).
     * @param matchId ID of the match the request pertains to.
     * @param requestId ID of the request document (usually the UID of the requester).
     * @param requestingUserId UID of the user who sent the request.
     * @param requestingUserLineupId Lineup ID of the requester.
     * @param opponentName Display name of the requester (to be stored as opponentName if accepted).
     * @param accepted True if accepting, false if declining.
     */
    public void respondToMatchRequest(String matchId, String requestId, String requestingUserId, String requestingUserLineupId, String opponentName, boolean accepted) {
        matchRepository.respondToMatchRequest(matchId, requestId, requestingUserId, requestingUserLineupId, opponentName, accepted);
    }

    // --- Getters for Match Request LiveData ---
    public LiveData<Boolean> getRequestSentStatus() { return requestSentStatusLiveData; }
    public LiveData<String> getRequestSentError() { return requestSentErrorLiveData; }
    public LiveData<List<MatchRequest>> getIncomingRequestsLiveData() { return incomingRequestsLiveData; }
    public LiveData<String> getFetchIncomingRequestsErrorLiveData() { return fetchIncomingRequestsErrorLiveData; }
    public LiveData<Boolean> getRequestResponseStatus() { return requestResponseStatusLiveData; }








}

