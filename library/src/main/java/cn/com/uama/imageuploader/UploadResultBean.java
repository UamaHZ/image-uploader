package cn.com.uama.imageuploader;

import java.util.List;

/**
 * Created by liwei on 2017/7/3 15:02
 * Email: liwei@uama.com.cn
 * Description: upload result entity class
 */
public class UploadResultBean extends BaseBean {
    private List<String> data;

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
