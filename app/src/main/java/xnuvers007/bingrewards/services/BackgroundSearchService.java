package xnuvers007.bingrewards.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import xnuvers007.bingrewards.MainActivity;
import xnuvers007.bingrewards.utils.KeywordGenerator;
import java.util.Random;

public class BackgroundSearchService extends Service {

    private static final String CHANNEL_ID = "BingSearchChannel";
    private static final int NOTIFICATION_ID = 1;

    private Handler handler;
    private Runnable searchRunnable;
    private BingSearchService bingSearchService;
    private KeywordGenerator keywordGenerator;

    private int totalSearches;
    private int currentSearch = 0;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        handler = new Handler(Looper.getMainLooper());
        bingSearchService = new BingSearchService();
        keywordGenerator = new KeywordGenerator();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            totalSearches = intent.getIntExtra("search_count", 30);
            startSearchProcess();
        }
        return START_STICKY;
    }

    private void startSearchProcess() {
        if (isRunning) return;

        isRunning = true;
        currentSearch = 0;

        Notification notification = createNotification("Starting Bing search...");
        startForeground(NOTIFICATION_ID, notification);

        scheduleNextSearch();
    }

    private void scheduleNextSearch() {
        if (!isRunning || currentSearch >= totalSearches) {
            completeSearchProcess();
            return;
        }

        // Random delay between 5-15 seconds to avoid detection
        int delay = 5000 + new Random().nextInt(10000);

        searchRunnable = () -> {
            if (isRunning) {
                performSearch();
            }
        };

        handler.postDelayed(searchRunnable, delay);
    }

    private void performSearch() {
        currentSearch++;
        String keyword = keywordGenerator.generateRandomKeyword();

        // Update notification
        Notification notification = createNotification(
                "Searching: " + keyword + " (" + currentSearch + "/" + totalSearches + ")"
        );
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);

        // Perform actual search
        bingSearchService.performSearch(keyword, new BingSearchService.SearchCallback() {
            @Override
            public void onSuccess() {
                // Update UI if MainActivity is active
                MainActivity activity = MainActivity.getInstance();
                if (activity != null) {
                    activity.updateProgress(currentSearch, totalSearches, keyword);
                }

                // Schedule next search
                scheduleNextSearch();
            }

            @Override
            public void onFailure(String error) {
                // Handle failure, maybe retry or skip
                scheduleNextSearch();
            }
        });
    }

    private void completeSearchProcess() {
        isRunning = false;

        Notification notification = createNotification(
                "✅ Completed " + totalSearches + " searches!"
        );
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);

        // Stop service after 5 seconds
        handler.postDelayed(() -> stopSelf(), 5000);
    }

    private Notification createNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bing Rewards Auto Search")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_menu_search)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bing Search Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (handler != null && searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}