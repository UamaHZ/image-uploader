package cn.com.uama.imageuploader;

/**
 * Created by liwei on 2019/1/15 13:26
 * Email: liwei@uama.com.cn
 * Description: 图片上传异常
 */
public class UploadException extends IllegalStateException {
    public UploadException(String message) {
        super(message);
    }
}
