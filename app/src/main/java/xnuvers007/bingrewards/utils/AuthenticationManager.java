package xnuvers007.bingrewards.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class AuthenticationManager {

    private Context context;
    private OkHttpClient client;

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public AuthenticationManager(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    public void checkBingAuthentication(AuthCallback callback) {
        Request request = new Request.Builder()
                .url("https://rewards.bing.com/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();

                // Check if user is logged in by looking for specific elements
                if (html.contains("id=\"rewards-dashboard\"") ||
                        html.contains("class=\"user-info\"") ||
                        html.contains("Point balance")) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Not authenticated");
                }
                response.close();
            }
        });
    }

    public void openBingLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://login.live.com/login.srf?wa=wsignin1.0&rpsnv=13&ct=1609459200&rver=7.0.6737.0&wp=MBI_SSL&wreply=https%3A%2F%2Faccount.microsoft.com%2F&lc=1033&id=292666&lw=1&fl=easi2"));
        context.startActivity(intent);
    }
}