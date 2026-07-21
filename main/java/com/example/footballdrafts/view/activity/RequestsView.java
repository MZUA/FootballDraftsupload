package com.example.footballdrafts.view.activity;

import android.os.Bundle;
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
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footballdrafts.R;
import com.example.footballdrafts.model.Match; // Needed to fetch match details for display
import com.example.footballdrafts.model.MatchRequest;
import com.example.footballdrafts.viewmodel.MatchViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // For fetching match details

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RequestsView extends AppCompatActivity {
    private static final String TAG = "RequestsView";
    private RecyclerView recyclerViewRequests;
    private RequestsAdapter adapter;
    private List<MatchRequest> requestList;
    private MatchViewModel matchViewModel;
    private ProgressBar progressBar;
    private TextView textViewNoRequests;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_view);

        Toolbar toolbar = findViewById(R.id.toolbarRequestsView);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        recyclerViewRequests = findViewById(R.id.recyclerViewIncomingRequests);
        progressBar = findViewById(R.id.progressBarRequests);
        textViewNoRequests = findViewById(R.id.textViewNoIncomingRequests);

        requestList = new ArrayList<>();
        adapter = new RequestsAdapter(requestList);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRequests.setAdapter(adapter);

        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);
        observeViewModel();

        progressBar.setVisibility(View.VISIBLE);
        matchViewModel.triggerFetchIncomingRequests(currentUserId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void observeViewModel() {
        matchViewModel.getIncomingRequestsLiveData().observe(this, incomingRequests -> {
            progressBar.setVisibility(View.GONE);
            if (incomingRequests != null) {
                requestList.clear();
                requestList.addAll(incomingRequests);
                adapter.notifyDataSetChanged();
                textViewNoRequests.setVisibility(incomingRequests.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                textViewNoRequests.setVisibility(View.VISIBLE);
            }
        });

        matchViewModel.getFetchIncomingRequestsErrorLiveData().observe(this, error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading requests: " + error, Toast.LENGTH_SHORT).show();
                textViewNoRequests.setText("Failed to load requests.");
                textViewNoRequests.setVisibility(View.VISIBLE);
            }
        });

        matchViewModel.getRequestResponseStatus().observe(this, success -> {
            if (success != null) {
                if (success) {
                    Toast.makeText(this, "Response recorded.", Toast.LENGTH_SHORT).show();
                    // List will auto-update due to real-time listener in repository
                } else {
                    Toast.makeText(this, "Failed to record response.", Toast.LENGTH_SHORT).show();
                }
                // Reset LiveData in ViewModel if it's a one-time event
            }
        });
    }

    private class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {
        private List<MatchRequest> localRequestList;
        // You might want a way to fetch match details (date/location) to display with the request.
        // This adds complexity. For now, just showing requester name.

        RequestsAdapter(List<MatchRequest> requests) {
            this.localRequestList = requests;
        }

        @NonNull @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            MatchRequest request = localRequestList.get(position);
            holder.textRequestingUserName.setText((request.getRequestingUserName() != null ? request.getRequestingUserName() : "A player") + " wants to play!");
            // TODO: Fetch and display match info (e.g., "For match at [Location] on [Date]")
            // This would require an extra Firestore read for each item or embedding some match info in the request.
            // For simplicity now, we'll skip displaying detailed match info here.
            holder.textMatchInfo.setText("Match ID: " + request.getMatchId().substring(0, Math.min(request.getMatchId().length(), 6)) + "...");


            holder.buttonAccept.setOnClickListener(v -> {
                FirebaseUser hostUser = FirebaseAuth.getInstance().getCurrentUser();
                if (hostUser != null && request.getRequestingUserName() != null) {
                    matchViewModel.respondToMatchRequest(
                            request.getMatchId(),
                            request.getRequestingUserId(), // The requestId is the requestingUser's ID
                            request.getRequestingUserId(),
                            request.getRequestingUserLineupId(),
                            request.getRequestingUserName(), // Name of the opponent
                            true // Accepted
                    );
                } else {
                    Toast.makeText(RequestsView.this, "Error processing request.", Toast.LENGTH_SHORT).show();
                }
            });

            holder.buttonDecline.setOnClickListener(v -> {
                FirebaseUser hostUser = FirebaseAuth.getInstance().getCurrentUser();
                if (hostUser != null && request.getRequestingUserName() != null) {
                    matchViewModel.respondToMatchRequest(
                            request.getMatchId(),
                            request.getRequestingUserId(),
                            request.getRequestingUserId(),
                            request.getRequestingUserLineupId(),
                            request.getRequestingUserName(),
                            false // Declined
                    );
                } else {
                    Toast.makeText(RequestsView.this, "Error processing request.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override public int getItemCount() { return localRequestList.size(); }
        class RequestViewHolder extends RecyclerView.ViewHolder {
            TextView textRequestingUserName, textMatchInfo;
            Button buttonAccept, buttonDecline;
            RequestViewHolder(View itemView) {
                super(itemView);
                textRequestingUserName = itemView.findViewById(R.id.textViewRequestingUserName);
                textMatchInfo = itemView.findViewById(R.id.textViewRequestMatchInfo);
                buttonAccept = itemView.findViewById(R.id.buttonAcceptRequest);
                buttonDecline = itemView.findViewById(R.id.buttonDeclineRequest);
            }
        }
    }
}