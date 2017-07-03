package cn.com.uama.imageuploader;

import java.util.List;

import okhttp3.Interceptor;

/**
 * Created by liwei on 2017/7/3 13:51
 * Email: liwei@uama.com.cn
 * Description: 配置类
 */
public interface Config {
    List<Interceptor> interceptors();
}
