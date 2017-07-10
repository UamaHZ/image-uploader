package cn.com.uama.imageuploader;

import com.google.gson.Gson;
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
    private static Gson gson;

    /**
     * 根据配置类进行初始化操作
     *
     * @param config 配置信息
     * @param debug  是否是 debug 模式
     */
    public static void init(Config config, boolean debug) {
        if (api != null) return;
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (config != null) {
            uploadUrl = config.uploadUrl();

            List<Interceptor> interceptors = config.interceptors();
            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    clientBuilder.addInterceptor(interceptor);
                }
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

        // 如果没有配置上传接口，使用默认接口
        if (uploadUrl == null || uploadUrl.trim().equals("")) {
            uploadUrl = getDefaultUploadUrl(debug);
        }

        OkHttpClient client = clientBuilder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(uploadUrl.endsWith(File.separator) ? uploadUrl : uploadUrl + File.separator)
                .client(client)
                .addConverterFactory(LMGsonConverterFactory.create(BaseBean.class))
                .build();
        api = retrofit.create(Api.class);
        gson = new Gson();
    }

    /**
     * 获取默认上传接口
     *
     * @param debug 是否是 debug 模式
     * @return 如果 debug 为 true ，返回开放环境和测试环境下的上传接口，否则返回正式环境的上传接口
     */
    private static String getDefaultUploadUrl(boolean debug) {
        if (debug) {
            return "http://121.40.102.80:7888/upload";
        }
        // TODO: 2017/7/10 还没有正式环境的接口地址
        return "http://121.40.102.80:7888/upload";
    }

    /**
     * 上传图片
     *
     * @param fileList 待上传的图片文件列表
     * @param type     类型
     * @param listener 回调接口
     */
    public static void uploadFiles(List<File> fileList, String type, final UploadListener listener) {
        if (api == null) {
            throw new IllegalStateException("LMImageUploader not initialized, call LMImageUploader.init(Config config) in your custom application class first!");
        }
        List<MultipartBody.Part> partList = new ArrayList<>();
        partList.add(MultipartBody.Part.createFormData("type", type));
        for (File file : fileList) {
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
                                listener.onSuccess(gson.toJson(data));
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

    /**
     * 上传图片
     *
     * @param pathList 待上传的图片路径列表
     * @param type     类型
     * @param listener 回调接口
     */
    public static void upload(List<String> pathList, String type, final UploadListener listener) {
        List<File> fileList = new ArrayList<>();
        for (String path : pathList) {
            File file = new File(path);
            fileList.add(file);
        }
        uploadFiles(fileList, type, listener);
    }

    private static void onError(int errorCode, String errorMessage, UploadListener listener) {
        if (listener != null) {
            listener.onError(String.valueOf(errorCode), errorMessage);
        }
    }
}
