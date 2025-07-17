package xnuvers007.bingrewards.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import xnuvers007.bingrewards.MainActivity;
import xnuvers007.bingrewards.R;
import xnuvers007.bingrewards.utils.KeywordGenerator;
import xnuvers007.bingrewards.utils.NetworkUtils;
import java.util.Random;

public class BackgroundSearchService extends Service {

    private static final String TAG = "BackgroundSearchService";
    private static final String CHANNEL_ID = "BingSearchChannel";
    private static final int NOTIFICATION_ID = 1;

    private Handler handler;
    private Runnable searchRunnable;
    private BingSearchService bingSearchService;
    private KeywordGenerator keywordGenerator;
    private Random random;

    private int totalSearches;
    private int currentSearch = 0;
    private int successfulSearches = 0;
    private int failedSearches = 0;
    private boolean isRunning = false;
    private long searchStartTime;

    // Singleton instance untuk komunikasi dengan MainActivity
    private static BackgroundSearchService instance;

    public static BackgroundSearchService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();

        handler = new Handler(Looper.getMainLooper());
        bingSearchService = new BingSearchService();
        keywordGenerator = new KeywordGenerator();
        random = new Random();

        Log.d(TAG, "BackgroundSearchService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if ("STOP_SEARCH".equals(action)) {
                stopSearchProcess();
                return START_NOT_STICKY;
            }

            totalSearches = intent.getIntExtra("search_count", 30);
            Log.d(TAG, "Starting search process with " + totalSearches + " searches");

            if (!NetworkUtils.isNetworkAvailable(this)) {
                showErrorNotification("No internet connection");
                stopSelf();
                return START_NOT_STICKY;
            }

            startSearchProcess();
        }
        return START_STICKY;
    }

    private void startSearchProcess() {
        if (isRunning) {
            Log.w(TAG, "Search process already running");
            return;
        }

        isRunning = true;
        currentSearch = 0;
        successfulSearches = 0;
        failedSearches = 0;
        searchStartTime = System.currentTimeMillis();

        Notification notification = createNotification("Starting Bing search...", "");
        startForeground(NOTIFICATION_ID, notification);

        Log.d(TAG, "Search process started");
        scheduleNextSearch();
    }

    private void scheduleNextSearch() {
        if (!isRunning || currentSearch >= totalSearches) {
            completeSearchProcess();
            return;
        }

        // Random delay between 8-20 seconds to avoid detection
        int minDelay = 8000;  // 8 seconds
        int maxDelay = 20000; // 20 seconds
        int delay = minDelay + random.nextInt(maxDelay - minDelay);

        Log.d(TAG, "Scheduling next search in " + delay + "ms");

        searchRunnable = this::performSearch;
        handler.postDelayed(searchRunnable, delay);
    }

    private void performSearch() {
        if (!isRunning) {
            Log.d(TAG, "Search process stopped, skipping search");
            return;
        }

        currentSearch++;
        String keyword = keywordGenerator.generateRandomKeyword();

        Log.d(TAG, "Performing search " + currentSearch + "/" + totalSearches + " with keyword: " + keyword);

        // Update notification
        String progressText = currentSearch + "/" + totalSearches;
        String contentText = "Searching: " + keyword;
        Notification notification = createNotification(contentText, progressText);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }

        long searchStart = System.currentTimeMillis();

        // Perform actual search
        bingSearchService.performSearch(keyword, new BingSearchService.SearchCallback() {
            @Override
            public void onSuccess() {
                long searchDuration = System.currentTimeMillis() - searchStart;
                successfulSearches++;

                Log.d(TAG, "Search successful: " + keyword + " (took " + searchDuration + "ms)");

                // Update MainActivity if active
                updateMainActivity(keyword, true);

                // Schedule next search
                scheduleNextSearch();
            }

            @Override
            public void onFailure(String error) {
                failedSearches++;

                Log.w(TAG, "Search failed: " + keyword + " - " + error);

                // Update MainActivity if active
                updateMainActivity(keyword, false);

                // Continue with next search even if current one failed
                scheduleNextSearch();
            }
        });
    }

    private void updateMainActivity(String keyword, boolean success) {
        // Gunakan broadcasting untuk komunikasi dengan MainActivity
        Intent broadcastIntent = new Intent("com.bingrewards.SEARCH_UPDATE");
        broadcastIntent.putExtra("keyword", keyword);
        broadcastIntent.putExtra("current", currentSearch);
        broadcastIntent.putExtra("total", totalSearches);
        broadcastIntent.putExtra("success", success);
        sendBroadcast(broadcastIntent);
    }

    private void completeSearchProcess() {
        isRunning = false;
        long totalDuration = System.currentTimeMillis() - searchStartTime;

        Log.d(TAG, "Search process completed. Success: " + successfulSearches + ", Failed: " + failedSearches);

        String completionText = String.format(
                "✅ Completed! Success: %d, Failed: %d",
                successfulSearches, failedSearches
        );

        Notification notification = createNotification(completionText, "Duration: " + formatDuration(totalDuration));
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }

        // Broadcast completion
        Intent broadcastIntent = new Intent("com.bingrewards.SEARCH_COMPLETED");
        broadcastIntent.putExtra("successful_searches", successfulSearches);
        broadcastIntent.putExtra("failed_searches", failedSearches);
        broadcastIntent.putExtra("total_duration", totalDuration);
        sendBroadcast(broadcastIntent);

        // Stop service after 10 seconds
        handler.postDelayed(() -> {
            stopForeground(true);
            stopSelf();
        }, 10000);
    }

    public void stopSearchProcess() {
        Log.d(TAG, "Stopping search process");
        isRunning = false;

        if (handler != null && searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        Notification notification = createNotification("⏹️ Search process stopped", "");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }

        // Broadcast stop
        Intent broadcastIntent = new Intent("com.bingrewards.SEARCH_STOPPED");
        sendBroadcast(broadcastIntent);

        stopForeground(true);
        stopSelf();
    }

    private void showErrorNotification(String error) {
        Notification notification = createNotification("❌ Error: " + error, "");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(String contentText, String subText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, BackgroundSearchService.class);
        stopIntent.setAction("STOP_SEARCH");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bing Rewards Auto Search")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(isRunning)
                .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent);

        if (!subText.isEmpty()) {
            builder.setSubText(subText);
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bing Search Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notifications for Bing auto search process");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        isRunning = false;

        if (handler != null && searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        Log.d(TAG, "BackgroundSearchService destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}