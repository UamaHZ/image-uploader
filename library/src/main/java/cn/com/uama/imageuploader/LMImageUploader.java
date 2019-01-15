package cn.com.uama.imageuploader;

import android.content.Context;

import com.google.gson.Gson;
import com.uama.retrofit.converter.gson.LMGsonConverterFactory;

import java.io.File;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import uama.com.image.compress.ImageCompressFactory;

public class LMImageUploader {

    private static Api api;
    private static String uploadUrl;
    private static Gson gson;

    /**
     * 初始化（没有配置）
     *
     * @param debug 是否是 debug 模式
     * @deprecated 使用 {@link #init(Config)} 代替
     */
    @Deprecated
    public static void init(boolean debug) {
        init(null, debug);
    }

    /**
     * 根据配置类进行初始化操作
     *
     * @param config 配置信息
     * @param debug  是否是 debug 模式
     * @deprecated 使用 {@link #init(Config)} 代替
     */
    @Deprecated
    public static void init(Config config, boolean debug) {
        init(config);
    }

    /**
     * 根据配置类进行初始化操作
     *
     * @param config 配置信息，不能为 null
     */
    public static void init(Config config) {
        if (api != null) return;
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null, because upload url must be provided.");
        }

        uploadUrl = config.uploadUrl();
        if (uploadUrl == null) {
            throw new IllegalArgumentException("Upload url must be provided.");
        }
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // 设置超时时间
        clientBuilder.connectTimeout(60, TimeUnit.SECONDS);
        clientBuilder.readTimeout(60, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS);

        // 设置拦截器
        List<Interceptor> interceptors = config.interceptors();
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                clientBuilder.addInterceptor(interceptor);
            }
        }

        // 设置 https 证书
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

    /**
     * 将图片压缩之后再进行上传
     *
     * @param context  Context 对象
     * @param pathList 待上传的图片路径列表
     * @param type     类型
     * @param listener 回调接口
     */
    public static void compressAndUpload(Context context, List<String> pathList,
                                         String type, UploadListener listener) {
        List<File> fileList = new ArrayList<>();
        for (String path : pathList) {
            fileList.add(ImageCompressFactory.getNewFile(context, path));
        }
        uploadFiles(fileList, type, listener);
    }

    private static void onError(int errorCode, String errorMessage, UploadListener listener) {
        if (listener != null) {
            listener.onError(String.valueOf(errorCode), errorMessage);
        }
    }

    /**
     * 获取上传图片的 Observable 对象
     *
     * @param fileList 待上传的图片文件列表
     * @param type     类型
     */
    public static Observable<String> uploadFilesObservable(List<File> fileList, String type) {
        return api.uploadObservable(uploadUrl, createPartList(fileList, type))
                .map(new UploadMapFunction());
    }

    /**
     * 获取上传图片的 Observable 对象
     *
     * @param pathList 待上传的图片路径列表
     * @param type     类型
     */
    public static Observable<String> uploadObservable(List<String> pathList, String type) {
        return api.uploadObservable(uploadUrl, createPartList(createFileList(null, pathList, false), type))
                .map(new UploadMapFunction());
    }

    /**
     * 获取压缩图片并上传的 Observable 对象
     *
     * @param context  Context 对象
     * @param pathList 待上传的图片路径列表
     * @param type     类型
     */
    public static Observable<String> compressAndUploadObservable(Context context, List<String> pathList, String type) {
        return api.uploadObservable(uploadUrl, createPartList(createFileList(context, pathList, true), type))
                .map(new UploadMapFunction());
    }

    private static class UploadMapFunction implements Function<UploadResultBean, String> {
        @Override
        public String apply(UploadResultBean uploadResultBean) throws Exception {
            int status = uploadResultBean.getStatus();
            if (status != 100) {
                // 接口报错
                String msg = uploadResultBean.getMsg();
                throw new UploadException(msg);
            }
            List<String> data = uploadResultBean.getData();
            if (data == null) data = new ArrayList<>();
            return gson.toJson(data);
        }
    }

    /**
     * 根据图片路径创建文件列表
     *
     * @param context  Context 对象
     * @param pathList 图片路径列表
     * @param compress 是否压缩
     */
    private static List<File> createFileList(Context context, List<String> pathList, boolean compress) {
        List<File> fileList = new ArrayList<>();
        for (String path : pathList) {
            if (compress) {
                fileList.add(ImageCompressFactory.getNewFile(context, path));
            } else {
                fileList.add(new File(path));
            }
        }
        return fileList;
    }

    /**
     * 根据图片和 type 创建 part 列表
     *
     * @param fileList 带上传的图片文件列表
     * @param type     类型
     */
    private static List<MultipartBody.Part> createPartList(List<File> fileList, String type) {
        List<MultipartBody.Part> partList = new ArrayList<>();
        partList.add(MultipartBody.Part.createFormData("type", type));
        for (File file : fileList) {
            partList.add(MultipartBody.Part.createFormData("files", file.getName(),
                    RequestBody.create(MediaType.parse("image/png"), file)));
        }
        return partList;
    }
}
