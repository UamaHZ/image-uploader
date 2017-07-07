package cn.com.uama.imageuploader;

/**
 * Created by liwei on 2017/7/3 11:12
 * Email: liwei@uama.com.cn
 * Description: 上传回调接口
 */
public interface UploadListener {
    /**
     * successful uploading callback
     *
     * @param imageUrls image url array of JSON format
     */
    void onSuccess(String imageUrls);

    /**
     * failed uploading callback
     *
     * @param errorCode    code representing error type
     * @param errorMessage error message
     */
    void onError(String errorCode, String errorMessage);
}
