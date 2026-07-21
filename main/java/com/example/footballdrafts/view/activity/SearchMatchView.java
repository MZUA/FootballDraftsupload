package com.example.footballdrafts.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballdrafts.R;
import com.example.footballdrafts.model.Match;
import com.example.footballdrafts.model.MatchRequest;
import com.example.footballdrafts.model.User;
import com.example.footballdrafts.viewmodel.LineupViewModel;
import com.example.footballdrafts.viewmodel.MatchViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects; // For Objects.requireNonNull

public class SearchMatchView extends AppCompatActivity {

    private static final String TAG = "SearchMatchView";

    private RecyclerView recyclerViewMatches;
    private MatchAdapter matchAdapter;
    private List<Match> matchList;
    private MatchViewModel matchViewModel;

    private LineupViewModel lineupViewModel;
    private ProgressBar progressBar;
    private TextView textViewNoMatches;
    private Toolbar toolbar;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_match_view);

        toolbar = findViewById(R.id.toolbarSearchMatch);
        setSupportActionBar(toolbar);
        // Optional: Add Up button for navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to search matches.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        recyclerViewMatches = findViewById(R.id.recyclerViewSearchMatches);
        progressBar = findViewById(R.id.progressBarSearchMatches);
        textViewNoMatches = findViewById(R.id.textViewNoMatchesFound);

        matchList = new ArrayList<>();

        matchAdapter = new MatchAdapter(matchList);
        recyclerViewMatches.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMatches.setAdapter(matchAdapter);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        lineupViewModel = new ViewModelProvider(this).get(LineupViewModel.class);
        lineupViewModel.fetchCurrentUserLineup();

        observeViewModel();
        loadAvailableMatches();
    }

    // Handle Up button press
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadAvailableMatches() {
        progressBar.setVisibility(View.VISIBLE);
        textViewNoMatches.setVisibility(View.GONE);
        recyclerViewMatches.setVisibility(View.GONE); // Hide list while loading
        matchViewModel.triggerFetchAvailableMatches(currentUserId);
    }

    private void observeViewModel() {
        matchViewModel.getAvailableMatchesLiveData().observe(this, matches -> {
            progressBar.setVisibility(View.GONE);
            if (matches != null) {
                matchList.clear();
                matchList.addAll(matches);
                matchAdapter.notifyDataSetChanged();

                if (matches.isEmpty()) {
                    textViewNoMatches.setVisibility(View.VISIBLE);
                    recyclerViewMatches.setVisibility(View.GONE);
                } else {
                    textViewNoMatches.setVisibility(View.GONE);
                    recyclerViewMatches.setVisibility(View.VISIBLE);
                }
            } else {
                // Error case or no data
                textViewNoMatches.setVisibility(View.VISIBLE);
                recyclerViewMatches.setVisibility(View.GONE);
                Log.d(TAG, "Observed matches list is null");
            }
        });

        matchViewModel.getFetchMatchesErrorLiveData().observe(this, error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                textViewNoMatches.setText("Failed to load matches. " + error);
                textViewNoMatches.setVisibility(View.VISIBLE);
                recyclerViewMatches.setVisibility(View.GONE);
                Log.e(TAG, "Error fetching matches: " + error);
            }
        });
    }

    // RecyclerView Adapter
    private class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
        private List<Match> localMatchList;
        private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault());
        private User currentUserProfileData; // To store current user's profile


        MatchAdapter(List<Match> matches) {
            this.localMatchList = matches;
            fetchCurrentUsersProfileData();
        }

        private void fetchCurrentUsersProfileData() {
            FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
            if (fbUser != null) {
                FirebaseFirestore.getInstance().collection("users").document(fbUser.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                currentUserProfileData = documentSnapshot.toObject(User.class);
                            }
                        });
            }
        }

        @NonNull
        @Override
        public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_match, parent, false);
            return new MatchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
            Match match = localMatchList.get(position);

            holder.textHostTeamName.setText(match.getHostName() != null ? match.getHostName() : "Team Name N/A");
            holder.textLocation.setText(match.getLocationName() != null ? match.getLocationName() : "Location TBD");

            if (match.getDateTime() != null && match.getDateTime().toDate() != null) {
                holder.textDateTime.setText(dateTimeFormat.format(match.getDateTime().toDate()));
            } else {
                holder.textDateTime.setText("Date/Time: TBD");
            }

            holder.buttonViewDetails.setOnClickListener(v -> {
                sendRequestForMatch(match, holder.buttonViewDetails);
            });

        }
        private void sendRequestForMatch(Match matchToRequest, Button requestButton) {
            FirebaseUser fbRequester = FirebaseAuth.getInstance().getCurrentUser();
            if (fbRequester == null) {
                Toast.makeText(SearchMatchView.this, "You need to be logged in.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentUserProfileData == null || TextUtils.isEmpty(currentUserProfileData.getName())) {
                Toast.makeText(SearchMatchView.this, "Your profile name is needed to send a request.", Toast.LENGTH_SHORT).show();
                // Optionally re-fetch profile or prompt user
                fetchCurrentUsersProfileData();
                return;
            }

             if (lineupViewModel.getCurrentUserLineup().getValue() == null) {
                 Toast.makeText(SearchMatchView.this, "You need a lineup to send a request.", Toast.LENGTH_SHORT).show();
                 return;
             }

            // Create MatchRequest object
            String requesterName = currentUserProfileData.getName();
            String requesterLineupId = fbRequester.getUid(); // Lineup ID is User ID

            MatchRequest newRequest = new MatchRequest(
                    matchToRequest.getMatchId(),
                    fbRequester.getUid(),
                    requesterName,
                    requesterLineupId,
                    matchToRequest.getHostUserId() // Store host ID for easier querying by host later
            );

            // Use the Activity's MatchViewModel to send the request
            SearchMatchView.this.matchViewModel.sendMatchRequest(newRequest);
            requestButton.setEnabled(false); // Disable button to prevent multiple requests
            requestButton.setText("Request Sent");
            Toast.makeText(SearchMatchView.this, "Request Sent to " + matchToRequest.getHostName(), Toast.LENGTH_SHORT).show();

        }


        @Override
        public int getItemCount() {
            return localMatchList.size();
        }


        class MatchViewHolder extends RecyclerView.ViewHolder {
            TextView textHostTeamName, textLocation, textDateTime;
            Button buttonViewDetails;

            MatchViewHolder(View itemView) {
                super(itemView);
                textHostTeamName = itemView.findViewById(R.id.textViewItemMatchHostTeamName);
                textLocation = itemView.findViewById(R.id.textViewItemMatchLocation);
                textDateTime = itemView.findViewById(R.id.textViewItemMatchDateTime);
                buttonViewDetails = itemView.findViewById(R.id.buttonItemMatchRequest);
            }
        }
    }
}