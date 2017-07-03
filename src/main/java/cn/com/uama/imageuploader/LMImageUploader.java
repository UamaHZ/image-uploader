package cn.com.uama.imageuploader;

import com.google.gson.Gson;

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
    private static Gson gson;

    /**
     * 根据配置类进行初始化操作
     */
    public static void init(Config config) {
        if (client != null) return;
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (config != null) {
            for (Interceptor interceptor : config.interceptors()) {
                clientBuilder.addInterceptor(interceptor);
            }
        }
        client = clientBuilder.build();
        gson = new Gson();
        // TODO: 2017/7/3 url 会随环境变化
        uploadUrl = "http://121.40.102.80:7888/upload";
    }

    /**
     * 上传图片
     * @param pathList 待上传的图片路径列表
     * @param type 类型
     * @param listener 回调接口
     */
    public static void upload(List<String> pathList, String type, final UploadListener listener) {
        if (client == null) {
            throw new IllegalStateException("LMImageUploader not initialized, call LMImageUploader.init(Config config) in your custom application class first!");
        }
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
                    try {
                        ResponseBody body = response.body();
                        UploadResultBean uploadResultBean = gson.fromJson(body.charStream(), UploadResultBean.class);
                        int status = uploadResultBean.getStatus();
                        String message = uploadResultBean.getMsg();
                        if (status == 100) {
                            if (listener != null) {
                                List<String> data = uploadResultBean.getData();
                                if (data == null) {
                                    data = new ArrayList<>();
                                }
                                listener.onSuccess(data);
                            }
                        } else {
                            onError(status, message, listener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onError(-1, "", listener);
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
