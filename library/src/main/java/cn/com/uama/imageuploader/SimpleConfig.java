package cn.com.uama.imageuploader;

import java.io.InputStream;
import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/7/10 15:33
 * Email: liwei@uama.com.cn
 * Description: a simple implementation of {@link Config}
 */
public class SimpleConfig implements Config {
    @Override
    public List<Interceptor> interceptors() {
        return null;
    }

    @Override
    public String uploadUrl() {
        return null;
    }

    @Override
    public InputStream trustedCertificatesInputStream() {
        return null;
    }
}
