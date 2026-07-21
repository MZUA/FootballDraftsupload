package com.example.footballdrafts.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // For ViewModel instantiation

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.footballdrafts.R;
import com.example.footballdrafts.model.Lineup;
import com.example.footballdrafts.model.Player;
import com.example.footballdrafts.viewmodel.LineupViewModel;

import java.util.HashMap;
import java.util.Map;

public class LineupView extends AppCompatActivity {



    private TextInputEditText editTextLineupName;
    private Map<String, Player> currentPlayersInPositions = new HashMap<>();
    private Map<String, View> positionViews = new HashMap<>();

    private static final String POS_ST = "ST";
    private static final String POS_LW = "LW";
    private static final String POS_CM = "CM";
    private static final String POS_RW = "RW";
    private static final String POS_LCB = "LCB";
    private static final String POS_RCB = "RCB";
    private static final String POS_GK = "GK";
    private String[] allPositions = {POS_ST, POS_LW, POS_CM, POS_RW, POS_LCB, POS_RCB, POS_GK};

    private LineupViewModel lineupViewModel;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentLoadedLineupId = null; // Will store the userId if lineup is loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup_view);

        initializePositionViewsAndDefaults(); // Initialize UI before loading data

        // Retrieve ViewModel
        lineupViewModel = new ViewModelProvider(this).get(LineupViewModel.class);
        lineupViewModel.fetchCurrentUserLineup(); // Attempt to load existing lineup

        editTextLineupName = findViewById(R.id.editTextLineupName);

        Button buttonSaveFullLineup = findViewById(R.id.buttonSaveFullLineup);
        buttonSaveFullLineup.setOnClickListener(v -> saveCurrentUserLineup());

