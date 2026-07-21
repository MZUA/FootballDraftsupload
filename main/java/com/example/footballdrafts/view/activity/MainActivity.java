package com.example.footballdrafts.view.activity;

import static com.google.android.gms.common.util.CollectionUtils.listOf;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.footballdrafts.R;
import com.example.footballdrafts.viewmodel.AuthViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private FloatingActionButton fabSearchMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        Button logoutButton = findViewById(R.id.logout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.homeicon);
        fabSearchMatches = findViewById(R.id.fabSearchMatches);

        logoutButton.setOnClickListener(v -> {
            authViewModel.logout(); // Call logout method on ViewModel
        });

        // --- Observer for Logout Status ---
        authViewModel.getLoggedOutLiveData().observe(this, loggedOut -> {
            if (loggedOut != null && loggedOut) {
                // Logout was successful
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                startActivity(intent);
                finish(); // Finish MainActivity
            }
        });

        fabSearchMatches.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchMatchView.class);
            startActivity(intent);

        });






        // --- Bottom Navigation View Listener ---
        // Use NavigationBarView.OnItemSelectedListener for Material Components 1.3.0+
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId(); // Get item ID using getter

                if (itemId == R.id.homeicon) {
                    // Already on home or reload home fragment/activity
                    // If MainActivity *is* the home screen, do nothing or reload content.
                    return true;
                } else if (itemId == R.id.lineupicon) {
                    startActivity(new Intent(MainActivity.this, LineupView.class));
                    // Consider if you want to finish MainActivity or not.
                    // If LineupView is a major section, you might not want to finish MainActivity.
                    // finish(); // Remove this if you want to return to MainActivity
                    overridePendingTransition(0,0); // Optional: Remove transition animation
                    return true;
                } else if (itemId == R.id.creatematchicon) {
                    startActivity(new Intent(MainActivity.this, createMatchView.class)); // Assuming CreateMatchActivity
                    // finish();
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.requesticon) {
                    startActivity(new Intent(MainActivity.this, RequestsView.class)); // Assuming IncomingRequestsActivity
                    // finish();
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });
    }
}