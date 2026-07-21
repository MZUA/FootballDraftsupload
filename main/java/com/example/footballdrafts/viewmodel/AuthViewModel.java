package com.example.footballdrafts.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.footballdrafts.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;

    private LiveData<FirebaseUser> userOperationResultLiveData;
    private LiveData<Boolean> loggedOutLiveData;
    private LiveData<Boolean> userProfileCreationStatusLiveData;
    private LiveData<String> authOperationErrorLiveData;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        userOperationResultLiveData = authRepository.getUserOperationResultLiveData();
        loggedOutLiveData = authRepository.getLoggedOutLiveData();
        userProfileCreationStatusLiveData = authRepository.getUserProfileCreationStatusLiveData();
        authOperationErrorLiveData = authRepository.getAuthOperationErrorLiveData();
    }

    public void login(String email, String password) {
        authRepository.login(email, password);
    }

    public void signUp(String email, String password, String name, String preferences) {
        authRepository.signUp(email, password, name, preferences);
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<FirebaseUser> getUserOperationResultLiveData() {
        return userOperationResultLiveData;
    }

    public LiveData<Boolean> getLoggedOutLiveData() {
        return loggedOutLiveData;
    }

    public LiveData<Boolean> getUserProfileCreationStatusLiveData() {
        return userProfileCreationStatusLiveData;
    }

    public LiveData<String> getAuthOperationErrorLiveData() {
        return authOperationErrorLiveData;
    }
}
