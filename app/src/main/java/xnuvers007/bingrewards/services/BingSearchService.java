package xnuvers007.bingrewards.services;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import android.webkit.CookieManager;

public class BingSearchService {

    private OkHttpClient client;

    public interface SearchCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public BingSearchService() {
        syncCookiesToOkHttp();
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "en-US,en;q=0.5")
                            .header("Accept-Encoding", "gzip, deflate")
                            .header("Connection", "keep-alive")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    public void syncCookiesToOkHttp() {
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie("https://rewards.bing.com/");
        if (cookies != null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("Cookie", cookies)
                                .build();
                        return chain.proceed(request);
                    })
                    .build();
        }
    }

    public void performSearch(String keyword, SearchCallback callback) {
        syncCookiesToOkHttp();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = "https://www.bing.com/search?form=QBRE&q=" + encodedKeyword;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Successfully performed search
                        callback.onSuccess();
                    } else {
                        callback.onFailure("HTTP " + response.code());
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }
}