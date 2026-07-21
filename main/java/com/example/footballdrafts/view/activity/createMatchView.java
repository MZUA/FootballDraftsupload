package com.example.footballdrafts.view.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.footballdrafts.R;
import com.example.footballdrafts.model.Lineup;
import com.example.footballdrafts.model.Match;
import com.example.footballdrafts.model.User;
import com.example.footballdrafts.viewmodel.MatchViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class createMatchView extends AppCompatActivity {

    private static final String TAG = "CreateMatchActivity";
    private TextInputEditText editTextDate, editTextTime, editTextLocation;
    private Button buttonCreateMatch;
    private ProgressBar progressBar;
    private MatchViewModel matchViewModel;


    private Calendar selectedDateTime = Calendar.getInstance();
    private Place selectedPlace;
    private Lineup currentUserLineup;
    private User currentUserProfile;



    private final ActivityResultLauncher<Intent> startAutocomplete =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Place place = Autocomplete.getPlaceFromIntent(result.getData());
                            if (place != null) {
                                selectedPlace = place;
                                String placeName = place.getName();
                                if (placeName != null) {
                                    editTextLocation.setText(placeName);
                                    Log.i(TAG, "Place selected: " + placeName);
                                    String placeAddress = place.getAddress();
                                    if (placeAddress != null) Log.i(TAG, "Address: " + placeAddress);
                                    com.google.android.gms.maps.model.LatLng placeLatLng = place.getLatLng();
                                    if (placeLatLng != null) Log.i(TAG, "LatLng: " + placeLatLng.latitude + ", " + placeLatLng.longitude);
                                } else {
                                    editTextLocation.setText("Selected Location (Name N/A)");
                                    Log.i(TAG, "Place selected, but name is null.");
                                }
                            } else {
                                Log.e(TAG, "Autocomplete returned OK, but Place object was null.");
                                Toast.makeText(this, "Could not retrieve place details.", Toast.LENGTH_SHORT).show();
                            }
                        } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                            Status status = Autocomplete.getStatusFromIntent(result.getData());
                            Log.e(TAG, "Autocomplete Error: " + status.getStatusMessage());
                            Toast.makeText(this, "Error selecting location: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        } else if (result.getResultCode() == RESULT_CANCELED) {
                            Log.i(TAG, "User canceled location selection.");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_match_view);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonCreateMatch = findViewById(R.id.buttonCreateMatch);
        progressBar = findViewById(R.id.progressBarCreateMatch);

        initializePlacesSDK();

        setupDateTimePickers();
        setupLocationPicker();
        observeViewModel();

        matchViewModel.triggerFetchCurrentUserLineup();; // Fetch lineup
        fetchCurrentUserProfile();

        // Set OnClickListener for the Create Match Button
        buttonCreateMatch.setOnClickListener(v -> createMatch());


        //bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.creatematchicon);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.homeicon) {
                startActivity(new Intent(createMatchView.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.lineupicon) {
                startActivity(new Intent(createMatchView.this, LineupView.class));
                finish();
                return true;
            } else if (itemId == R.id.creatematchicon) {
                return true; // Already on this screen
            } else if (itemId == R.id.requesticon) {

                startActivity(new Intent(createMatchView.this, RequestsView.class));
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void initializePlacesSDK() {
        if (!Places.isInitialized()) {
            String apiKey = getString(R.string.google_maps_key);
            if (apiKey.isEmpty() || apiKey.equals("YOUR_API_KEY_PLACEHOLDER_FROM_TEMPLATE") || apiKey.startsWith("YOUR_")) {
                Toast.makeText(this, "Places API Key is not configured in strings.xml!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Places API Key is not configured or is a placeholder in strings.xml! Key: " + apiKey);
                editTextLocation.setEnabled(false);
            } else {
                try {
                    Places.initialize(getApplicationContext(), apiKey);
                    Log.i(TAG, "Places SDK initialized successfully.");
                    editTextLocation.setEnabled(true); // Ensure enabled after successful init
                } catch (Exception e) {
                    Toast.makeText(this, "Error initializing Places SDK: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error initializing Places SDK. Key prefix: " + (apiKey.length() > 5 ? apiKey.substring(0, 6) : apiKey) + ".....", e);
                    editTextLocation.setEnabled(false);
                }
            }
        } else {
            Log.i(TAG, "Places SDK already initialized.");
            editTextLocation.setEnabled(true); // Ensure enabled if already init
        }
    }



    private void setupDateTimePickers() {
        editTextDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateEditText();
                    },
                    selectedDateTime.get(Calendar.YEAR),
                    selectedDateTime.get(Calendar.MONTH),
                    selectedDateTime.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent past dates
            datePickerDialog.show();
        });

        editTextTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateTimeEditText();
                    },
                    selectedDateTime.get(Calendar.HOUR_OF_DAY),
                    selectedDateTime.get(Calendar.MINUTE),
                    false); // False for 12-hour AM/PM format
            timePickerDialog.show();
        });
        updateDateEditText();
        updateTimeEditText();
    }

    private void updateDateEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        editTextDate.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void updateTimeEditText() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        editTextTime.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void setupLocationPicker() {
        editTextLocation.setOnClickListener(v -> {
            if (!Places.isInitialized()) {
                Toast.makeText(this, "Places SDK not ready. Please try again.", Toast.LENGTH_SHORT).show();
                initializePlacesSDK(); // Attempt to re-initialize if not ready
                return;
            }
            if (!editTextLocation.isEnabled()) {
                Toast.makeText(this, "Location input is disabled due to configuration issues.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setHint("Search for a football pitch")
                    .build(this);
            startAutocomplete.launch(intent);
        });
    }

    private void fetchCurrentUserProfile() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(fbUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserProfile = documentSnapshot.toObject(User.class);
                            if (currentUserProfile == null || TextUtils.isEmpty(currentUserProfile.getName())) {

                                // Log or handle missing name, but don't necessarily block if not critical for this screen display
                                Log.w(TAG, "User profile name not found or empty.");
                            }
                        } else {
                            Log.w(TAG, "User profile document not found.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load user profile.", e));
        }
    }

    private void observeViewModel() {
        // Observe the LiveData property for lineup
        matchViewModel.getCurrentUserLineup().observe(this, new Observer<Lineup>() { // Explicit Observer type
            @Override
            public void onChanged(Lineup lineup) { // onChanged method
                if (lineup != null) {
                    currentUserLineup = lineup;
                    Log.d(TAG, "User lineup loaded: " + lineup.getLineupName());
                } else {
                    currentUserLineup = null;
                    Log.d(TAG, "No user lineup found or error fetching lineup.");
                }
            }
        });

        matchViewModel.getMatchCreationStatus().observe(this, success -> {
            if (success != null && success) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Match created successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        matchViewModel.getMatchCreationError().observe(this, error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Match creation failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    // This is the method to call when the "Create Match" button is clicked
    private void createMatch() {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();

        // --- Prerequisite Checks ---
        if (fbUser == null) {
            Toast.makeText(this, "You must be logged in to create a match.", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentUserLineup == null || currentUserLineup.getPlayerPositions() == null || currentUserLineup.getPlayerPositions().isEmpty()) {
            Toast.makeText(this, "A saved lineup with players is required. Please create or update your lineup.", Toast.LENGTH_LONG).show();

            return;
        }

        if (selectedPlace == null) {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show();
            return;
        }

        String placeName = selectedPlace.getName();
        String placeAddress = selectedPlace.getAddress();
        com.google.android.gms.maps.model.LatLng placeLatLng = selectedPlace.getLatLng();

        if (TextUtils.isEmpty(placeName)) {
            Toast.makeText(this, "Selected location is missing a name.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (placeLatLng == null) {
            Toast.makeText(this, "Selected location is missing GPS coordinates. Please select another location.", Toast.LENGTH_LONG).show();
            return;
        }

        // --- Show Progress ---
        progressBar.setVisibility(View.VISIBLE);

        // --- Construct Match Object ---
        Match newMatch = new Match();

        // matchId will be set in repository
        newMatch.setHostUserId(fbUser.getUid());
        newMatch.setHostLineupId(fbUser.getUid()); // Assuming lineupId is the userId
        newMatch.setHostName(currentUserProfile.getName()); // Use fetched profile name
        newMatch.setDateTime(new Timestamp(selectedDateTime.getTime())); // selectedDateTime is a Calendar instance
        newMatch.setLocationName(placeName);
        newMatch.setLocationAddress(placeAddress != null ? placeAddress : "Address not provided"); // Handle null address
        newMatch.setLocationGeoPoint(new GeoPoint(placeLatLng.latitude, placeLatLng.longitude));
        newMatch.setStatus("pending_opponent");

        // --- Call ViewModel to Create Match ---
        matchViewModel.createMatch(newMatch);
    }
}