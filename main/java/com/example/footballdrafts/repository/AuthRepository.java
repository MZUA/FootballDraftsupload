package com.example.footballdrafts.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.example.footballdrafts.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class AuthRepository {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private MutableLiveData<FirebaseUser> userOperationResultLiveData; // For login/signup results
    private MutableLiveData<Boolean> loggedOutLiveData;
    private MutableLiveData<Boolean> userProfileCreationStatusLiveData;
    private MutableLiveData<String> authOperationErrorLiveData; //for errors

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userOperationResultLiveData = new MutableLiveData<>();
        loggedOutLiveData = new MutableLiveData<>();
        userProfileCreationStatusLiveData = new MutableLiveData<>();
        authOperationErrorLiveData = new MutableLiveData<>();

    }

    public void login(String email, String password) {
        //Reset state for this specific operation
        authOperationErrorLiveData.postValue(null); // Clear previous error
        userOperationResultLiveData.postValue(null);   // Clear previous success user

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userOperationResultLiveData.postValue(firebaseAuth.getCurrentUser());
                    } else {
                        String errorMsg = "Login failed";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                            Log.e("AuthRepo", "Login failed: ", task.getException());
                        } else {
                            Log.e("AuthRepo", "Login failed with null exception.");
                        }
                        authOperationErrorLiveData.postValue(errorMsg);
                        userOperationResultLiveData.postValue(null); // Ensure result is null on error
                    }
                });
    }

    public void signUp(String email, String password, String name, String preferences) {
        //Reset State
        authOperationErrorLiveData.postValue(null);
        userProfileCreationStatusLiveData.postValue(null);
        userOperationResultLiveData.postValue(null);


        firebaseAuth.createUserWithEmailAndPassword(email, password)

                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User userProfile = new User(firebaseUser.getUid(), name, email, preferences);
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        userOperationResultLiveData.postValue(firebaseUser);
                                        userProfileCreationStatusLiveData.postValue(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        userProfileCreationStatusLiveData.postValue(false); // Profile creation failed
                                        authOperationErrorLiveData.postValue("Profile creation failed: " + e.getMessage());
                                        Log.e("AuthRepo", "Profile creation failed: ", e);

                                    });
                        }
                    } else {
                        //error handling
                        String errorMsg = "Signup failed";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                            Log.e("AuthRepo", "Signup failed: ", task.getException());
                        }
                        authOperationErrorLiveData.postValue(errorMsg);
                        userProfileCreationStatusLiveData.postValue(false);
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
        loggedOutLiveData.postValue(true);
        userOperationResultLiveData.postValue(null); // Clear user on logout
        authOperationErrorLiveData.postValue(null); // Clear any errors
    }


    public MutableLiveData<FirebaseUser> getUserOperationResultLiveData() {
        return userOperationResultLiveData;
    }
    public MutableLiveData<Boolean> getLoggedOutLiveData() {
        return loggedOutLiveData;
    }
    public MutableLiveData<Boolean> getUserProfileCreationStatusLiveData() {
        return userProfileCreationStatusLiveData;
    }
    public MutableLiveData<String> getAuthOperationErrorLiveData() {
        return authOperationErrorLiveData;
    }
}