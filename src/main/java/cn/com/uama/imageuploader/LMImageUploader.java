package cn.com.uama.imageuploader;

import com.uama.retrofit.converter.gson.LMGsonConverterFactory;

import java.io.File;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LMImageUploader {

    private static Api api;
    private static String uploadUrl;

    /**
     * 根据配置类进行初始化操作
     */
    public static void init(Config config) {
        if (api != null) return;
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (config != null) {
            uploadUrl = config.uploadUrl();

            for (Interceptor interceptor : config.interceptors()) {
                clientBuilder.addInterceptor(interceptor);
            }

            InputStream inputStream = config.trustedCertificatesInputStream();
            if (inputStream != null) {
                // 配置 https 证书
                X509TrustManager trustManager;
                SSLSocketFactory sslSocketFactory;
                try {
                    trustManager = HttpsHelper.trustManagerForCertificates(inputStream);
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{trustManager}, null);
                    sslSocketFactory = sslContext.getSocketFactory();
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
                clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
            }
        }

        OkHttpClient client = clientBuilder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("")
                .client(client)
                .addConverterFactory(LMGsonConverterFactory.create(BaseBean.class))
                .build();
        api = retrofit.create(Api.class);
    }

    /**
     * 上传图片
     *
     * @param pathList 待上传的图片路径列表
     * @param type     类型
     * @param listener 回调接口
     */
    public static void upload(List<String> pathList, String type, final UploadListener listener) {
        if (api == null) {
            throw new IllegalStateException("LMImageUploader not initialized, call LMImageUploader.init(Config config) in your custom application class first!");
        }
        List<MultipartBody.Part> partList = new ArrayList<>();
        partList.add(MultipartBody.Part.createFormData("type", type));
        for (String path : pathList) {
            File file = new File(path);
            partList.add(MultipartBody.Part.createFormData("files", file.getName(),
                    RequestBody.create(MediaType.parse("image/png"), file)));
        }
        api.upload(uploadUrl, partList).enqueue(new Callback<UploadResultBean>() {
            @Override
            public void onResponse(Call<UploadResultBean> call, Response<UploadResultBean> response) {
                if (response.isSuccessful()) {
                    try {
                        UploadResultBean uploadResultBean = response.body();
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

            @Override
            public void onFailure(Call<UploadResultBean> call, Throwable t) {
                onError(-1, "", listener);
            }
        });
    }

    private static void onError(int errorCode, String errorMessage, UploadListener listener) {
        if (listener != null) {
            listener.onError(String.valueOf(errorCode), errorMessage);
        }
    }
}
