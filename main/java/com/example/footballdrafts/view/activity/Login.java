package com.example.footballdrafts.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.footballdrafts.R;
import com.example.footballdrafts.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    AuthViewModel authViewModel;
    Button buttonLogin;
    TextView goToSignup;
    ProgressBar progressBar;

    private boolean isLoginAttemptInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progressBar);
        goToSignup = findViewById(R.id.signupNow);

        setupObservers();

        buttonLogin.setOnClickListener(v -> attemptLogin());

        goToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Register.class);
            startActivity(intent);
        });
    }

    private void setupObservers() {
        // Observer for successful login (FirebaseUser object)
        authViewModel.getUserOperationResultLiveData().observe(this, firebaseUser -> {
            if (!isLoginAttemptInProgress) { // If we are not actively waiting, ignore.
                return;
            }
            // This observer is for successful logins only.
            if (firebaseUser != null) {
                isLoginAttemptInProgress = false; // Login attempt is now handled (success)
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAffinity();
            }
        });

        // Observer for login errors (String error message)
        authViewModel.getAuthOperationErrorLiveData().observe(this, errorMsg -> {
            if (!isLoginAttemptInProgress) {
                return;
            }
            if (errorMsg != null) {
                isLoginAttemptInProgress = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Login Failed: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptLogin() {
        if (isLoginAttemptInProgress) {
            return;
        }

        String email = String.valueOf(editTextEmail.getText()).trim();
        String password = String.valueOf(editTextPassword.getText()).trim();

        if (TextUtils.isEmpty(email)) {  progressBar.setVisibility(View.GONE); return; }
        if (TextUtils.isEmpty(password)) {  progressBar.setVisibility(View.GONE); return; }

        progressBar.setVisibility(View.VISIBLE);
        isLoginAttemptInProgress = true;
        authViewModel.login(email, password);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null && !isLoginAttemptInProgress) {
            Toast.makeText(this, "Already logged in. Redirecting...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        }
    }
}