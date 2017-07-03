package cn.com.uama.imageuploader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LMImageUploader {

    private static OkHttpClient client;
    private static String uploadUrl;

    public static void init(Config config) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (config != null) {
            for (Interceptor interceptor : config.interceptors()) {
                clientBuilder.addInterceptor(interceptor);
            }
        }
        client = clientBuilder.build();
        uploadUrl = "http://121.40.102.80:7888/upload";
    }

    public static void upload(List<String> pathList, String type, final UploadListener listener) {
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("type", type);
        for (String path : pathList) {
            File file = new File(path);
            requestBodyBuilder.addFormDataPart("files", file.getName(),
                    RequestBody.create(MediaType.parse("image/png"), file));
        }
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBodyBuilder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(-1, "", listener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    JSONObject jsonObject = new JSONObject(body.string());
                    int status = jsonObject.getInt("status");
                    String message = jsonObject.getString("msg");
                    if (status == 100) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        List<String> imageUrls = new ArrayList<>();
                        for (Object o : data) {
                            imageUrls.add((String) o);
                        }
                        if (listener != null) {
                            listener.onSuccess(imageUrls);
                        }
                    } else {
                        onError(status, message, listener);
                    }
                } else {
                    onError(response.code(), "", listener);
                }
            }
        });
    }

    private static void onError(int errorCode, String errorMessage, UploadListener listener) {
        if (listener != null) {
            listener.onError(String.valueOf(errorCode), errorMessage);
        }
    }
}