        observeViewModel();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.lineupicon);
        // --- Bottom Navigation View Listener ---
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId(); // Get item ID using getter

                if (itemId == R.id.homeicon) {
                    startActivity(new Intent(LineupView.this, MainActivity.class));

                    return true;
                } else if (itemId == R.id.lineupicon) {

                    return true;
                } else if (itemId == R.id.creatematchicon) {
                    startActivity(new Intent(LineupView.this, createMatchView.class));
                    finish();
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.requesticon) {
                    startActivity(new Intent(LineupView.this, RequestsView.class)); // Assuming IncomingRequestsActivity
                    finish();
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });


    }

    //the function obesrves if the userlineup has any existing players in it or not
    private void observeViewModel() {
        //if the
        lineupViewModel.getCurrentUserLineup().observe(this, lineup -> {
            if (lineup != null) {
                currentLoadedLineupId = lineup.getLineupId(); // Should be the userId
                editTextLineupName.setText(lineup.getLineupName());
                currentPlayersInPositions.clear();
                if (lineup.getPlayerPositions() != null) {
                    currentPlayersInPositions.putAll(lineup.getPlayerPositions());
                }
                updateAllPositionUIs(); // Update all views based on loaded data
                Toast.makeText(this, "Lineup '" + lineup.getLineupName() + "' loaded.", Toast.LENGTH_SHORT).show();

            } else {
                // No lineup exists for the user, or an error occurred
                // Reset UI to a default state if needed, or allow user to create one by saving
                currentLoadedLineupId = null;
                editTextLineupName.setText(""); // Clear name if no lineup
                currentPlayersInPositions.clear();
                updateAllPositionUIs(); // Reset all views
                Toast.makeText(this, "No lineup found. Create one by adding players and saving.", Toast.LENGTH_LONG).show();
            }
        });

        lineupViewModel.getLineupSaveStatus().observe(this, isSuccess -> {
            if (isSuccess == null) return; // Initial state or no operation yet

            if (isSuccess) {
                Toast.makeText(this, "Lineup saved successfully!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Failed to save lineup. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializePositionViewsAndDefaults() {
        int[] viewIds = {R.id.positionST, R.id.positionLW, R.id.positionCM, R.id.positionRW,
                R.id.positionLCB, R.id.positionRCB, R.id.positionGK};

        for (int i = 0; i < allPositions.length; i++) {
            String positionKey = allPositions[i];
            View positionView = findViewById(viewIds[i]);
            if (positionView == null) {
                Log.e("LineupView", "Could not find view for position: " + positionKey + " with ID: " + viewIds[i]);
                continue; // Skip if view not found to prevent crash
            }
            positionViews.put(positionKey, positionView);
            updatePositionUIAfterChange(positionKey, null); // Set to default empty state
            positionView.setOnClickListener(v -> showAssignPlayerDialog(positionKey));
        }
    }

    private void updateAllPositionUIs() {
        for (String positionKey : allPositions) {
            Player player = currentPlayersInPositions.get(positionKey);
            updatePositionUIAfterChange(positionKey, player);
        }
    }

    private void showAssignPlayerDialog(final String positionKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_assign_player, null);
        builder.setView(dialogView);

        final TextView dialogTitle = dialogView.findViewById(R.id.textViewDialogPositionTitle);
        final EditText etFirstName = dialogView.findViewById(R.id.editTextPlayerFirstName);
        final EditText etLastName = dialogView.findViewById(R.id.editTextPlayerLastName);
        final Button btnSavePlayer = dialogView.findViewById(R.id.buttonDialogSavePlayer);
        final Button btnRemovePlayer = dialogView.findViewById(R.id.buttonDialogRemovePlayer);

        dialogTitle.setText(String.format("Assign Player to %s", positionKey));

        if (currentPlayersInPositions.containsKey(positionKey) && currentPlayersInPositions.get(positionKey) != null) {
            Player existingPlayer = currentPlayersInPositions.get(positionKey);
            etFirstName.setText(existingPlayer.getFirstName());
            etLastName.setText(existingPlayer.getLastName());
            btnRemovePlayer.setVisibility(View.VISIBLE);
        } else {
            btnRemovePlayer.setVisibility(View.GONE);
        }

        final AlertDialog dialog = builder.create();

        btnSavePlayer.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();

            if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
                Toast.makeText(this, "Enter at least a first or last name.", Toast.LENGTH_SHORT).show();
                return;
            }
            Player player = new Player(firstName, lastName);
            player.setAssignedPosition(positionKey);
            currentPlayersInPositions.put(positionKey, player);
            updatePositionUIAfterChange(positionKey, player);
            dialog.dismiss();
        });

        btnRemovePlayer.setOnClickListener(v -> {
            currentPlayersInPositions.remove(positionKey);
            updatePositionUIAfterChange(positionKey, null);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updatePositionUIAfterChange(String positionKey, Player player) {
        View positionView = positionViews.get(positionKey);
        if (positionView == null) return;

        ImageView tShirtBg = positionView.findViewById(R.id.imageViewTshirtBackground);
        TextView playerInfoText = positionView.findViewById(R.id.textViewPlayerInfo);

        if (tShirtBg == null || playerInfoText == null) {
            Log.e("LineupView", "Tshirt or PlayerInfo TextView not found in included layout for " + positionKey);
            return;
        }

        if (player != null) {
            String displayName = player.getInitials();
            if (displayName.isEmpty()) displayName = player.getLastName();
            if (displayName.isEmpty()) displayName = player.getFirstName();
            if (displayName.isEmpty() || displayName.length() > 7) displayName = positionKey; // Fallback or too long
            else if (displayName.length() > 5 && displayName.contains(" ")) { // Try initials if full name is too long
                String[] names = displayName.split(" ");
                if(names.length > 1 && !names[0].isEmpty() && !names[1].isEmpty()){
                    displayName = ("" + names[0].charAt(0) + names[1].charAt(0)).toUpperCase();
                }
            }


            playerInfoText.setText(displayName.toUpperCase());
            tShirtBg.setImageResource(R.drawable.tshirt_icon_filled);
        } else {
            playerInfoText.setText(positionKey);
            tShirtBg.setImageResource(R.drawable.tshirt_icon_empty);
        }
    }

    private void saveCurrentUserLineup() {
        String lineupName = editTextLineupName.getText().toString().trim();
        if (TextUtils.isEmpty(lineupName)) {
            editTextLineupName.setError("Lineup name is required");
            editTextLineupName.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You need to be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        Lineup lineupToSave = new Lineup(userId, lineupName); // Constructor sets lineupId and userId to userId
        lineupToSave.setPlayerPositions(new HashMap<>(currentPlayersInPositions)); // Crucial: set the players

        lineupViewModel.saveOrUpdateCurrentUserLineup(lineupToSave);



    }
}