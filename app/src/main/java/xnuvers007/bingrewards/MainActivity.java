package xnuvers007.bingrewards;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xnuvers007.bingrewards.services.BackgroundSearchService;
import xnuvers007.bingrewards.utils.AuthenticationManager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etSearchCount;
    private Button btnStartSearch, btnStopSearch, btnCheckAuth;
    private TextView tvStatus, tvProgress;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SearchHistoryAdapter adapter;
    private List<String> searchHistory;

    private AuthenticationManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initRecyclerView();

        authManager = new AuthenticationManager(this);

        btnCheckAuth.setOnClickListener(v -> checkAuthentication());
        btnStartSearch.setOnClickListener(v -> startSearchProcess());
        btnStopSearch.setOnClickListener(v -> stopSearchProcess());
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

        etSearchCount.setText("30"); // Default 30 pencarian
    }

    private void initRecyclerView() {
        searchHistory = new ArrayList<>();
        adapter = new SearchHistoryAdapter(searchHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void checkAuthentication() {
        tvStatus.setText("Checking authentication...");

        authManager.checkBingAuthentication(new AuthenticationManager.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    tvStatus.setText("✅ Authenticated - Ready to search");
                    btnStartSearch.setEnabled(true);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    tvStatus.setText("❌ Not authenticated - Please login to Bing");
                    btnStartSearch.setEnabled(false);
                    // Open Bing login page
                    authManager.openBingLogin();
                });
            }
        });
    }

    private void startSearchProcess() {
        String countStr = etSearchCount.getText().toString().trim();
        if (countStr.isEmpty()) {
            Toast.makeText(this, "Please enter search count", Toast.LENGTH_SHORT).show();
            return;
        }

        int searchCount = Integer.parseInt(countStr);
        if (searchCount > 56) {
            Toast.makeText(this, "Maximum 56 searches to avoid detection", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, BackgroundSearchService.class);
        serviceIntent.putExtra("search_count", searchCount);
        startForegroundService(serviceIntent);

        btnStartSearch.setEnabled(false);
        btnStopSearch.setEnabled(true);
        tvStatus.setText("🔍 Starting search process...");
        progressBar.setMax(searchCount);
        progressBar.setProgress(0);
    }

    private void stopSearchProcess() {
        Intent serviceIntent = new Intent(this, BackgroundSearchService.class);
        stopService(serviceIntent);

        btnStartSearch.setEnabled(true);
        btnStopSearch.setEnabled(false);
        tvStatus.setText("⏹️ Search process stopped");
    }

    public void updateProgress(int current, int total, String keyword) {
        runOnUiThread(() -> {
            progressBar.setProgress(current);
            tvProgress.setText(current + "/" + total);
            tvStatus.setText("Searching: " + keyword);

            searchHistory.add(0, keyword + " (" + current + "/" + total + ")");
            if (searchHistory.size() > 20) {
                searchHistory.remove(searchHistory.size() - 1);
            }
            adapter.notifyDataSetChanged();
        });
    }
}
