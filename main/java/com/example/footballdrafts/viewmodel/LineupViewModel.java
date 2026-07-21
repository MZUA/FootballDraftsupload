package com.example.footballdrafts.viewmodel;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.footballdrafts.model.Lineup;
import com.example.footballdrafts.repository.LineupRepository;

public class LineupViewModel extends ViewModel {

    private LineupRepository lineupRepository;
    private LiveData<Boolean> lineupSaveStatus;
    private LiveData<Lineup> currentUserLineup; // For the single lineup

    public LineupViewModel() {
        lineupRepository = new LineupRepository();
        lineupSaveStatus = lineupRepository.getSaveStatusLiveData();
        currentUserLineup = lineupRepository.getCurrentUserLineupLiveData();
    }

    /**
     * Saves/Updates the current user's lineup.
     * @param lineup The Lineup object to be saved.
     */
    public void saveOrUpdateCurrentUserLineup(Lineup lineup) {
        lineupRepository.saveCurrentUserLineup(lineup);
    }

    /**
     * Exposes LiveData for the UI to observe the status of the save operation.
     */
    public LiveData<Boolean> getLineupSaveStatus() {
        return lineupSaveStatus;
    }

    /**
     * Initiates fetching the current user's lineup.
     * The result will be available via getCurrentUserLineup().
     */
    public void fetchCurrentUserLineup() {
        lineupRepository.fetchCurrentUserLineup();
    }

    /**
     * Exposes LiveData for the UI to observe the current user's lineup.
     */
    public LiveData<Lineup> getCurrentUserLineup() {
        return currentUserLineup;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d("LineupViewModel", "ViewModel cleared");
    }
}