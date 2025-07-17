package xnuvers007.bingrewards.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 15000; // 15 seconds

    public interface NetworkCallback {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    /**
     * Check if device has internet connection
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Check if device has WiFi connection
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    /**
     * Check if device has mobile data connection
     */
    public static boolean isMobileDataConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileInfo != null && mobileInfo.isConnected();
        }
        return false;
    }

    /**
     * Test internet connectivity by pinging a reliable server
     */
    public static void testInternetConnection(Context context, NetworkCallback callback) {
        if (!isNetworkAvailable(context)) {
            callback.onDisconnected();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Test connection to Google DNS
                URL url = new URL("https://www.google.com");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                if (responseCode == 200) {
                    callback.onConnected();
                } else {
                    callback.onError("HTTP " + responseCode);
                }

            } catch (IOException e) {
                Log.e(TAG, "Internet connection test failed", e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Get network type as string
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                switch (activeNetworkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        return "WiFi";
                    case ConnectivityManager.TYPE_MOBILE:
                        return "Mobile Data";
                    case ConnectivityManager.TYPE_ETHERNET:
                        return "Ethernet";
                    default:
                        return "Unknown";
                }
            }
        }
        return "No Connection";
    }

    /**
     * Get detailed network info
     */
    public static String getNetworkInfo(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return String.format(
                        "Type: %s, State: %s, Available: %s, Connected: %s",
                        activeNetworkInfo.getTypeName(),
                        activeNetworkInfo.getState(),
                        activeNetworkInfo.isAvailable(),
                        activeNetworkInfo.isConnected()
                );
            }
        }
        return "No network information available";
    }

    /**
     * Check if connection is metered (limited data plan)
     */
    public static boolean isConnectionMetered(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            return connectivityManager.isActiveNetworkMetered();
        }
        return false;
    }
}