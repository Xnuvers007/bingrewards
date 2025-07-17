package xnuvers007.bingrewards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xnuvers007.bingrewards.adapters.SearchHistoryAdapter;
import xnuvers007.bingrewards.services.BackgroundSearchService;
import xnuvers007.bingrewards.utils.AuthenticationManager;
import xnuvers007.bingrewards.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etSearchCount;
    private Button btnStartSearch, btnStopSearch, btnCheckAuth;
    private TextView tvStatus, tvProgress, tvStats;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SearchHistoryAdapter adapter;
    private List<String> searchHistory;

    private AuthenticationManager authManager;
    private boolean isAuthenticated = false;
    private boolean isSearching = false;

    // Statistics
    private int totalSearches = 0;
    private int successfulSearches = 0;
    private int failedSearches = 0;

    // Broadcast receiver untuk menerima update dari service
    private final BroadcastReceiver searchUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ("com.bingrewards.SEARCH_UPDATE".equals(action)) {
                String keyword = intent.getStringExtra("keyword");
                int current = intent.getIntExtra("current", 0);
                int total = intent.getIntExtra("total", 0);
                boolean success = intent.getBooleanExtra("success", false);

                updateProgress(current, total, keyword, success);

            } else if ("com.bingrewards.SEARCH_COMPLETED".equals(action)) {
                int successful = intent.getIntExtra("successful_searches", 0);
                int failed = intent.getIntExtra("failed_searches", 0);
                long duration = intent.getLongExtra("total_duration", 0);

                onSearchCompleted(successful, failed, duration);

            } else if ("com.bingrewards.SEARCH_STOPPED".equals(action)) {
                onSearchStopped();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initRecyclerView();
        setupClickListeners();
        checkNetworkConnection();

        authManager = new AuthenticationManager(this);

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.bingrewards.SEARCH_UPDATE");
        filter.addAction("com.bingrewards.SEARCH_COMPLETED");
        filter.addAction("com.bingrewards.SEARCH_STOPPED");
        registerReceiver(searchUpdateReceiver, filter);
    }

    private void initViews() {
        etSearchCount = findViewById(R.id.etSearchCount);
        btnStartSearch = findViewById(R.id.btnStartSearch);
        btnStopSearch = findViewById(R.id.btnStopSearch);
        btnCheckAuth = findViewById(R.id.btnCheckAuth);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        // Add stats textview (if not in layout, you may need to add it)
        tvStats = findViewById(R.id.tvStats);

        etSearchCount.setText("30"); // Default 30 searches
    }

    private void initRecyclerView() {
        searchHistory = new ArrayList<>();
        adapter = new SearchHistoryAdapter(searchHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnCheckAuth.setOnClickListener(v -> checkAuthentication());
        btnStartSearch.setOnClickListener(v -> startSearchProcess());
        btnStopSearch.setOnClickListener(v -> stopSearchProcess());
    }

    private void checkNetworkConnection() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            tvStatus.setText("❌ No internet connection");
            btnStartSearch.setEnabled(false);
            btnCheckAuth.setEnabled(false);
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        } else {
            String networkType = NetworkUtils.getNetworkType(this);
            Log.d(TAG, "Network type: " + networkType);
        }
    }

    private void checkAuthentication() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            tvStatus.setText("❌ No internet connection");
            return;
        }

        tvStatus.setText("Checking authentication...");
        btnCheckAuth.setEnabled(false);

        authManager.checkBingAuthentication(new AuthenticationManager.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    isAuthenticated = true;
                    tvStatus.setText("✅ Authenticated - Ready to search");
                    btnStartSearch.setEnabled(true);
                    btnCheckAuth.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Authentication successful!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    isAuthenticated = false;
                    tvStatus.setText("❌ Not authenticated - Please login to Bing");
                    btnStartSearch.setEnabled(false);
                    btnCheckAuth.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Authentication failed: " + error, Toast.LENGTH_LONG).show();

                    // Open Bing login page
                    authManager.openBingLogin();
                });
            }
        });
    }

    private void startSearchProcess() {
        if (isSearching) {
            Toast.makeText(this, "Search is already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        String countStr = etSearchCount.getText().toString().trim();

        if (TextUtils.isEmpty(countStr)) {
            Toast.makeText(this, "Please enter search count", Toast.LENGTH_SHORT).show();
            return;
        }

        int searchCount;
        try {
            searchCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (searchCount <= 0) {
            Toast.makeText(this, "Search count must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        if (searchCount > 56) {
            Toast.makeText(this, "Maximum 56 searches to avoid detection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAuthenticated) {
            Toast.makeText(this, "Please check authentication first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset statistics
        totalSearches = searchCount;
        successfulSearches = 0;
        failedSearches = 0;
        searchHistory.clear();
        adapter.notifyDataSetChanged();

        // Start service
        Intent serviceIntent = new Intent(this, BackgroundSearchService.class);
        serviceIntent.putExtra("search_count", searchCount);
        startForegroundService(serviceIntent);

        // Update UI
        isSearching = true;
        btnStartSearch.setEnabled(false);
        btnStopSearch.setEnabled(true);
        btnCheckAuth.setEnabled(false);
        tvStatus.setText("🔍 Starting search process...");
        progressBar.setMax(searchCount);
        progressBar.setProgress(0);
        updateStatsDisplay();

        Toast.makeText(this, "Search process started", Toast.LENGTH_SHORT).show();
    }

    private void stopSearchProcess() {
        Intent serviceIntent = new Intent(this, BackgroundSearchService.class);
        serviceIntent.setAction("STOP_SEARCH");
        startService(serviceIntent);

        // UI will be updated via broadcast receiver
        Toast.makeText(this, "Stopping search process...", Toast.LENGTH_SHORT).show();
    }

    private void updateProgress(int current, int total, String keyword, boolean success) {
        runOnUiThread(() -> {
            progressBar.setProgress(current);
            tvProgress.setText(current + "/" + total);
            tvStatus.setText("Searching: " + keyword);

            if (success) {
                successfulSearches++;
            } else {
                failedSearches++;
            }

            // Add to history
            String historyItem = String.format(
                    "%s (%d/%d) %s",
                    keyword, current, total, success ? "✅" : "❌"
            );

            searchHistory.add(0, historyItem);
            if (searchHistory.size() > 50) { // Keep more history
                searchHistory.remove(searchHistory.size() - 1);
            }
            adapter.notifyDataSetChanged();

            updateStatsDisplay();
        });
    }

    private void onSearchCompleted(int successful, int failed, long duration) {
        runOnUiThread(() -> {
            isSearching = false;
            btnStartSearch.setEnabled(true);
            btnStopSearch.setEnabled(false);
            btnCheckAuth.setEnabled(true);

            successfulSearches = successful;
            failedSearches = failed;

            String durationStr = formatDuration(duration);
            tvStatus.setText(String.format(
                    "✅ Completed! Success: %d, Failed: %d (Duration: %s)",
                    successful, failed, durationStr
            ));

            updateStatsDisplay();

            Toast.makeText(this, "Search process completed!", Toast.LENGTH_LONG).show();
        });
    }

    private void onSearchStopped() {
        runOnUiThread(() -> {
            isSearching = false;
            btnStartSearch.setEnabled(true);
            btnStopSearch.setEnabled(false);
            btnCheckAuth.setEnabled(true);
            tvStatus.setText("⏹️ Search process stopped");

            Toast.makeText(this, "Search process stopped", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStatsDisplay() {
        if (tvStats != null) {
            String statsText = String.format(
                    "Total Searches: %d\n" +
                            "Successful Searches: %d\n" +
                            "Failed Searches: %d\n" +
                            "Success Rate: %.2f%%",
                    totalSearches,
                    successfulSearches,
                    failedSearches,
                    (float) successfulSearches / totalSearches * 100
            );
            tvStats.setText(statsText);
        }
    }

private String formatDuration(long durationMillis) {
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(searchUpdateReceiver);
    }
}
