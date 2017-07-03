package cn.com.uama.imageuploader;

import java.io.InputStream;
import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/7/3 13:51
 * Email: liwei@uama.com.cn
 * Description: configuration for uploader
 */
public interface Config {
    List<Interceptor> interceptors();
    String uploadUrl();
    InputStream trustedCertificatesInputStream();
}
