package cn.com.uama.imageuploader;

/**
 * Created by liwei on 2017/7/3 11:12
 * Email: liwei@uama.com.cn
 * Description: 上传回调接口
 */
public interface UploadListener {
    void onSuccess(String imageUrls);
    void onError(String errorCode, String errorMessage);
}
