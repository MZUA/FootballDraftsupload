package com.example.footballdrafts.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footballdrafts.R;
import com.example.footballdrafts.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextPref;
    Button buttonreg;
    AuthViewModel authViewModel;
    TextView goToLogin;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        authViewModel = new AuthViewModel();

        //Find fields via ID's on the activity_register.xml
        editTextName = findViewById(R.id.name);
        editTextPref = findViewById(R.id.preference);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonreg = findViewById(R.id.signup_btn);
        goToLogin = findViewById(R.id.loginNow);
        progressBar = findViewById(R.id.progressBar);

        //go to login page
        goToLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent Intent = new Intent(getApplicationContext(), Login.class);
                startActivity(Intent);
                finish();
            }


        });

        //When Register button is clicked
        buttonreg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                //initialise local variables
                String name, pref, email, password;
                //get get text from the input fields using .getText() method
                name = String.valueOf(editTextName.getText());
                pref = String.valueOf(editTextPref.getText());
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                //make sure fields are not empty
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Enter Email: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter Password: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (TextUtils.isEmpty(name)) {
                    Toast.makeText(Register.this, "Enter Name: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                } else if (TextUtils.isEmpty(pref)) {
                    Toast.makeText(Register.this, "Enter Preference: ", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                } else {
                    authViewModel.signUp(email, password, name, pref);
                    authViewModel.getUserProfileCreationStatusLiveData().observe(Register.this, isSuccess -> {
                        if (isSuccess != null) {
                            Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

            }
        });

    }
}